package com.jrtc27.invblock.hooks;

public class PluginNotFoundException extends HookAttachException {
	private static final long serialVersionUID = 989943549696228505L;

	public final String pluginName;

	public PluginNotFoundException(final String pluginName) {
		super("Plugin not found: " + pluginName);
		this.pluginName = pluginName;
	}
}
