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

    /**
     * Handles the InventoryClickEvent to process button clicks in the WardrobeGUI.
     *
     * @param event The event triggered when a player clicks in an inventory.
     */
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        final Player player = (Player) event.getWhoClicked();
        final int slot = event.getSlot();
        event.setCancelled(false);

        // Check if the player has the "WardrobeGUI" metadata
        if (player.hasMetadata("WardrobeGUI")) {
            final WardrobeGUI menu = (WardrobeGUI) player.getMetadata("WardrobeGUI").get(0).value();

            // Iterate through all buttons in the WardrobeGUI
            for (final Button button : menu.getButtons()) {
                Inventory inventoryClicked = event.getClickedInventory();

                // Check if the clicked inventory is the top inventory in the player's open inventory
                if (inventoryClicked != null && inventoryClicked.equals(player.getOpenInventory().getTopInventory())) {

                    // Check if the clicked slot matches the button's slot
                    if (button.getSlot() == slot) {
                        button.onClick(player);
                        event.setCancelled(true); // Cancel the event to prevent default behavior
                    }
                }
            }
        }
    }

    /**
     * Handles the InventoryCloseEvent to save the player's inventory if they have the "WardrobeGUI" metadata.
     *
     * @param event The event triggered when a player closes an inventory.
     */
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        final Player player = (Player) event.getPlayer();

        // Check if the player has the "WardrobeGUI" metadata
        if (player.hasMetadata("WardrobeGUI")) {
            UUID playerUUID = player.getUniqueId();
            Inventory inventory = event.getInventory();

            // Save the player's inventory contents
            WardrobeData.getInstance().getSavedInventories().put(playerUUID, inventory.getContents());
            WardrobeData.getInstance().saveInventories();
        }

        // Remove the "WardrobeGUI" metadata from the player
        if (player.hasMetadata("WardrobeGUI")) {
            player.removeMetadata("WardrobeGUI", Wardrobe.getInstance());
        }
    }
}