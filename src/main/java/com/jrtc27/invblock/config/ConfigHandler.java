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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.configuration.MemoryConfiguration;

import com.jrtc27.invblock.InvBlockPlugin;

public class ConfigHandler {
	private final InvBlockPlugin plugin;

	private final Set<String> worlds = new HashSet<String>();
	private WorldSelectMode mode;

	public ConfigHandler(final InvBlockPlugin plugin) {
		this.plugin = plugin;
	}

	public void load() {
		this.loadConfig();
	}

	private void loadConfig() {
		this.plugin.saveDefaultConfig();
		final MemoryConfiguration config = this.plugin.getConfig();
		this.plugin.checkForUpdates = config.getBoolean("check-updates", true);
		final String modeString = config.getString("mode", "").toUpperCase();
		try {
			this.mode = WorldSelectMode.valueOf(modeString);
		} catch (IllegalArgumentException e) {
			this.plugin.logSevere("Invalid mode given: " + config.getString("mode") + ". Using the default option of 'WHITELIST'.");
			this.mode = WorldSelectMode.WHITELIST;
		}
		final List<String> worlds = config.getStringList("worlds");
		if (worlds != null) this.worlds.addAll(worlds);
	}

	public boolean appliesToWorld(final String world) {
		switch (this.mode) {
			case WHITELIST:
				return this.worlds.contains(world);
			case BLACKLIST:
				return !this.worlds.contains(world);
			default:
				return true;
		}
	}

	private enum WorldSelectMode {
		BLACKLIST,
		WHITELIST;
	}
}
