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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.	See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.	If not, see <https://www.gnu.org/licenses/>.
 */

package io.github.theluca98.creativeheads.util;

import com.google.common.base.Charsets;
import com.google.common.io.BaseEncoding;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.authlib.properties.Property;
import io.github.theluca98.creativeheads.CreativeHeads;
import io.github.theluca98.creativeheads.util.exception.PlayerNotFound;
import lombok.SneakyThrows;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.profile.PlayerTextures;
import org.bukkit.Bukkit;


import java.io.InputStreamReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;
import java.util.Base64;
import java.net.URL;
import java.net.MalformedURLException;

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

		@SneakyThrows
		public ItemBuilder withCustomSkullProfile(PlayerProfile profile) {
				checkArgument(meta instanceof SkullMeta, "Not a player head item");
				((SkullMeta) meta).setOwnerProfile(profile);
				return this;
		}

		public ItemBuilder withCustomSkullTexture(String textureUrl) {
				var profile = Bukkit.createPlayerProfile(UUID.randomUUID(), "CreativeHeads");
				var textures = profile.getTextures();
				try {
						textures.setSkin(new URL(textureUrl));
						profile.setTextures(textures);
						return withCustomSkullProfile(profile);
				} catch (MalformedURLException e) {
						e.printStackTrace();
						return null;
				}
		}

		public UUID getOnlineUUID(String username) {
				try {
						URL uuidUrl = new URL("https://api.mojang.com/users/profiles/minecraft/" + username);
						InputStreamReader uuidReader = new InputStreamReader(uuidUrl.openStream());
						JsonObject uuidJson = JsonParser.parseReader(uuidReader).getAsJsonObject();
						return UUID.fromString(uuidJson.get("id").getAsString().replaceFirst("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})", "$1-$2-$3-$4-$5"));
				} catch (FileNotFoundException e) {
                        throw new PlayerNotFound("Online UUID for "+username+" not found.", e);
				} catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }
		}

		public String getSkinURL(String uuid) {
				try {
						URL profileUrl = new URL("https://sessionserver.mojang.com/session/minecraft/profile/" + uuid);
						InputStreamReader profileReader = new InputStreamReader(profileUrl.openStream());
						JsonObject profileJson = JsonParser.parseReader(profileReader).getAsJsonObject();
						JsonObject properties = profileJson.get("properties").getAsJsonArray().get(0).getAsJsonObject();
						String texture = properties.get("value").getAsString();

						JsonObject textureJson = JsonParser.parseString(new String(Base64.getDecoder().decode(texture))).getAsJsonObject();
						return textureJson.get("textures").getAsJsonObject().get("SKIN").getAsJsonObject().get("url").getAsString();
				} catch (Exception e) {
						e.printStackTrace();
						return null;
				}
		}

		public ItemBuilder withCustomSkullOwner(String playerName) {
				checkArgument(meta instanceof SkullMeta, "Not a player head item");
				var playerUUID = getOnlineUUID(playerName);
				var textureUrl = getSkinURL(playerUUID.toString());

				var profile = Bukkit.createPlayerProfile(playerUUID, playerName);
				var textures = profile.getTextures();
				try {
						textures.setSkin(new URL(textureUrl));
						profile.setTextures(textures);
						return withCustomSkullProfile(profile);
				} catch (MalformedURLException e) {
						e.printStackTrace();
				}
				return this;
		}

		public ItemStack build() {
				item.setItemMeta(meta);
				return item;
		}

}
