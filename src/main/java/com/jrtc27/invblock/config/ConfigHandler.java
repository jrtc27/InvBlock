/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jrtc27.invblock.config;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.configuration.file.FileConfiguration;

import com.jrtc27.invblock.InvBlockPlugin;

public class ConfigHandler {
	private final InvBlockPlugin plugin;

	private static final int CURRENT_CONFIG_VERSION = 2;

	private WorldSelectMode inventoryMode;
	private final Set<String> inventoryWorlds = new HashSet<String>();
	private WorldSelectMode respawnMode;
	private final Set<String> respawnWorlds = new HashSet<String>();

	public ConfigHandler(final InvBlockPlugin plugin) {
		this.plugin = plugin;
	}

	public void load() {
		this.loadConfig();
	}

	private void loadConfig() {
		this.plugin.saveDefaultConfig();
		this.upgradeConfig();
		final MemoryConfiguration config = this.plugin.getConfig();
		this.plugin.checkForUpdates = config.getBoolean("check-updates");

		final String inventoryModeString = config.getString("inv-mode").toUpperCase();
		try {
			this.inventoryMode = WorldSelectMode.valueOf(inventoryModeString);
		} catch (IllegalArgumentException e) {
			this.plugin.logSevere("Invalid inv-mode given: " + config.getString("inv-mode") + ". Using the default option of 'WHITELIST'.");
			this.inventoryMode = WorldSelectMode.WHITELIST;
		}
		final List<String> inventoryWorlds = config.getStringList("inv-worlds");
		if (inventoryWorlds != null) this.inventoryWorlds.addAll(inventoryWorlds);

		final String respawnModeString = config.getString("spawn-mode").toUpperCase();
		try {
			this.respawnMode = WorldSelectMode.valueOf(respawnModeString);
		} catch (IllegalArgumentException e) {
			this.plugin.logSevere("Invalid spawn-mode given: " + config.getString("spawn-mode") + ". Using the default option of 'WHITELIST'.");
			this.respawnMode = WorldSelectMode.WHITELIST;
		}
		final List<String> respawnWorlds = config.getStringList("spawn-worlds");
		if (respawnWorlds != null) this.respawnWorlds.addAll(respawnWorlds);
	}

	private void upgradeConfig() {
		final FileConfiguration oldConfig = this.plugin.getConfig();
		int version = oldConfig.getInt("configVersionDoNotTouch", 1);
		if (version < CURRENT_CONFIG_VERSION) {
			this.plugin.logInfo("Upgrading config from version " + version + " to version " + CURRENT_CONFIG_VERSION + ". The old config will be saved in config_old.yml.");
			oldConfig.set("configVersionDoNotTouch", version); // In case it was missing
			try {
				oldConfig.save(new File(this.plugin.getDataFolder(), "config_old.yml"));
			} catch (IOException e) {
				e.printStackTrace();
				return;
			}
			this.plugin.saveResource("config.yml", true);
			this.plugin.reloadConfig();
			final FileConfiguration newConfig = this.plugin.getConfig();
			if (version <= 1) {
				newConfig.set("check-updates", oldConfig.getBoolean("check-updates"));
				newConfig.set("inv-mode", oldConfig.get("mode"));
				newConfig.set("inv-worlds", oldConfig.getStringList("worlds"));
				newConfig.set("spawn-mode", oldConfig.get("mode"));
				newConfig.set("spawn-worlds", oldConfig.getStringList("worlds"));
			}
			this.plugin.saveConfig();
		}
	}

	public boolean inventoryAppliesToWorld(final String world) {
		switch (this.inventoryMode) {
			case WHITELIST:
				return this.inventoryWorlds.contains(world);
			case BLACKLIST:
				return !this.inventoryWorlds.contains(world);
			default:
				return true;
		}
	}

	public boolean respawnAppliesToWorld(final String world) {
		switch (this.respawnMode) {
			case WHITELIST:
				return this.respawnWorlds.contains(world);
			case BLACKLIST:
				return !this.respawnWorlds.contains(world);
			default:
				return true;
		}
	}

	private enum WorldSelectMode {
		BLACKLIST,
		WHITELIST;
	}
}
