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

import org.bukkit.entity.HumanEntity;

public enum IBPermission {
	PICKUP,
	DROP,
	MOVE_ITEMS,
	RESPAWN,
	CLEAR_ITEMS,
	CLEAR_ARMOR,
	NOTIFY;

	private static InvBlockPlugin plugin;

	public static void setPlugin(final InvBlockPlugin plugin) {
		IBPermission.plugin = plugin;
	}

	private final String name;

	private IBPermission() {
		this.name = this.name().toLowerCase().replaceAll("_", "-");
	}

	private IBPermission(final String name) {
		this.name = name;
	}

	public String node() {
		return "invblock." + this.name;
	}

	public boolean has(final HumanEntity humanEntity) {
		switch (this) {
			case PICKUP:
			case DROP:
			case MOVE_ITEMS:
			case CLEAR_ITEMS:
			case CLEAR_ARMOR:
				if (IBPermission.plugin.configHandler.inventoryAppliesToWorld(humanEntity.getWorld().getName())) {
					return humanEntity.hasPermission(this.node());
				}
				return false;
			case RESPAWN:
				if (IBPermission.plugin.configHandler.respawnAppliesToWorld(humanEntity.getWorld().getName())) {
					return humanEntity.hasPermission(this.node());
				}
				return false;
			case NOTIFY:
			default:
				IBPermission.plugin.logSevere(this.getClass().getCanonicalName() + ".has(HumanEntity) does not apply to " + this.name());
				return false;
		}
	}
}
