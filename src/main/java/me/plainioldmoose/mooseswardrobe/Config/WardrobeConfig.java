package me.plainioldmoose.mooseswardrobe.Config;

public class WardrobeConfig {

    private final static WardrobeConfig instance = new WardrobeConfig();

    private WardrobeConfig() {

    }

    public static WardrobeConfig getInstance() {
        return instance;
    }
}
