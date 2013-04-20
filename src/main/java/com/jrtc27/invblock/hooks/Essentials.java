package com.jrtc27.invblock.hooks;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.earth2me.essentials.IEssentials;
import com.jrtc27.invblock.InvBlockPlugin;

public class Essentials {
	private final InvBlockPlugin plugin;

	private final IEssentials essentials;

	public Essentials(final InvBlockPlugin plugin) throws PluginNotFoundException {
		this.plugin = plugin;
		final Plugin essentialsPlugin = this.plugin.getServer().getPluginManager().getPlugin("Essentials");
		if (essentialsPlugin instanceof IEssentials) {
			this.essentials = (IEssentials) essentialsPlugin;
		} else {
			this.essentials = null;
			throw new PluginNotFoundException("Essentials");
		}
	}

	public String getGroup(final Player player) {
		if (this.essentials != null) {
			return player != null ? this.essentials.getUser(player).getGroup() : "default";
		} else {
			return null;
		}
	}
}
