package com.yourname.speedrunnerswap;

import com.yourname.speedrunnerswap.commands.SwapCommand;
import com.yourname.speedrunnerswap.config.ConfigManager;
import com.yourname.speedrunnerswap.effects.EffectsManager;
import com.yourname.speedrunnerswap.game.GameManager;
import com.yourname.speedrunnerswap.game.GameStateManager;
import com.yourname.speedrunnerswap.game.SerializableGameState;
import com.yourname.speedrunnerswap.gui.GuiManager;
import com.yourname.speedrunnerswap.integration.placeholderapi.SpeedrunnerSwapExpansion;
import com.yourname.speedrunnerswap.listeners.EventListeners;
import com.yourname.speedrunnerswap.tracking.TrackerManager;
import com.yourname.speedrunnerswap.voice.VoiceChatIntegration;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class SpeedrunnerSwap extends JavaPlugin {

    private static SpeedrunnerSwap instance;
    private ConfigManager configManager;
    private GameManager gameManager;
    private GuiManager guiManager;
    private TrackerManager trackerManager;
    private VoiceChatIntegration voiceChatIntegration;
    private GameStateManager gameStateManager;
    private EffectsManager effectsManager;

    @Override
    public void onEnable() {
        instance = this;

        // Initialize managers
        this.configManager = new ConfigManager(this);
        this.gameStateManager = new GameStateManager(this);
        this.effectsManager = new EffectsManager(this);
        this.gameManager = new GameManager(this);
        this.guiManager = new GuiManager(this);
        this.trackerManager = new TrackerManager(this);
        this.voiceChatIntegration = new VoiceChatIntegration(this);

        // Register commands
        getCommand("swap").setExecutor(new SwapCommand(this));

        // Register event listeners
        Bukkit.getPluginManager().registerEvents(new EventListeners(this), this);

        // Load game state if it exists
        if (gameStateManager.hasSavedState()) {
            SerializableGameState state = gameStateManager.loadState();
            if (state != null) {
                gameManager.loadGameFromState(state);
            }
        }

        // Hook into PlaceholderAPI
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new SpeedrunnerSwapExpansion(this).register();
            getLogger().info("Successfully hooked into PlaceholderAPI.");
        }

        // Log startup
        getLogger().info("SpeedrunnerSwap v" + getDescription().getVersion() + " has been enabled!");
    }

    @Override
    public void onDisable() {
        // Save game state if a game is running
        gameManager.shutdown();

        // Log shutdown
        getLogger().info("SpeedrunnerSwap has been disabled!");
    }

    public static SpeedrunnerSwap getInstance() {
        return instance;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public GameManager getGameManager() {
        return gameManager;
    }

    public GuiManager getGuiManager() {
        return guiManager;
    }

    public TrackerManager getTrackerManager() {
        return trackerManager;
    }

    public VoiceChatIntegration getVoiceChatIntegration() {
        return voiceChatIntegration;
    }

    public GameStateManager getGameStateManager() {
        return gameStateManager;
    }

    public EffectsManager getEffectsManager() {
        return effectsManager;
    }
}