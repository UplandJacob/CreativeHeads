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

package io.github.theluca98.creativeheads.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.github.theluca98.creativeheads.CreativeHeads;
import io.github.theluca98.creativeheads.core.command.Subcommand;
import io.github.theluca98.creativeheads.gui.HeadListView;
import io.github.theluca98.creativeheads.util.Pagination;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static com.mojang.brigadier.arguments.StringArgumentType.greedyString;

public class SearchCommand extends Subcommand {

    private final CreativeHeads plugin;

    public SearchCommand(CreativeHeads plugin) {
        this.plugin = plugin;
    }

    @Override
    public LiteralArgumentBuilder<CommandSender> node() {
        return literal("search").then(
            argument("query", greedyString())
                .executes(this)
        );
    }

    @Override
    public void onCommand(Player sender, CommandContext<CommandSender> ctx) {
        String query = ctx.getArgument("query", String.class);
        plugin.getRepository().executeSearchAsync(query)
            .thenApplyAsync(list -> new Pagination<>(list, HeadListView.PAGE_SIZE))
            .thenAcceptAsync((results) -> {
                if (results.hasPage(0)) {
                    new HeadListView(plugin, results, 0, query).open(sender);
                } else {
                    sender.sendMessage(ChatColor.RED + "The search returned no results.");
                }
            }, plugin.getExecutor())
            .exceptionally(e -> {
                sender.sendMessage(ChatColor.RED + "An internal error occurred, please try again later.");
                plugin.logException(e);
                return null;
            });
    }

}
