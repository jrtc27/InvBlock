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

package com.jrtc27.invblock;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import com.jrtc27.invblock.listeners.PlayerListener;

public class InvBlockPlugin extends JavaPlugin {
	public String adminMessage = null;
	public final PlayerListener playerListener = new PlayerListener(this);

	private Logger logger;
	private PluginDescriptionFile pdf;
	private BukkitTask updateCheckTask = null;
	private String version = null;
	private String jenkinsBuild = null;
	public boolean checkForUpdates = true;

	@Override
	public void onEnable() {
		this.loadVersionInfo();
		this.logger = this.getLogger();
		this.pdf = this.getDescription();
		this.setupTimers();
		this.getServer().getPluginManager().registerEvents(this.playerListener, this);
		this.playerListener.load();
		this.logInfo("Enabled " + this.pdf.getFullName() + "!");
	}

	@Override
	public void onDisable() {
		this.cancelTimers();
	}

	private void cancelTimers() {
		if (this.updateCheckTask != null) {
			this.updateCheckTask.cancel();
			this.updateCheckTask = null;
		}
	}

	private void setupTimers() {
		this.cancelTimers();
		if (this.version == null || this.version.equalsIgnoreCase("${project.version}")) {
			this.logSevere("Error reading version info file!");
			this.adminMessage = null;
		} else if (this.version.endsWith("-SNAPSHOT")) {
			this.logWarning("You are currently running a snapshot version - please be aware that there may be (serious) bugs!");
			this.adminMessage = null;
		} else if (this.checkForUpdates) {
			this.updateCheckTask = this.getServer().getScheduler().runTaskTimerAsynchronously(this, new Runnable() {
				@Override
				public void run() {
					checkForUpdates();
				}
			}, 20, 432000); // 20 ticks * 60 seconds * 60 minutes * 6 hours => 6 hours in ticks
		} else {
			this.logInfo("Update checking has been disabled!");
		}
	}

	public void loadVersionInfo() {
		final InputStream stream = this.getResource("version-info.yml");
		if (stream != null) {
			final FileConfiguration config = new YamlConfiguration();
			boolean loaded = true;
			try {
				config.load(stream);
			} catch (Exception e) {
				e.printStackTrace();
				loaded = false;
			}
			if (loaded) {
				this.version = config.getString("version");
				this.jenkinsBuild = config.getString("jenkins-build");
				return;
			}
		}
		this.version = null;
		this.jenkinsBuild = null;
	}

	public String getVersion() {
		return this.pdf.getVersion();
	}

	public void log(final Level level, final String message) {
		this.logger.log(level, message);
	}

	public void logInfo(final String message) {
		this.log(Level.INFO, message);
	}

	public void logWarning(final String message) {
		this.log(Level.WARNING, message);
	}

	public void logSevere(final String message) {
		this.log(Level.SEVERE, message);
	}

	public void broadcastAdminMessage(final String message, final boolean sendToConsole) {
		if (sendToConsole) {
			this.getServer().getConsoleSender().sendMessage(message);
		}

		for (final Player player : this.getServer().getOnlinePlayers()) {
			if (player.hasPermission(IBPermission.NOTIFY.node())) {
				player.sendMessage(message);
			}
		}
	}

	public Location getSpawn() {
		for (final World world : this.getServer().getWorlds()) {
			if (world.getEnvironment() == Environment.NORMAL) return world.getSpawnLocation();
		}

		return this.getServer().getWorlds().get(0).getSpawnLocation();
	}

	public void checkForUpdates() {
		BufferedReader reader = null;
		try {
			final URLConnection connection = new URL("http://jrtc27.github.com/InvBlock/version").openConnection();
			connection.setConnectTimeout(10000);
			connection.setReadTimeout(10000);
			reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			final String version = reader.readLine();
			if (version != null) {
				if (this.isVersionNewer(this.version, version)) {
					final String message = "A new recommended version (" + version + ") is available - please update for new features and fixes!";
					this.logInfo(message);
					final String playerMessage = "[InvBlock] " + message;
					this.broadcastAdminMessage(playerMessage, false);
					this.adminMessage = playerMessage;
				} else {
					this.adminMessage = null;
				}
				return;
			}
		} catch (Exception e) {
			// Do nothing
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					// Do nothing
				}
			}
		}
		this.logWarning("Unable to check if plugin was up to date!");
	}

	private boolean isVersionNewer(final String current, final String reported) {
		final String[] currentElements = current.split("\\.");
		final String[] reportedElements = reported.split("\\.");
		final int length = Math.min(currentElements.length, reportedElements.length);
		for (int i = 0; i < length; i++) {
			final int currentInt, reportedInt;
			try {
				currentInt = Integer.valueOf(currentElements[i]);
				reportedInt = Integer.valueOf(reportedElements[i]);
			} catch (NumberFormatException e) {
				return true;
			}
			if (reportedInt > currentInt) return true;
			else if (reportedInt < currentInt) return false;
		}
		return reportedElements.length > currentElements.length;
	}
}
