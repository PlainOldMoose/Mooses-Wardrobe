package me.plainioldmoose.mooseswardrobe.GUI;

import me.plainioldmoose.mooseswardrobe.Data.WardrobeData;
import me.plainioldmoose.mooseswardrobe.Wardrobe;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.Map;
import java.util.UUID;

public final class WardrobeListener implements Listener {
    private UUID checkedPlayerUUID;

    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent event) {
        final Player player = (Player) event.getPlayer();

        // Checks for any admins viewing wardrobes, if found disables wardrobes for everyone
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.hasMetadata("CheckingWardrobe") && !p.equals(player)) {
                player.sendMessage("§f[§c§lWardrobe§f]§c Admin is checking wardrobes!");
                player.removeMetadata("WardrobeGUI", Wardrobe.getInstance());
                event.setCancelled(true);
                return;
            }
        }

        Map<UUID, ItemStack[]> inventories = WardrobeData.getInstance().getSavedInventories();
        Inventory inventory = event.getInventory();

        // The below code is only used to find the UUID of the player who's inventory you are checking, this is used later to save the inventory after an admin has edited it.
        // Check if the player has the "WardrobeGUI" metadata
        if (player.hasMetadata("CheckingWardrobe")) {
            for (Map.Entry<UUID, ItemStack[]> entry : inventories.entrySet()) {
                if (Arrays.equals(entry.getValue(), inventory.getContents())) {
                    UUID playerUUID = entry.getKey();
                    checkedPlayerUUID = entry.getKey();
                    Player checkedPlayer = Bukkit.getPlayer(playerUUID);
                    if (checkedPlayer.hasMetadata("WardrobeGUI")) {
                        checkedPlayer.closeInventory();
                        checkedPlayer.sendMessage("§f[§c§lWardrobe§f]§c Admin is checking your wardrobe!");
                        checkedPlayer.removeMetadata("WardrobeGUI", Wardrobe.getInstance());
                    }
                }
            }
        }
    }

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
        if (player.hasMetadata("WardrobeGUI") || player.hasMetadata("CheckingWardrobe")) {
            final WardrobeGUI menu;
            if (player.hasMetadata("WardrobeGUI")) {
                menu = (WardrobeGUI) player.getMetadata("WardrobeGUI").get(0).value();
            } else {
                menu = (WardrobeGUI) player.getMetadata("CheckingWardrobe").get(0).value();
            }

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
        } else if (player.hasMetadata("CheckingWardrobe")) {
            Inventory inventory = event.getInventory();

            // This fetches from the openInvetoryEvent where the UUID of the player being checked was stored
            WardrobeData.getInstance().getSavedInventories().put(checkedPlayerUUID, inventory.getContents());
            WardrobeData.getInstance().saveInventories();
        }

        // Remove the "WardrobeGUI" metadata from the player
        if (player.hasMetadata("WardrobeGUI")) {
            player.removeMetadata("WardrobeGUI", Wardrobe.getInstance());
        }

        if (player.hasMetadata("CheckingWardrobe")) {
            player.removeMetadata("CheckingWardrobe", Wardrobe.getInstance());
        }
    }
}