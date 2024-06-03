package me.plainioldmoose.mooseswardrobe;

import me.plainioldmoose.mooseswardrobe.Command.WardrobeCommand;
import me.plainioldmoose.mooseswardrobe.GUI.WardrobeListener;
import org.bukkit.plugin.java.JavaPlugin;

public final class Wardrobe extends JavaPlugin {

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(new WardrobeListener(), this);
        getCommand("wardrobe").setExecutor(new WardrobeCommand());
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public static Wardrobe getInstance() {
        return getPlugin(Wardrobe.class);
    }

}
