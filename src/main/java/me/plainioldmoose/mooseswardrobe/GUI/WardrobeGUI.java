package me.plainioldmoose.mooseswardrobe.GUI;

import me.plainioldmoose.mooseswardrobe.Wardrobe;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * The WardrobeGUI class represents a custom inventory GUI for the Wardrobe plugin.
 */
public class WardrobeGUI {
    private final List<Button> buttons = new ArrayList<>();
    private int size = 9 * 6;
    private String title = "Wardrobe";

    public List<Button> getButtons() {
        return buttons;
    }

    protected void addButton(Button button) {
        this.buttons.add(button);
    }

    protected void setSize(int size) {
        this.size = size;
    }

    protected void setTitle(String title) {
        this.title = title;
    }

    /**
     * Displays the wardrobe GUI to a player.
     *
     * @param player The player to display the GUI to.
     */
    public void displayTo(Player player) {
        final Inventory inventory = Bukkit.createInventory(player, this.size,
                ChatColor.translateAlternateColorCodes('&', this.title));

        createGrayPaneButtons();
        createArmourSlots();
        createEquipUnequipButtons(inventory);

        final Button closeButton = new Button(this.size - 5) {
            @Override
            public ItemStack getItem() {
                return createItemStack(Material.BARRIER, ChatColor.GOLD + "Close");
            }

            @Override
            public void onClick(Player player) {
                Bukkit.getScheduler().runTaskLater(Wardrobe.getInstance(), player::closeInventory, 1);
            }
        };
        this.buttons.add(closeButton);

        for (final Button button : this.buttons) {
            inventory.setItem(button.getSlot(), button.getItem());
        }

        if (player.hasMetadata("WardrobeGUI")) {
            player.closeInventory();
        }

        player.setMetadata("WardrobeGUI", new FixedMetadataValue(Wardrobe.getInstance(), this));
        player.openInventory(inventory);
    }

    /**
     * Creates background tiles and provides functionality for clicking on a background tile with a piece of armour.
     */
    private void createArmourSlots() {
        Map<Integer, String> loreMap = Map.of(
                0, "Helmet",
                1, "Chestplate",
                2, "Leggings",
                3, "Boots"
        );

        Map<Integer, Material> materialMap = Map.of(
                0, Material.ORANGE_STAINED_GLASS_PANE,
                1, Material.YELLOW_STAINED_GLASS_PANE,
                2, Material.LIME_STAINED_GLASS_PANE,
                3, Material.GREEN_STAINED_GLASS_PANE,
                4, Material.LIGHT_BLUE_STAINED_GLASS_PANE,
                5, Material.CYAN_STAINED_GLASS_PANE,
                6, Material.BLUE_STAINED_GLASS_PANE,
                7, Material.PURPLE_STAINED_GLASS_PANE,
                8, Material.MAGENTA_STAINED_GLASS_PANE
        );

        for (int i = 0; i < size - 10; i++) {
            int column = i % 9;
            int row = i / 9;
            this.buttons.add(new Button(i) {

                @Override
                public ItemStack getItem() {
                    return createItemStack(materialMap.get(column), ChatColor.GOLD + loreMap.get(row));
                }


                /**
                 *  When an armour slot is clicked, if it is clicked with a corresponding piece of armour, update the tile and delete the item from cursor
                 * @param player The player clicking
                 */
                @Override
                public void onClick(Player player) {
                    ItemStack itemOnCursor = player.getItemOnCursor();
                    if (itemOnCursor.getType() != Material.AIR) {
                        return;
                    }

                    // Name of item on cursor
                    String itemName = itemOnCursor.getType().toString().toLowerCase();
                    // Name of armour slot i.e. helmet, chestplate, leggings, boots.
                    String slotName = getItem().getItemMeta().getDisplayName().toLowerCase().substring(2);

                    /* Check that cursor item is being clicked on a slot that matches.
                    *  e.g. diamond_leggings contains leggings.
                     */
                    if (itemName.contains(slotName)) {
                        Inventory inventoryInstance = player.getInventory();

                        // If item in slot is a block (i.e. the background pane) then place armor and delete pane item
                        if (inventoryInstance.getItem(getSlot()).getType().isBlock()) {
                            inventoryInstance.setItem(getSlot(), itemOnCursor);
                            player.setItemOnCursor(null);
                            // If item in slot is not block, must be armour piece, then swap cursor and slot
                        } else {
                            ItemStack itemToReturn = inventoryInstance.getItem(getSlot());
                            inventoryInstance.setItem(getSlot(), itemOnCursor);
                            player.setItemOnCursor(itemToReturn);
                        }
                    }
                }
            });
        }
    }

    /**
     * Creates gray pane buttons for the wardrobe GUI.
     */
    private void createGrayPaneButtons() {
        for (int i = size - 1; i > size - 10; i--) {
            this.buttons.add(new Button(i) {
                @Override
                public ItemStack getItem() {
                    return createItemStack(Material.GRAY_STAINED_GLASS_PANE, ChatColor.GOLD + "");
                }

                @Override
                public void onClick(Player player) {
                }
            });
        }
    }

    /**
     * Creates equip and unequip buttons for the wardrobe GUI.
     *
     * @param inventory The inventory to add the buttons to.
     */
    private void createEquipUnequipButtons(Inventory inventory) {
        for (int i = size - 10; i > size - 19; i--) {
            int slot = i;
            buttons.add(new Button(i) {
                private ItemStack item;

                @Override
                public ItemStack getItem() {
                    item = new ItemStack(Material.GRAY_DYE);
                    final ItemMeta meta = item.getItemMeta();
                    meta.setDisplayName(ChatColor.RED + "Equip Loadout");
                    item.setItemMeta(meta);
                    return item;
                }

                @Override
                public void onClick(Player player) {
                    toggleEquipUnequip(player, inventory, slot);
                }
            });
        }
    }

    /**
     * Toggles between equip and unequip states for the loadout.
     *
     * @param player    The player to toggle the loadout for.
     * @param inventory The inventory to update.
     * @param slot      The slot of the item to toggle.
     */
    private void toggleEquipUnequip(Player player, Inventory inventory, int slot) {
        final ItemStack equip = createItemStack(Material.GRAY_DYE, ChatColor.RED + "Equip Loadout");
        final ItemStack unequip = createItemStack(Material.LIME_DYE, ChatColor.GREEN + "Unequip Loadout");

        ItemMeta currentMeta = inventory.getItem(slot).getItemMeta();

        if (currentMeta.getDisplayName().equals(ChatColor.RED + "Equip Loadout")) {
            // TODO - LOGIC TO EQUIP LOADOUT
            for (int i = size - 10; i > size - 19; i--) {
                inventory.setItem(i, equip);
            }

            inventory.setItem(slot, unequip);
        } else {
            // TODO - LOGIC TO UNEQUIP LOADOUT
            inventory.setItem(slot, equip);
        }
    }

    /**
     * Creates an ItemStack with specified material and display name.
     *
     * @param material    The material of the item.
     * @param displayName The display name of the item.
     * @return The created ItemStack.
     */
    private ItemStack createItemStack(Material material, String displayName) {
        final ItemStack item = new ItemStack(material);
        final ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(displayName);
        item.setItemMeta(meta);
        return item;
    }
}
