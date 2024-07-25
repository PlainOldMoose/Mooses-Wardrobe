package me.plainioldmoose.mooseswardrobe.GUI;

import me.plainioldmoose.mooseswardrobe.Data.WardrobeData;
import me.plainioldmoose.mooseswardrobe.Wardrobe;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

// TODO - Item refunds when resetting wardrobe
// TODO - fix /wardrobe <name> bug opening for the player named instead of the command sender

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
     * Displays the wardrobe GUI to a player.
     *
     * @param player The player to display the GUI to.
     */
    public void displayTo(Player player, boolean self, @Nullable UUID playerID) {
        buttons.clear();
        // Create the inventory with the specified size and title
        final Inventory inventory = Bukkit.createInventory(player, this.size, ChatColor.translateAlternateColorCodes('&', this.title));

        // Create and add buttons to the inventory
        createBackgroundTiles();
        createGrayPaneButtons();
        createEquipUnequipButtons(inventory);
        createCloseButton();

        createArmourSlots(inventory, player);


        // Render all buttons in the inventory
        for (final Button button : this.buttons) {
            inventory.setItem(button.getSlot(), button.getItem());
        }

        if (self) {
            // Load and set saved inventory contents if they exist
            UUID playerUUID = player.getUniqueId();
            Map<UUID, ItemStack[]> savedInventories = WardrobeData.getInstance().getSavedInventories();
            if (savedInventories.containsKey(playerUUID)) {
                inventory.setContents(savedInventories.get(playerUUID));
            }
            updateButtonsOnPermissions(player, inventory);
            // Check for item duplication issues
            dupeFailsafe(player, inventory);

            // Ensure no leftover metadata interferes
            if (player.hasMetadata("WardrobeGUI")) {
                player.closeInventory();
            }

            // Set metadata to indicate the wardrobe GUI is open and display it
            player.setMetadata("WardrobeGUI", new FixedMetadataValue(Wardrobe.getInstance(), this));
        } else {
            // Load and set saved inventory contents if they exist
            Map<UUID, ItemStack[]> savedInventories = WardrobeData.getInstance().getSavedInventories();
            if (savedInventories.containsKey(playerID)) {
                inventory.setContents(savedInventories.get(playerID));
            }
            player.setMetadata("CheckingWardrobe", new FixedMetadataValue(Wardrobe.getInstance(), this));
        }

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
                    int loadoutColumn = this.getSlot() % 9 + 1;
                    if (!(player.hasPermission("wardrobe.use.slot" + loadoutColumn))) {
                        player.sendMessage("§b§lWardrobe §8»§3 You do not have permission this loadout!");
                    }
                }
            });
        }
    }

    private void updateButtonsOnPermissions(Player player, Inventory inventory) {
        // Define ItemStacks for reusable items
        final ItemStack empty = createItemStack(Material.RED_DYE, ChatColor.GOLD + "§lStore a loadout first!");
        final ItemStack noPermissionPane = createItemStack(Material.GRAY_STAINED_GLASS_PANE, ChatColor.RED + "");
        final ItemStack defaultEquipButton = createItemStack(Material.END_CRYSTAL, ChatColor.GOLD + "§cYou don't have permission for this slot!");

        // Iterate through each column (0 to 8)
        for (int i = 0; i < 9; i++) {
            boolean hasPermission = player.hasPermission("wardrobe.use.slot" + (i + 1));
            boolean hasArmour = columnHasArmour(inventory, i);

            // Handle case where player does not have permission for current column
            if (!hasPermission) {
                handleNoPermission(player, inventory, i, hasArmour, noPermissionPane, defaultEquipButton);
            } else {
                // Handle case where player has permission for current column
                handleHasPermission(inventory, i, hasArmour, empty);
            }
        }
    }

    // Handles setting items when player does not have permission for a column
    private void handleNoPermission(Player player, Inventory inventory, int column, boolean hasArmour, ItemStack noPermissionPane, ItemStack defaultEquipButton) {
        // Check if the column has armour items
        if (hasArmour) {
            // Iterate through the rows of the column (0 to 3)
            for (int c = column; c < 36; c += 9) {
                ItemStack item = inventory.getItem(c);
                // Check if item exists and is a block type
                if (item != null && item.getType().isBlock()) {
                    inventory.setItem(c, noPermissionPane); // Replace with no permission pane
                } else {
                    // Refund item to player if inventory is full, then replace with no permission pane
                    if (item != null) {
                        refundItem(player, item);
                    }
                    inventory.setItem(c, noPermissionPane);
                }
            }
        } else {
            // Set no permission pane for all slots in the column
            for (int j = 0; j < 4; j++) {
                inventory.setItem(column + j * 9, noPermissionPane);
            }
        }
        // Set default equip button for the current column
        inventory.setItem(column + 36, defaultEquipButton);
    }

    // Handles setting items when player has permission for a column
    private void handleHasPermission(Inventory inventory, int column, boolean hasArmour, ItemStack empty) {
        // Check if the column has armour items
        if (!hasArmour) {
            // Set default panes for all slots in the column
            for (int j = 0; j < 4; j++) {
                inventory.setItem(column + j * 9, createDefaultPane(column + j * 9));
            }
            // Set empty pane for equip button slot
            inventory.setItem(column + 36, empty);
        } else {
            // Check each row in the column
            for (int x = 0; x < 36; x += 9) {
                ItemStack item = inventory.getItem(column + x);
                // If item is a block type, replace with default pane
                if (item != null && item.getType().isBlock()) {
                    inventory.setItem(column + x, createDefaultPane(column + x));
                }
            }
        }
    }

    // Refunds item to player or drops it if inventory is full
    private void refundItem(Player player, ItemStack item) {
        if (!player.getInventory().addItem(item).isEmpty()) {
            player.getWorld().dropItem(player.getLocation(), item);
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
                            inventory.setItem(correspondingSlotIndex, createDefaultPane(armorSlots[j])); // Sets correct index to blank pane
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
    private void createArmourSlots(Inventory inventory, Player player) {
        List<Integer> allowedSlots = new ArrayList<>();

        // Gather all allowed slots based on player permissions
        for (int i = 1; i <= 9; i++) {
            if (player.hasPermission("wardrobe.use.slot" + i)) {
                allowedSlots.add(i - 1); // Adjusting to zero-based index for inventory slots
            }
        }

        // Create slots based on the permissions
        for (int i : allowedSlots) {
            for (int j = 0; j < 35; j += 9) {
                int column = (i + j) % 9;
                int row = (i + j) / 9;
                this.buttons.add(new Button(i + j) {
                    @Override
                    public ItemStack getItem() {
                        return createDefaultPane(this.getSlot());
                    }

                    @Override
                    public void onClick(Player player) {
                        // Get necessary items and inventory
                        ItemStack itemOnCursor = player.getItemOnCursor();
                        Inventory inventory = player.getOpenInventory().getTopInventory();
                        int slot = this.getSlot();
                        ItemStack currentSlotItem = inventory.getItem(slot);

                        // Check if editing an active loadout
                        if (isEditingActiveLoadout(inventory, slot)) {
                            player.sendMessage("§b§lWardrobe §8»§3 You cannot edit active loadout!");
                            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                            return;
                        }

                        // Handle removing items from the GUI
                        if (itemOnCursor.getType().isAir() && currentSlotItem.getType().getEquipmentSlot() != EquipmentSlot.HAND) {
                            handleItemRemoval(player, inventory, slot);
                        }

                        // Handle inserting items into the GUI
                        handleItemInsertion(player, inventory, itemOnCursor, currentSlotItem, slot);
                    }

                    private boolean isEditingActiveLoadout(Inventory inventory, int slot) {
                        int offset = slot % 9;
                        return inventory.getItem(36 + offset) != null && inventory.getItem(36 + offset).getType() == Material.LIME_DYE && slot < 36;
                    }

                    private void handleItemRemoval(Player player, Inventory inventory, int slot) {
                        int offset = slot % 9;
                        ItemStack defaultPane = createDefaultPane(slot);
                        ItemStack currentSlotItem = inventory.getItem(slot);

                        player.setItemOnCursor(currentSlotItem);
                        inventory.setItem(slot, defaultPane);

                        if (!columnHasArmour(inventory, slot)) {
                            ItemStack emptyLoadoutButton = createItemStack(Material.RED_DYE, ChatColor.GOLD + "§lStore a loadout first!");
                            inventory.setItem(36 + offset, emptyLoadoutButton);
                        }
                    }

                    private void handleItemInsertion(Player player, Inventory inventory, ItemStack itemOnCursor, ItemStack currentSlotItem, int slot) {
                        String itemName = itemOnCursor.getType().toString().toLowerCase();
                        String buttonName = this.getItem().getItemMeta().getDisplayName().toLowerCase().substring(4);

                        if (!itemOnCursor.getType().isAir() && itemName.contains(buttonName)) {
                            if (!(currentSlotItem.getType().isBlock())) {
                                ItemStack itemToReturn = currentSlotItem;
                                inventory.setItem(slot, itemOnCursor);
                                player.setItemOnCursor(itemToReturn);
                            } else {
                                inventory.setItem(slot, itemOnCursor);
                                player.setItemOnCursor(null);
                            }
                            int offset = slot % 9;
                            ItemStack equipLoadoutButton = createItemStack(Material.GRAY_DYE, ChatColor.RED + "§lEquip Loadout");
                            inventory.setItem(36 + offset, equipLoadoutButton);
                        }
                    }
                });
            }
        }
        for (final Button button : this.buttons) {
            inventory.setItem(button.getSlot(), button.getItem());
        }
    }

    /**
     * Creates a default pane item for a given slot.
     *
     * @param slot The slot to create the pane for.
     * @return The created ItemStack.
     */
    private ItemStack createDefaultPane(int slot) {
        Map<Integer, String> loreMap = Map.of(0, "§lHelmet", 1, "§lChestplate", 2, "§lLeggings", 3, "§lBoots");

        Map<Integer, Material> materialMap = Map.of(0, Material.ORANGE_STAINED_GLASS_PANE, 1, Material.YELLOW_STAINED_GLASS_PANE, 2, Material.LIME_STAINED_GLASS_PANE, 3, Material.GREEN_STAINED_GLASS_PANE, 4, Material.LIGHT_BLUE_STAINED_GLASS_PANE, 5, Material.CYAN_STAINED_GLASS_PANE, 6, Material.BLUE_STAINED_GLASS_PANE, 7, Material.PURPLE_STAINED_GLASS_PANE, 8, Material.MAGENTA_STAINED_GLASS_PANE);

        int column = slot % 9; // assuming column is derived from slot position
        int row = slot / 9;
        return createItemStack(materialMap.get(column), ChatColor.GOLD + loreMap.get(row));
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
                    int loadoutColumn = this.getSlot() % 9 + 1;
                    if (player.hasPermission("wardrobe.use.slot" + loadoutColumn)) {
                        if (!columnHasArmour(inventory, slot)) {
                            player.sendMessage("§b§lWardrobe §8»§3 You cannot select empty loadout!");
                            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
                            return;
                        } else {
                            toggleEquipUnequip(player, inventory, slot);
                            return;
                        }
                    }
                    return;
                }
            });
        }
    }

    /**
     * Checks if a given column in the inventory contains armor.
     *
     * @param inventory The inventory to check.
     * @param slot      The slot to check.
     * @return True if the column contains armor, false otherwise.
     */
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
     * Determines if a refund of the currently equipped armor is required by checking for the presence of a "LIME_DYE" item
     * in the row containing the specified slot. If such an item is found, no refund is needed.
     *
     * @param inventory The inventory to check for the presence of the "LIME_DYE" item.
     * @param slot      The slot in the inventory whose row is being checked.
     * @return true if a refund is required, false otherwise.
     */
    private boolean equipmentRefundRequired(Inventory inventory, int slot) {
        int row = slot / 9;
        for (int x = row * 9; x < row * 9 + 9; x++) {
            if (inventory.getItem(x).getType() == Material.LIME_DYE) {
                return false;
            }
        }
        return true;
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

        ItemStack currentItem = inventory.getItem(slot);
        if (currentItem == null || !currentItem.hasItemMeta()) {
            return;
        }

        ItemMeta currentMeta = currentItem.getItemMeta();
        if (currentMeta.getDisplayName().equals(ChatColor.RED + "§lEquip Loadout")) {
            if (equipmentRefundRequired(inventory, slot)) {
                refundEquipment(player);
            }

            updateLoadoutButtons(player, inventory, slot, empty, equip);
            equipArmor(player, inventory, slot);
        } else {
            player.getEquipment().setArmorContents(new ItemStack[4]);
            inventory.setItem(slot, equip);
        }

    }

    /**
     * Refunds the player's currently equipped armor by attempting to place it back into their inventory.
     * If the inventory is full, the armor pieces are dropped at the player's location.
     *
     * @param player The player whose equipment is being refunded.
     */
    private void refundEquipment(Player player) {
        for (ItemStack itemToReturn : player.getEquipment().getArmorContents()) {
            if (itemToReturn != null) {
                if (!player.getInventory().addItem(itemToReturn).isEmpty()) {
                    player.getWorld().dropItem(player.getLocation(), itemToReturn);
                }
            }
        }
        player.getEquipment().setArmorContents(new ItemStack[4]);
    }

    /**
     * Updates the loadout buttons in the inventory based on whether each column has armor.
     *
     * @param player    The player whose loadout buttons are being updated.
     * @param inventory The inventory containing the loadout buttons.
     * @param slot      The slot in the inventory that indicates the active loadout.
     * @param empty     The ItemStack representing an empty loadout button.
     * @param equip     The ItemStack representing an equip loadout button.
     */
    private void updateLoadoutButtons(Player player, Inventory inventory, int slot, ItemStack empty, ItemStack equip) {
        for (int i = inventory.getSize() - 18; i < inventory.getSize() - 9; i++) {
            if (columnHasArmour(inventory, i) && player.hasPermission("wardrobe.use.slot" + (i % 9 + 1))) {
                inventory.setItem(i, equip);
            } else if (!player.hasPermission("wardrobe.use.slot" + (i % 9 + 1))) {
                inventory.setItem(i, createItemStack(Material.END_CRYSTAL, ChatColor.GOLD + "§cYou don't have permission for this slot!"));
            } else {
                inventory.setItem(i, empty);
            }
        }
    }

    /**
     * Equips the armor from the specified slots in the inventory to the player's armor slots.
     *
     * @param player    The player who is equipping the armor.
     * @param inventory The inventory from which the armor is being equipped.
     * @param slot      The slot in the inventory that indicates the active loadout.
     */

    private void equipArmor(Player player, Inventory inventory, int slot) {
        int[] armorSlots = {slot - 9, slot - 2 * 9, slot - 3 * 9, slot - 4 * 9};
        ItemStack[] armor = new ItemStack[4];

        for (int i = 0; i < armorSlots.length; i++) {
            ItemStack item = inventory.getItem(armorSlots[i]);
            armor[i] = (item != null && !item.getType().isBlock()) ? item : new ItemStack(Material.AIR);
        }

        player.getEquipment().setArmorContents(armor);
        inventory.setItem(slot, createItemStack(Material.LIME_DYE, ChatColor.GREEN + "§lUnequip Loadout"));
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