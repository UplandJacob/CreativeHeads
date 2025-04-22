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
import io.github.theluca98.creativeheads.util.ItemBuilder;
import io.github.theluca98.creativeheads.util.exception.PlayerNotFound;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.concurrent.CompletableFuture;

import static com.mojang.brigadier.arguments.StringArgumentType.word;

public class PlayerHeadCommand extends Subcommand {

    private final CreativeHeads plugin;

    public PlayerHeadCommand(CreativeHeads plugin) {
        this.plugin = plugin;
    }

    @Override
    public LiteralArgumentBuilder<CommandSender> node() {
        return literal("player").then(
            argument("username", word())
                .executes(this)
        );
    }

    @Override
    public void onCommand(Player sender, CommandContext<CommandSender> ctx) {
        var username = ctx.getArgument("username", String.class);
        if (!username.matches("\\w+")) {
            sender.sendMessage(ChatColor.RED + "That's not a valid username!");
            return;
        }

        CompletableFuture.supplyAsync(() ->
            ItemBuilder.of(Material.PLAYER_HEAD)
                .withCustomSkullOwner(username)
                .build()
        ).thenAcceptAsync(item -> {
            if (plugin.giveItem(sender, item)) {
                sender.playSound(sender.getLocation(), Sound.ENTITY_ITEM_PICKUP, SoundCategory.MASTER, 1, 1);
            } else {
                sender.sendMessage(ChatColor.RED + "Your inventory is full!");
                sender.playSound(sender.getLocation(), Sound.BLOCK_NOTE_BLOCK_DIDGERIDOO, SoundCategory.MASTER, 1, 0);
            }
        }, plugin.getExecutor()).exceptionally(e -> {
            if (e instanceof PlayerNotFound) {
                sender.sendMessage(ChatColor.RED + e.getMessage());
            } else {
                sender.sendMessage(ChatColor.RED + "An internal error occurred, please try again later.");
                plugin.logException(e);
            }
            return null;
        });
    }

}
