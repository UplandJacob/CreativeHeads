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

package io.github.theluca98.creativeheads.util;

import com.google.common.base.Charsets;
import com.google.common.io.BaseEncoding;
// import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import io.github.theluca98.creativeheads.CreativeHeads;
import lombok.SneakyThrows;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.Bukkit;

import java.util.Arrays;
import java.util.Map;
import java.util.UUID;
import java.net.URL;

import static com.google.common.base.Preconditions.checkArgument;

public class ItemBuilder {

    private final ItemStack item;
    private final ItemMeta meta;

    private ItemBuilder(ItemStack item) {
        this.item = item;
        this.meta = item.getItemMeta();
    }

    public static ItemBuilder of(Material material, int amount) {
        return new ItemBuilder(new ItemStack(material, amount));
    }

    public static ItemBuilder of(Material material) {
        return new ItemBuilder(new ItemStack(material));
    }

    public static ItemBuilder from(ItemStack item) {
        return new ItemBuilder(item.clone());
    }

    public ItemBuilder withName(String name) {
        meta.setDisplayName(name);
        return this;
    }

    public ItemBuilder withLore(String... lore) {
        meta.setLore(Arrays.asList(lore));
        return this;
    }

    public ItemBuilder withHiddenAttributes() {
        meta.addItemFlags(ItemFlag.values());
        return this;
    }

    public ItemBuilder withCustomSkullTexture(String textureUrl) {
        var profile = Bukkit.createPlayerProfile(UUID.randomUUID(), "CreativeHeadsCustomHead");
        var textures = profile.getTextures();
        textures.setSkin(new URL(textureUrl));
        profile.setTextures(textures);
        return withCustomSkullProfile(profile);
    }

    @SneakyThrows
    public ItemBuilder withCustomSkullProfile(PlayerProfile profile) {
        checkArgument(meta instanceof SkullMeta, "Not a player head item");
        ((SkullMeta) meta).setOwnerProfile(profile);
        return this;
    }

    @SuppressWarnings("deprecation")
    public ItemBuilder withCustomSkullOwner(String playerName) {
        checkArgument(meta instanceof SkullMeta, "Not a player head item");
        ((SkullMeta) meta).setOwningPlayer(Bukkit.getOfflinePlayer(playerName));
        return this;
    }

    public ItemStack build() {
        item.setItemMeta(meta);
        return item;
    }

}
