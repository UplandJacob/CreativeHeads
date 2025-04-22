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

package io.github.theluca98.creativeheads.gui;

import io.github.theluca98.creativeheads.CreativeHeads;
import io.github.theluca98.creativeheads.core.gui.ClickFunction;
import io.github.theluca98.creativeheads.core.gui.InventoryGUI;
import io.github.theluca98.creativeheads.model.CustomHead;
import io.github.theluca98.creativeheads.util.ItemBuilder;
import io.github.theluca98.creativeheads.util.Pagination;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;

import static com.google.common.base.Preconditions.checkState;

public class HeadListView extends InventoryGUI {
    public static final int PAGE_SIZE = COLUMNS * 5;

    public HeadListView(CreativeHeads plugin, Pagination<CustomHead> results) {
        this(plugin, results, 0);
    }
    public HeadListView(CreativeHeads plugin, Pagination<CustomHead> results, int pageIndex) {
        super(plugin, String.format("Heads - Page %d of %d", pageIndex+1, results.pageCount()));
        return openView(plugin, results, pageIndex, "");
    }
    public HeadListView(CreativeHeads plugin, Pagination<CustomHead> results, String search) {
        this(plugin, results, 0, search);
    }
    public HeadListView(CreativeHeads plugin, Pagination<CustomHead> results, int pageIndex, String search) {
        super(plugin, String.format("Heads Search: "+search+" - Page %d of %d", pageIndex+1, results.pageCount()));
        return openView(plugin, results, pageIndex, search);
    }

    public openView(CreativeHeads plugin, Pagination<CustomHead> results, int pageIndex, String search) {
        checkState(results.hasPage(pageIndex), "Page does not exist");
        var page = results.getPage(pageIndex);
        for (var head : page) {
            addItem(
                head.toMenuIcon(),
                (player, click) -> {
                    var item = head.toItem();
                    if (click.isRightClick()) {
                        item.setAmount(item.getType().getMaxStackSize());
                    }
                    if (plugin.giveItem(player, item)) {
                        player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, SoundCategory.MASTER, 1, 1);
                    } else {
                        player.sendMessage(ChatColor.RED + "Your inventory is full!");
                        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_DIDGERIDOO, SoundCategory.MASTER, 1, 0);
                    }
                    return false;
                }
            );
        }
        if (results.hasPage(pageIndex - 1)) {
            addItem(
                slot(5, 0),
                ItemBuilder.of(Material.LIME_STAINED_GLASS_PANE).withName(ChatColor.GREEN + "\u25C0 Previous page").build(),
                (player, click) -> {
                    player.playSound(player.getLocation(), Sound.ITEM_BOOK_PAGE_TURN, 1, 1);
                    new HeadListView(plugin, results, pageIndex - 1).open(player);
                    return false;
                }
            );
        } else {
            addItem(
                slot(5, 0),
                ItemBuilder.of(Material.BLACK_STAINED_GLASS_PANE).withName(ChatColor.GRAY + "\u25C0 No more pages").build(),
                ClickFunction.NONE
            );
        }
        if (results.hasPage(pageIndex + 1)) {
            addItem(
                slot(5, 8),
                ItemBuilder.of(Material.LIME_STAINED_GLASS_PANE).withName(ChatColor.GREEN + "Next page \u25B6").build(),
                (player, click) -> {
                    player.playSound(player.getLocation(), Sound.ITEM_BOOK_PAGE_TURN, 1, 1);
                    new HeadListView(plugin, results, pageIndex + 1).open(player);
                    return false;
                }
            );
        } else {
            addItem(
                slot(5, 8),
                ItemBuilder.of(Material.BLACK_STAINED_GLASS_PANE).withName(ChatColor.GRAY + "No more pages \u25B6").build(),
                ClickFunction.NONE
            );
        }

        for (int i = 1; i <= 7; i++) {
            if (i == 4 && search == "") {
                 addItem(
                    slot(5, 4),
                    ItemBuilder.of(Material.BARRIER).withName(ChatColor.YELLOW + "\u25B2 Back to categories \u25B2").build(),
                    (player, click) -> {
                        player.playSound(player.getLocation(), Sound.ITEM_BOOK_PAGE_TURN, 1, 1);
                        new CategoryListView(plugin).open(player);
                        return false;
                    }
                );
            } else {
                addItem(
                    slot(5, i),
                    ItemBuilder.of(Material.GRAY_STAINED_GLASS_PANE).withName(ChatColor.RESET.toString()).build(),
                    ClickFunction.NONE
                );
            }
        }
    }

}
