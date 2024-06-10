package me.plainioldmoose.mooseswardrobe.GUI;

import me.plainioldmoose.mooseswardrobe.Wardrobe;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.*;
import java.util.stream.Collectors;

// TODO - Fix bug where equipping loadout a containing the same items as the player equipment (not stored in wardrobe) does not refund correct item
// TODO - Implement live updating permissions / wardrobe slots

/**
 * The WardrobeGUI class represents a custom inventory GUI for the Wardrobe plugin.
 */
public class WardrobeGUI {
    private final List<Button> buttons = new ArrayList<>();
    private int size = 9 * 6; // Default size of the inventory
    private String title = "              Wardrobe"; // Default title of the inventory

    /**
     * Returns the list of buttons in the wardrobe GUI.
     *
     * @return A list of buttons.
     */
    public List<Button> getButtons() {
        return buttons;
    }

    /**
     * Adds a button to the wardrobe GUI.
     *
     * @param button The button to add.
     */
    protected void addButton(Button button) {
        this.buttons.add(button);
    }

    /**
     * Sets the size of the wardrobe GUI.
     *
     * @param size The size to set.
     */
    protected void setSize(int size) {
        this.size = size;
    }

    /**
     * Sets the title of the wardrobe GUI.
     *
     * @param title The title to set.
     */
    protected void setTitle(String title) {
        this.title = title;
    }

    /**
     * Displays the wardrobe GUI to a player.
     *
     * @param player The player to display the GUI to.
     */
    public void displayTo(Player player) {
        // Create the inventory with the specified size and title
        final Inventory inventory = Bukkit.createInventory(player, this.size, ChatColor.translateAlternateColorCodes('&', this.title));

        // Create and add buttons to the inventory
        createBackgroundTiles();
        createGrayPaneButtons();
        createArmourSlots(player);
        createEquipUnequipButtons(inventory);
        createCloseButton();

        // Render all buttons in the inventory
        for (final Button button : this.buttons) {
            inventory.setItem(button.getSlot(), button.getItem());
        }

        // Ensure no leftover metadata interferes
        if (player.hasMetadata("WardrobeGUI")) {
            player.closeInventory();
        }

        // Load and set saved inventory contents if they exist
//        UUID playerUUID = player.getUniqueId();
//        Map<UUID, ItemStack[]> savedInventories = WardrobeData.getInstance().getSavedInventories();
//        if (savedInventories.containsKey(playerUUID)) {
//            inventory.setContents(savedInventories.get(playerUUID));
//        }

        // Check for item duplication issues
        dupeFailsafe(player, inventory);


        // Set metadata to indicate the wardrobe GUI is open and display it
        player.setMetadata("WardrobeGUI", new FixedMetadataValue(Wardrobe.getInstance(), this));
        player.openInventory(inventory);
    }

    private void createBackgroundTiles() {
        for (int i = 0; i < 54; i++) {
            this.buttons.add(new Button(i) {
                @Override
                public ItemStack getItem() {
                    return createItemStack(Material.GRAY_STAINED_GLASS_PANE, ChatColor.GOLD + "");
                }

                @Override
                public void onClick(Player player) {
                    // Gray pane buttons have no functionality
                }
            });
        }
    }


    /**
     * Prevents item duplication by ensuring that armor pieces are properly handled.
     *
     * @param player    The player to check.
     * @param inventory The inventory to check for dupes.
     */
    private void dupeFailsafe(Player player, Inventory inventory) {
        ItemStack[] contents = inventory.getContents();
        // Check each item until the active loadout button is found
        for (int i = 0; i < contents.length; i++) {
            ItemStack item = contents[i];
            if (item != null && item.getType() == Material.LIME_DYE) {
                // once found, find armour sets using mod 9 offset
                int[] armorSlots = {i - 9, i - 18, i - 27, i - 36};
                ItemStack[] equippedArmorList = player.getEquipment().getArmorContents();
                // For each piece of armour saved, check if it matches what the player is wearing
                for (int j = 0; j < armorSlots.length; j++) {
                    ItemStack equippedArmour = equippedArmorList[j];
                    ItemStack correspondingSlot = contents[armorSlots[j]];
                    int correspondingSlotIndex = armorSlots[j];
                    if ((equippedArmour == null || !(equippedArmour.equals(correspondingSlot)))) { // If equipped piece is not null and does not equal
                        if (equippedArmour == null) { // If not armour equipped in this slot
                            inventory.setItem(correspondingSlotIndex, buttons.get(armorSlots[j] + 9).getItem()); // Sets correct index to blank pane
                        } else { // Else slot has armour
                            inventory.setItem(correspondingSlotIndex, equippedArmour); // Sets correct index to the armour found in the slot
                        }

                        if (!columnHasArmour(inventory, i)) {
                            ItemStack defaultEquipButton = createItemStack(Material.RED_DYE, ChatColor.GOLD + "§lStore a loadout first!");
                            inventory.setItem(i, defaultEquipButton);
                        } else {
                            ItemStack unequip = createItemStack(Material.LIME_DYE, ChatColor.GREEN + "§lUnequip Loadout");
                            inventory.setItem(i, unequip);
                        }
                    }
                }
            }
        }
    }

    /**
     * Creates a button to close the wardrobe GUI.
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
     * Creates the armor slot buttons in the wardrobe GUI.
     */
    private void createArmourSlots(Player player) {
        Map<Integer, String> loreMap = Map.of(0, "§lHelmet", 1, "§lChestplate", 2, "§lLeggings", 3, "§lBoots");

        Map<Integer, Material> materialMap = Map.of(0, Material.ORANGE_STAINED_GLASS_PANE, 1, Material.YELLOW_STAINED_GLASS_PANE, 2, Material.LIME_STAINED_GLASS_PANE, 3, Material.GREEN_STAINED_GLASS_PANE, 4, Material.LIGHT_BLUE_STAINED_GLASS_PANE, 5, Material.CYAN_STAINED_GLASS_PANE, 6, Material.BLUE_STAINED_GLASS_PANE, 7, Material.PURPLE_STAINED_GLASS_PANE, 8, Material.MAGENTA_STAINED_GLASS_PANE);

        int endSlot = 5;
        if (player.hasPermission("wardrobe.use.slot1")) {
            endSlot = 2;
        } else if (player.hasPermission("wardrobe.use.slot2")) {
            endSlot = 3;
        }

        for (int i = 0; i < endSlot; i++) {
            for (int j = 0; j < 35; j += 9) {
                int column = (i + j) % 9;
                int row = (i + j) / 9;
                this.buttons.add(new Button(i + j) {
                    @Override
                    public ItemStack getItem() {
                        return createItemStack(materialMap.get(column), ChatColor.GOLD + "§l" + loreMap.get(row));
                    }

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

                        // Disable removing while active
                        int offset = this.getSlot() % 9; // Find column which is has been clicked
                        if (inventory.getItem(36 + offset) != null && inventory.getItem(36 + offset).getType() == Material.LIME_DYE && this.getSlot() < 36) { // if the equip button for this column is active, and the slot clicked is one of the armour slots
                            player.sendMessage("§f[§c§lWardrobe§f]§c You cannot edit active loadout!");
                            return;
                        }

                        // Handle removing items from the GUI
                        if (itemOnCursor.getType().isAir() && currentSlotItem.getType().getEquipmentSlot() != EquipmentSlot.HAND) {
                            // Calculate default pane for this slot
                            ItemStack defaultPane = createItemStack(materialMap.get(column), ChatColor.GOLD + loreMap.get(row));
                            // Set cursor to current slot, then set slot to default background pane
                            player.setItemOnCursor(currentSlotItem);
                            inventory.setItem(this.getSlot(), defaultPane);

                            // If last piece removed, set columns equip button to empty
                            if (!columnHasArmour(inventory, this.getSlot())) {
                                ItemStack emptyLoadoutButton = createItemStack(Material.RED_DYE, ChatColor.GOLD + "§lStore a loadout first!");
                                inventory.setItem(36 + offset, emptyLoadoutButton);
                                return;
                            }
                        }


                        // Handle inserting items into the GUI
                        String itemName = itemOnCursor.getType().toString().toLowerCase();
                        String buttonName = this.getItem().getItemMeta().getDisplayName().toLowerCase().substring(4);

                        // If cursor has item
                        if (itemOnCursor.getType() != Material.AIR) {
                            // If item matches slot
                            if (itemName.contains(buttonName)) {
                                if (!(currentSlotItem.getType().isBlock())) { // If existing slot is not a pane
                                    // Place cursor item in slot and set cursor to original slot item
                                    ItemStack itemToReturn = currentSlotItem;
                                    inventory.setItem(this.getSlot(), itemOnCursor);
                                    player.setItemOnCursor(itemToReturn);
                                } else { // Else if it is pane, replace pane with item
                                    inventory.setItem(this.getSlot(), itemOnCursor);
                                    player.setItemOnCursor(null);
                                }
                                ItemStack equipLoadoutButton = createItemStack(Material.GRAY_DYE, ChatColor.RED + "§lEquip Loadout");
                                inventory.setItem(36 + offset, equipLoadoutButton);
                            }
                        }
                    }
                });
            }
        }
    }

    /**
     * Creates the gray pane buttons for the bottom row of the wardrobe GUI.
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
                    // Gray pane buttons have no functionality
                }
            });
        }
    }

    /**
     * Creates the equip and unequip buttons for the wardrobe GUI.
     *
     * @param inventory The inventory to add the buttons to.
     */
    private void createEquipUnequipButtons(Inventory inventory) {
        for (int i = size - 18; i < size - 9; i++) {
            int slot = i;
            buttons.add(new Button(i) {
                @Override
                public ItemStack getItem() {
                    return createItemStack(Material.RED_DYE, ChatColor.GOLD + "§lStore a loadout first!");
                }

                @Override
                public void onClick(Player player) {
                    if (!columnHasArmour(inventory, slot)) {
                        player.sendMessage("§f[§c§lWardrobe§f]§c You cannot select empty loadout!");
                    } else {
                        toggleEquipUnequip(player, inventory, slot);
                    }
                }
            });
        }
    }

    private boolean columnHasArmour(Inventory inventory, int slot) {
        int column = slot % 9;
        int[] offsets = {0, 9, 18, 27};
        for (int offset : offsets) {
            ItemStack itemTypeInSlot = inventory.getItem(column + offset);
            if (itemTypeInSlot != null && !itemTypeInSlot.getType().isBlock()) {
                return true;
            }
        }
        return false;
    }


    /**
     * Toggles between equip and unequip states for the loadout.
     *
     * @param player    The player to toggle the loadout for.
     * @param inventory The inventory to update.
     * @param slot      The slot of the button clicked.
     */
    private void toggleEquipUnequip(Player player, Inventory inventory, int slot) {
        final ItemStack empty = createItemStack(Material.RED_DYE, ChatColor.GOLD + "§lStore a loadout first!");
        final ItemStack equip = createItemStack(Material.GRAY_DYE, ChatColor.RED + "§lEquip Loadout");
        final ItemStack unequip = createItemStack(Material.LIME_DYE, ChatColor.GREEN + "§lUnequip Loadout");

        ItemMeta currentMeta = inventory.getItem(slot).getItemMeta();
        if (currentMeta.getDisplayName().equals(ChatColor.RED + "§lEquip Loadout")) {
            // For each button, check if it's column has any armour, if true set button to equip, if false set button to empty
            for (int i = size - 18; i < size - 9; i++) {
                if (columnHasArmour(inventory, i)) {
                    inventory.setItem(i, equip);
                } else {
                    inventory.setItem(i, empty);
                }
            }

            // Handle equipping of armour
            int[] armorSlots = {slot - 9, slot - 18, slot - 27, slot - 36}; // Boots, Leggings, Chestplate, Helmet slots in the inventory
            ItemStack[] armor = new ItemStack[4];

            /**
             * if find item already in wardrobe > don't return
             * if item not in wardrobe > return 1x item
             *
             */

            // Handle item refunds if item is not equipped in wardrobe
            ItemStack[] currentEquipment = player.getEquipment().getArmorContents();
            ItemStack[] currentWardrobe = inventory.getContents();

            // Filter out nulls from currentWardrobe and create a set for fast lookups
            Set<ItemStack> wardrobeSet = Arrays.stream(currentWardrobe).filter(Objects::nonNull).collect(Collectors.toSet());

            for (ItemStack armourPiece : currentEquipment) {
                if (armourPiece != null && !wardrobeSet.contains(armourPiece)) {
                    // Add 1x of the item to the player's inventory
                    ItemStack singleItem = armourPiece.clone();
                    singleItem.setAmount(1);
                    if (!player.getInventory().addItem(singleItem).isEmpty()) {
                        Location playerLocation = player.getLocation();
                        player.getWorld().dropItem(playerLocation, singleItem);
                    }
                }
            }

            for (int i = 0; i < armorSlots.length; i++) {
                ItemStack item = inventory.getItem(armorSlots[i]);
                if (!item.getType().isBlock()) {
                    armor[i] = item;
                } else {
                    armor[i] = new ItemStack(Material.AIR);
                }
            }
            player.getEquipment().setArmorContents(armor);
            inventory.setItem(slot, unequip);
        } else {
            player.getEquipment().setArmorContents(new ItemStack[4]);
            inventory.setItem(slot, equip);
        }
    }

    /**
     * Creates an ItemStack with the specified material and display name.
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
