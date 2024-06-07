package me.plainioldmoose.mooseswardrobe.Data;

import me.plainioldmoose.mooseswardrobe.Wardrobe;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class WardrobeData {
    private final static WardrobeData instance = new WardrobeData();

    private File dataFile;
    private FileConfiguration dataConfig;
    private final Map<UUID, ItemStack[]> savedInventories = new HashMap<UUID, ItemStack[]>();

    public WardrobeData() {
        loadInventories();
    }

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

    public Map<UUID, ItemStack[]> getSavedInventories() {
        return savedInventories;
    }

    public static WardrobeData getInstance() {
        return instance;
    }
}