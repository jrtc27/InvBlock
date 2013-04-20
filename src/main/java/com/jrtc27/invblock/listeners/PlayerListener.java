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

package com.jrtc27.invblock.listeners;

import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

import com.jrtc27.invblock.IBPermission;
import com.jrtc27.invblock.InvBlockPlugin;

public class PlayerListener implements Listener {
	private final InvBlockPlugin plugin;

	public PlayerListener(final InvBlockPlugin plugin) {
		this.plugin = plugin;
	}

	public void load() {
		// Nothing to do (yet)
	}

	@EventHandler(ignoreCancelled = true)
	public void onPlayerPickupItem(final PlayerPickupItemEvent event) {
		event.setCancelled(IBPermission.PICKUP.has(event.getPlayer()));
	}

	@EventHandler(ignoreCancelled = true)
	public void onPlayerDropItem(final PlayerDropItemEvent event) {
		// Will only be called when the player uses their drop key, as clicking outside of an inventory
		// is already cancelled below when necessary.
		event.setCancelled(IBPermission.DROP.has(event.getPlayer()));
	}

	@EventHandler(ignoreCancelled = true)
	public void onInventoryClick(final InventoryClickEvent event) {
		final HumanEntity human = event.getWhoClicked();

		final boolean cancelEvent;
		if (event.getSlotType() == SlotType.OUTSIDE || event.getRawSlot() == -999) { // Use magic number because of BUKKIT-2768
			cancelEvent = IBPermission.DROP.has(human);
		} else {
			cancelEvent = IBPermission.MOVE_ITEMS.has(human);
		}

		if (cancelEvent) {
			event.setCancelled(true);

			if (human instanceof Player) {
				((Player) human).updateInventory(); // Deprecated but necessary
			}
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onPlayerJoin(final PlayerJoinEvent event) {
		final Player player = event.getPlayer();
		if (IBPermission.CLEAR_ITEMS.has(player)) {
			player.getInventory().clear();
		}
		if (IBPermission.CLEAR_ARMOR.has(player)) {
			player.getInventory().setArmorContents(null);
		}
		if (IBPermission.RESPAWN.has(player)) {
			player.teleport(this.plugin.getSpawn(player), TeleportCause.PLUGIN);
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onPlayerQuit(final PlayerQuitEvent event) {
		final Player player = event.getPlayer();
		if (IBPermission.CLEAR_ITEMS.has(player)) {
			player.getInventory().clear();
		}
		if (IBPermission.CLEAR_ARMOR.has(player)) {
			player.getInventory().setArmorContents(null);
		}
		if (IBPermission.RESPAWN.has(player)) {
			player.teleport(this.plugin.getSpawn(player), TeleportCause.PLUGIN);
		}
	}
}
