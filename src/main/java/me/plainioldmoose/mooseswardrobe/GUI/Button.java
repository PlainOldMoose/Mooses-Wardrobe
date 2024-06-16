package me.plainioldmoose.mooseswardrobe.GUI;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * Abstract class representing a button in a GUI.
 * Each button has a specific slot and defines behavior for when it is clicked.
 */
public abstract class Button {

    private final int slot;

    /**
     * Constructor to create a Button at a specified slot.
     *
     * @param slot The slot number where the button will be placed.
     */
    public Button(int slot) {
        this.slot = slot;
    }

    /**
     * Gets the slot number of the button.
     *
     * @return The slot number.
     */
    public final int getSlot() {
        return slot;
    }

    /**
     * Gets the ItemStack representing the button.
     * Must be implemented by subclasses to define the button's appearance.
     *
     * @return The ItemStack representing the button.
     */
    public abstract ItemStack getItem();

    /**
     * Defines the action to be taken when the button is clicked.
     * Must be implemented by subclasses to define specific click behavior.
     *
     * @param player The player who clicked the button.
     */
    public abstract void onClick(Player player);
}