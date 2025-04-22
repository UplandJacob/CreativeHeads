/*
 * CreativeHeads
 * Copyright (C) 2022 Luca <https://github.com/TheLuca98>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package io.github.theluca98.creativeheads.core.command;

import com.google.common.collect.Iterables;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.tree.CommandNode;
import io.github.theluca98.creativeheads.CreativeHeads;
import lombok.SneakyThrows;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.List;
import java.util.stream.Collectors;

public class MainCommandDispatcher implements CommandExecutor, TabCompleter {

    private final CreativeHeads plugin;
    private final CommandDispatcher<CommandSender> dispatcher;

    public MainCommandDispatcher(CreativeHeads plugin) {
        this.plugin = plugin;
        this.dispatcher = new CommandDispatcher<>();
    }

    public void register(Subcommand subcommand) {
        dispatcher.register(subcommand.node());
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        var input = String.join(" ", args);
        var results = dispatcher.parse(input, sender);
        try {
            dispatcher.execute(results);
        } catch (CommandSyntaxException e) {
            var usages = getUsageStrings(results);
            if (usages.isEmpty()) {
                sender.sendMessage(ChatColor.RED + e.getMessage());
            } else {
                sender.sendMessage(ChatColor.GOLD + "Usage:");
                for (String usage : usages) {
                    sender.sendMessage(String.format("  /%s %s", label, usage));
                }
            }
        } catch (Exception e) {
            sender.sendMessage(ChatColor.RED + "Sorry, an internal error occurred. Check the console for details.");
            plugin.logException(e);
        }
        return true;
    }

    @Override
    @SneakyThrows
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        var input = String.join(" ", args);
        var parseResults = dispatcher.parse(input, sender);
        return dispatcher
            .getCompletionSuggestions(parseResults)
            .get()
            .getList()
            .stream()
            .map(Suggestion::getText)
            .collect(Collectors.toList());
    }

    private List<String> getUsageStrings(ParseResults<CommandSender> results) {
        CommandNode<CommandSender> currentNode;
        List<ParsedCommandNode<CommandSender>> nodes = results.getContext().getNodes();
        if (nodes.isEmpty()) {
            currentNode = dispatcher.getRoot();
        } else {
            currentNode = nodes.get(nodes.size() - 1).getNode();
        }
        var currentPath = String.join(" ", dispatcher.getPath(currentNode));
        return dispatcher.getSmartUsage(currentNode, results.getContext().getSource())
            .values()
            .stream()
            .map(value -> (currentPath + " " + value).trim())
            .collect(Collectors.toList());
    }

}
