package me.plainioldmoose.mooseswardrobe.Data;

import me.plainioldmoose.mooseswardrobe.Wardrobe;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Singleton class for managing the saved inventories of players.
 * Handles loading and saving inventory data to a YAML file.
 */
public class WardrobeData {
    private final static WardrobeData instance = new WardrobeData();

    private File dataFile;
    private FileConfiguration dataConfig;
    private final Map<UUID, ItemStack[]> savedInventories = new HashMap<>();

    /**
     * Private constructor to enforce singleton pattern.
     * Loads the inventories from the data file on instantiation.
     */
    private WardrobeData() {
        loadInventories();
    }

    /**
     * Saves the current state of inventories to the data file.
     */
    public void saveInventories() {
        for (Map.Entry<UUID, ItemStack[]> wardrobe : savedInventories.entrySet()) {
            dataConfig.set(wardrobe.getKey().toString(), wardrobe.getValue());
        }

        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void reset(Player player) {
        player.sendMessage("before : " + savedInventories.toString());
        player.sendMessage("Resetting " + player.getName() + "'s wardrobe!");

        // Remove the player's data from the savedInventories map
        savedInventories.remove(player.getUniqueId());

        // Remove the player's data from the dataConfig
        dataConfig.set(player.getUniqueId().toString(), null);

        // Save the updated dataConfig to the dataFile
        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        saveInventories();
        player.sendMessage("is now : " + savedInventories.toString());
    }

    /**
     * Loads the inventories from the data file into the savedInventories map.
     * If the data file does not exist, it is created.
     */
    @SuppressWarnings("unchecked")
    public void loadInventories() {
        dataFile = new File(Wardrobe.getInstance().getDataFolder(), "data.yml");

        if (!dataFile.exists()) {
            Wardrobe.getInstance().saveResource("data.yml", false);
        }

        dataConfig = new YamlConfiguration();
        dataConfig.options().parseComments(true);

        try {
            dataConfig.load(dataFile);
        } catch (Exception e) {
            e.printStackTrace();
        }

        for (String key : dataConfig.getKeys(false)) {
            UUID playerUUID = UUID.fromString(key);
            ItemStack[] items = ((List<ItemStack>) dataConfig.get(key)).toArray(new ItemStack[0]);
            savedInventories.put(playerUUID, items);
        }
    }

    /**
     * Gets the map of saved inventories.
     *
     * @return The map containing UUIDs and their corresponding saved ItemStacks.
     */
    public Map<UUID, ItemStack[]> getSavedInventories() {
        return savedInventories;
    }

    /**
     * Gets the single instance of the WardrobeData class.
     *
     * @return The singleton instance of WardrobeData.
     */
    public static WardrobeData getInstance() {
        return instance;
    }
}
