package me.plainioldmoose.mooseswardrobe.GUI;

import me.plainioldmoose.mooseswardrobe.Wardrobe;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

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
            player.removeMetadata("WardrobeGUI", Wardrobe.getInstance());
        }
    }

    private boolean isArmour(ItemStack material) {
        switch (material.getType()) {
            case LEATHER_HELMET:
            case LEATHER_CHESTPLATE:
            case LEATHER_LEGGINGS:
            case LEATHER_BOOTS:
            case CHAINMAIL_HELMET:
            case CHAINMAIL_CHESTPLATE:
            case CHAINMAIL_LEGGINGS:
            case CHAINMAIL_BOOTS:
            case IRON_HELMET:
            case IRON_CHESTPLATE:
            case IRON_LEGGINGS:
            case IRON_BOOTS:
            case DIAMOND_HELMET:
            case DIAMOND_CHESTPLATE:
            case DIAMOND_LEGGINGS:
            case DIAMOND_BOOTS:
            case GOLDEN_HELMET:
            case GOLDEN_CHESTPLATE:
            case GOLDEN_LEGGINGS:
            case GOLDEN_BOOTS:
            case NETHERITE_HELMET:
            case NETHERITE_CHESTPLATE:
            case NETHERITE_LEGGINGS:
            case NETHERITE_BOOTS:
                return true;
            default:
                return false;
        }
    }
}
