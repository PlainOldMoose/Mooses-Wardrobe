package me.plainioldmoose.mooseswardrobe.GUI;

import me.plainioldmoose.mooseswardrobe.Data.WardrobeData;
import me.plainioldmoose.mooseswardrobe.Wardrobe;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * The WardrobeGUI class represents a custom inventory GUI for the Wardrobe plugin.
 */
public class WardrobeGUI {
    private final List<Button> buttons = new ArrayList<>();
    private int size = 9 * 6;
    private String title = "              Wardrobe";

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
        // Create basic inventory object
        final Inventory inventory = Bukkit.createInventory(player, this.size,
                ChatColor.translateAlternateColorCodes('&', this.title));

        // Create relevant buttons within GUI
        createGrayPaneButtons();
        createArmourSlots();
        createEquipUnequipButtons(inventory);
        createCloseButton();
        // TODO - Disable switching to empty slots,

        // Render all buttons with their assigned items and display names
        for (final Button button : this.buttons) {
            inventory.setItem(button.getSlot(), button.getItem());
        }

        // Error-handling to ensure no leftover metadata can interfere
        if (player.hasMetadata("WardrobeGUI")) {
            player.closeInventory();
        }

        // Load saved wardrobes from players after GUI is rendered
        UUID playerUUID = player.getUniqueId();
        Map<UUID, ItemStack[]> savedInventories = WardrobeData.getInstance().getSavedInventories();

        // If player has wardrobe saved, set contents of their GUI to match saved wardrobe
        if (savedInventories.containsKey(playerUUID)) {
            inventory.setContents(savedInventories.get(playerUUID));
        }

        dupeFailsafe(player, inventory);

        // Set metadata to infer menu is open and open menu
        player.setMetadata("WardrobeGUI", new FixedMetadataValue(Wardrobe.getInstance(), this));
        player.openInventory(inventory);
    }

    /**
     * This method is intended to remove duping from the plugin. If the player has an active slot,
     * and a piece of armour is removed from this slot then the plugin must also delete it from the wardrobe.
     *
     * @param inventory The inventory to check for dupes.
     */
    private void dupeFailsafe(Player player, Inventory inventory) {
        ItemStack[] contents = inventory.getContents();
        for (int i = 0; i < contents.length; i++) {
            Material buttonItem = contents[i].getType();

            if (buttonItem == Material.LIME_DYE) {
                int[] armourSlots = {i - 9, i - 18, i - 27, i - 36};
                ItemStack[] equippedArmour = player.getEquipment().getArmorContents();
                for (int j = 0; j < armourSlots.length; j++) {
                    if (equippedArmour[j] == null && !contents[armourSlots[j]].getType().isBlock()) {
                        player.sendMessage(buttons.get(j).getItem().toString());
                        inventory.setItem(armourSlots[j], buttons.get(armourSlots[j] + 9).getItem());
                    }
                }
            }
        }
    }

    /**
     * Creates a basic button to close the GUI.
     */
    private void createCloseButton() {
        final Button closeButton = new Button(this.size - 5) {
            @Override
            public ItemStack getItem() {
                return createItemStack(Material.BARRIER, ChatColor.GOLD + "§lClose");
            }

            @Override
            public void onClick(Player player) {
                Bukkit.getScheduler().runTaskLater(Wardrobe.getInstance(), player::closeInventory, 1);
            }
        };
        this.buttons.add(closeButton);
    }

    /**
     * Creates background tiles and provides functionality for clicking on a background tile with a piece of armour.
     */
    private void createArmourSlots() {
        Map<Integer, String> loreMap = Map.of(
                0, "§lHelmet",
                1, "§lChestplate",
                2, "§lLeggings",
                3, "§lBoots"
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
                    return createItemStack(materialMap.get(column), ChatColor.GOLD + "§l" + loreMap.get(row));
                }

                /**
                 *  When an armour slot is clicked, if it is clicked with a corresponding piece of armour, update the tile and delete the item from cursor.
                 *
                 * @param player The player clicking
                 */
                @Override
                public void onClick(Player player) {
                    /*This method is quite messy however I don't see a better way of abstracting it, this is the bread and butter of how inserting / removing items into the GUI is handled.
                     *  Each background tile is actually a button which can be clicked under certain criteria to perform certain tasks on the GUI. e.g. clicking an empty slot with a piece of armour will
                     *  update that buttons icon to the item clicked and remove it from the player's cursor.
                     */

                    // Get the item currently on the player's cursor
                    ItemStack itemOnCursor = player.getItemOnCursor();
                    Inventory inventory = player.getOpenInventory().getTopInventory();
                    ItemStack currentSlotItem = inventory.getItem(this.getSlot());

                    // HANDLE REMOVING


                    // DISABLE REMOVING WHILE ACTIVE
                    int offset = this.getSlot() % 9;
                    if (inventory.getItem(36 + offset).getType() == Material.LIME_DYE && this.getSlot() < 36) {
                        player.sendMessage("§f[§c§lWardrobe§f]§c You cannot edit active loadout!");
                        return;
                    }

                    if (itemOnCursor.getType().isAir() && !(currentSlotItem.getType().getEquipmentSlot() == EquipmentSlot.HAND)) {
                        // Calculate default pane for this slot
                        ItemStack defaultPane = createItemStack(materialMap.get(column), ChatColor.GOLD + loreMap.get(row));
                        // Set cursor to current slot, then set slot to default background pane;
                        player.setItemOnCursor(currentSlotItem);
                        inventory.setItem(this.getSlot(), defaultPane);
                    }

                    // HANDLE INSERTING

                    String itemName = itemOnCursor.getType().toString().toLowerCase();
                    String buttonName = this.getItem().getItemMeta().getDisplayName().toLowerCase().substring(4);

                    // If cursor has item
                    if (itemOnCursor.getType() != Material.AIR) {
                        // If item matches slot and slot is a pane
                        if (itemName.contains(buttonName)) {
                            // If existing slot is not a pane
                            if (!(currentSlotItem.getType().isBlock())) {
                                // Place cursor item in slot and set cursor to original slot item
                                ItemStack itemToReturn = currentSlotItem;
                                inventory.setItem(this.getSlot(), itemOnCursor);
                                player.setItemOnCursor(itemToReturn);
                            } else {
                                inventory.setItem(this.getSlot(), itemOnCursor);
                                player.setItemOnCursor(null);
                            }
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


                @Override
                public ItemStack getItem() {
                    ItemStack item = new ItemStack(Material.GRAY_DYE);
                    final ItemMeta meta = item.getItemMeta();
                    meta.setDisplayName(ChatColor.RED + "§lEquip Loadout");
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
        final ItemStack equip = createItemStack(Material.GRAY_DYE, ChatColor.RED + "§lEquip Loadout");
        final ItemStack unequip = createItemStack(Material.LIME_DYE, ChatColor.GREEN + "§lUnequip Loadout");

        ItemMeta currentMeta = inventory.getItem(slot).getItemMeta();

        if (currentMeta.getDisplayName().equals(ChatColor.RED + "§lEquip Loadout")) {
            // Set other buttons to say equip
            for (int i = size - 10; i > size - 19; i--) {
                inventory.setItem(i, equip);
            }

            // Handle equipping
            int[] armorSlots = {slot - 9, slot - 18, slot - 27, slot - 36}; // Boots, Leggings, Chestplate, Helmet slots in the inventory
            ItemStack[] armor = new ItemStack[4];
            for (int i = 0; i < armorSlots.length; i++) {

                ItemStack item = inventory.getItem(armorSlots[i]);
                if (!(item.getType().isBlock())) {
                    armor[i] = inventory.getItem(armorSlots[i]);
                } else {
                    armor[i] = new ItemStack(Material.AIR);
                }
            }
            player.getEquipment().setArmorContents(armor);

            // set slot clicked to say unequip
            inventory.setItem(slot, unequip);
        } else {
            // Handle unequipping
            ItemStack air = new ItemStack(Material.AIR);
            player.getEquipment().setBoots(air);
            player.getEquipment().setLeggings(air);
            player.getEquipment().setChestplate(air);
            player.getEquipment().setHelmet(air);

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
