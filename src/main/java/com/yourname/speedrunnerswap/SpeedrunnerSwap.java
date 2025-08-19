package com.yourname.speedrunnerswap;

import com.yourname.speedrunnerswap.game.GameStateManager;
import com.yourname.speedrunnerswap.game.SerializableGameState;
import com.yourname.speedrunnerswap.integration.placeholderapi.SpeedrunnerSwapExpansion;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

public class SpeedrunnerSwap extends JavaPlugin {
    private SwapManager manager;
    private MuteManager muteManager;
    private GuiHub guiHub;
    private GameStateManager gameStateManager;
    private EffectsManager effectsManager;

    @Override public void onEnable() {
        saveDefaultConfig();
        this.gameStateManager = new GameStateManager(this);
        this.effectsManager = new EffectsManager(this);
        buildManagerFromConfig(); // Builds manager, which might be replaced by loaded state

        // Load game state if it exists
        if (gameStateManager.hasSavedState()) {
            SerializableGameState state = gameStateManager.loadState();
            if (state != null) {
                // Overwrite the default manager with the one loaded from state
                manager.loadState(state);
            }
        }

        getCommand("swap").setExecutor(new SwapCommand(this));
        getServer().getPluginManager().registerEvents(manager, this);
        getServer().getPluginManager().registerEvents(new FreezeGuard(manager), this);
        getServer().getPluginManager().registerEvents(muteManager, this);

        // Hook into PlaceholderAPI
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new SpeedrunnerSwapExpansion(this).register();
            getLogger().info("Successfully hooked into PlaceholderAPI.");
        }

        getLogger().info("SpeedrunnerSwap v" + getDescription().getVersion() + " enabled.");
    }

    @Override public void onDisable() {
        // Save game state if a game is running
        if (manager != null && manager.isRunning()) {
            gameStateManager.saveState(manager);
        } else if (manager != null) {
            // Ensure no state file lingers if game wasn't running
            gameStateManager.saveState(manager);
        }
        HandlerList.unregisterAll(this);
        getLogger().info("SpeedrunnerSwap disabled.");
    }

    public void reloadAndRebuild() {
        reloadConfig();
        if (manager != null && manager.isRunning()) manager.pause();
        HandlerList.unregisterAll(manager);
        if (muteManager != null) HandlerList.unregisterAll(muteManager);
        buildManagerFromConfig();
        getServer().getPluginManager().registerEvents(manager, this);
        getServer().getPluginManager().registerEvents(new FreezeGuard(manager), this);
        getServer().getPluginManager().registerEvents(muteManager, this);
    }

    private void buildManagerFromConfig() {
        var c = getConfig();
        if (muteManager == null) muteManager = new MuteManager(this);
        manager = new SwapManager(this, c, muteManager);
    }

    public SwapManager manager() { return manager; }
    public GameStateManager gameStateManager() { return gameStateManager; }
    public EffectsManager effectsManager() { return effectsManager; }

    public GuiHub guiHub() {
        if (guiHub == null) guiHub = new GuiHub(this);
        return guiHub;
    }
}
