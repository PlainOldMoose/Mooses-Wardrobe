package me.plainioldmoose.mooseswardrobe;

import me.plainioldmoose.mooseswardrobe.Command.WardrobeCommand;
import me.plainioldmoose.mooseswardrobe.Data.WardrobeData;
import me.plainioldmoose.mooseswardrobe.GUI.WardrobeListener;
import org.bukkit.plugin.java.JavaPlugin;

public final class Wardrobe extends JavaPlugin {

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(new WardrobeListener(), this);
        getCommand("wardrobe").setExecutor(new WardrobeCommand());
        WardrobeData.getInstance().loadInventories();
    }

    @Override
    public void onDisable() {
        WardrobeData.getInstance().saveInventories();
    }

    public static Wardrobe getInstance() {
        return getPlugin(Wardrobe.class);
    }

}
