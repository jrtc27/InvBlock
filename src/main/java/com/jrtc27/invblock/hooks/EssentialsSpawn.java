package com.jrtc27.invblock.hooks;

import java.lang.reflect.Field;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.earth2me.essentials.spawn.SpawnStorage;
import com.jrtc27.invblock.InvBlockPlugin;

public class EssentialsSpawn {
	private final InvBlockPlugin plugin;
	private final HooksManager hooksManager;

	private final SpawnStorage essentialsSpawns;

	public EssentialsSpawn(final InvBlockPlugin plugin, final HooksManager hooksManager) throws HookAttachException {
		this.plugin = plugin;
		this.hooksManager = hooksManager;
		final Plugin essentialsSpawnPlugin = this.plugin.getServer().getPluginManager().getPlugin("EssentialsSpawn");
		if (essentialsSpawnPlugin instanceof com.earth2me.essentials.spawn.EssentialsSpawn) {
			final Field[] fields = com.earth2me.essentials.spawn.EssentialsSpawn.class.getDeclaredFields();
			final Class spawnStorageClass = SpawnStorage.class;
			Field spawnsField = null;
			for (final Field field : fields) {
				if (spawnStorageClass.isAssignableFrom(field.getType())) {
					spawnsField = field;
					break;
				}
			}
			if (spawnsField != null) {
				final SpawnStorage spawns;
				try {
					spawnsField.setAccessible(true);
					spawns = (SpawnStorage) spawnsField.get(essentialsSpawnPlugin);
				} catch (IllegalAccessException e) {
					this.essentialsSpawns = null;
					throw new HookAttachException("Failed to access field: " + spawnsField.toString());
				}
				this.essentialsSpawns = spawns;
			} else {
				this.essentialsSpawns = null;
				throw new HookAttachException("Failed to find field with type " + spawnStorageClass.getCanonicalName());
			}
		} else {
			this.essentialsSpawns = null;
			throw new PluginNotFoundException("Essentials");
		}
	}

	public Location getSpawn(final Player player) {
		if (this.essentialsSpawns != null) {
			final String group = hooksManager.essentials.getGroup(player);
			return group != null ? this.essentialsSpawns.getSpawn(group) : null;
		} else {
			return null;
		}
	}
}
