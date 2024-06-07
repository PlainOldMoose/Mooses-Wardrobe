package me.plainioldmoose.mooseswardrobe.GUI;

import me.plainioldmoose.mooseswardrobe.Data.WardrobeData;
import me.plainioldmoose.mooseswardrobe.Wardrobe;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;

import java.util.UUID;

public final class WardrobeListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        final Player player = (Player) event.getWhoClicked();
        final int slot = event.getSlot();
        event.setCancelled(false);

        if (player.hasMetadata("WardrobeGUI")) {
            final WardrobeGUI menu = (WardrobeGUI) player.getMetadata("WardrobeGUI").get(0).value();

            for (final Button button : menu.getButtons()) {
                Inventory inventoryClicked = event.getClickedInventory();
                if (inventoryClicked != null && inventoryClicked.equals(player.getOpenInventory().getTopInventory())) {
                    if (button.getSlot() == slot) {
                        button.onClick(player);
                        event.setCancelled(true);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        final Player player = (Player) event.getPlayer();

        if (player.hasMetadata("WardrobeGUI")) {
            UUID playerUUID = player.getUniqueId();
            Inventory inventory = event.getInventory();

            WardrobeData.getInstance().getSavedInventories().put(playerUUID, inventory.getContents());
            WardrobeData.getInstance().saveInventories();
        }

        if (player.hasMetadata("WardrobeGUI")) {
            player.removeMetadata("WardrobeGUI", Wardrobe.getInstance());
        }
    }
}