package com.jrtc27.invblock.hooks;

import com.jrtc27.invblock.InvBlockPlugin;

public class HooksManager {
	private final InvBlockPlugin plugin;

	public final Essentials essentials;
	public final EssentialsSpawn essentialsSpawn;

	public HooksManager(final InvBlockPlugin plugin) {
		this.plugin = plugin;

		final Essentials essentials;
		try {
			essentials = new Essentials(this.plugin);
		} catch (PluginNotFoundException e) {
			this.essentials = null;
			this.essentialsSpawn = null;
			return;
		}
		this.essentials = essentials;

		final EssentialsSpawn essentialsSpawn;
		try {
			essentialsSpawn = new EssentialsSpawn(this.plugin, this);
			this.plugin.logInfo("Hooking into EssentialsSpawn!");
		} catch (HookAttachException e) {
			this.essentialsSpawn = null;
			return;
		}
		this.essentialsSpawn = essentialsSpawn;
	}

}
