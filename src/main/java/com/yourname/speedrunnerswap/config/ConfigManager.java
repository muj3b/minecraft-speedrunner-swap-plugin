package com.yourname.speedrunnerswap.config;

import com.yourname.speedrunnerswap.SpeedrunnerSwap;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ConfigManager {
    
    private final SpeedrunnerSwap plugin;
    private FileConfiguration config;
    private List<String> runnerNames;
    private List<String> hunterNames;
    private Set<Material> dangerousBlocks;
    private List<String> allowedWorlds;
    
    // Effects
    private Effect swapWarningEffect;
    private Effect swapExecuteEffect;
    private Effect gameStartEffect;
    private Effect gameEndEffect;

    // Chat Isolation
    private boolean blockRunnerChat;
    private boolean blockRunnerPms;
    private String messageBlockReply;

    public ConfigManager(SpeedrunnerSwap plugin) {
        this.plugin = plugin;
        loadConfig();
    }
    
    /**
     * Load or reload the configuration
     */
    public void loadConfig() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        config = plugin.getConfig();
        
        // Load general settings
        allowedWorlds = config.getStringList("allowed-worlds");

        // Load team lists
        runnerNames = config.getStringList("teams.runners");
        hunterNames = config.getStringList("teams.hunters");
        
        // Load dangerous blocks
        dangerousBlocks = new HashSet<>();
        for (String blockName : config.getStringList("safe_swap.dangerous_blocks")) {
            try {
                Material material = Material.valueOf(blockName);
                dangerousBlocks.add(material);
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Invalid material in dangerous_blocks: " + blockName);
            }
        }

        // Load effects
        swapWarningEffect = parseEffect("effects.swap-warning");
        swapExecuteEffect = parseEffect("effects.swap-execute");
        gameStartEffect = parseEffect("effects.game-start");
        gameEndEffect = parseEffect("effects.game-end");

        // Load chat isolation settings
        blockRunnerChat = config.getBoolean("chat-isolation.block-runner-chat", true);
        blockRunnerPms = config.getBoolean("chat-isolation.block-runner-pms", true);
        messageBlockReply = config.getString("chat-isolation.message-block-reply", "&cYou cannot communicate with other runners during the game.");
    }

    private Effect parseEffect(String path) {
        ConfigurationSection section = config.getConfigurationSection(path);
        if (section == null) {
            return new Effect(false, "", 0, 0, "", "");
        }
        boolean enabled = section.getBoolean("enabled", false);
        String sound = section.getString("sound", "");
        float volume = (float) section.getDouble("volume", 1.0);
        float pitch = (float) section.getDouble("pitch", 1.0);
        String title = section.getString("title", "");
        String subtitle = section.getString("subtitle", "");
        return new Effect(enabled, sound, volume, pitch, title, subtitle);
    }
    
    /**
     * Save the configuration
     */
    public void saveConfig() {
        config.set("teams.runners", runnerNames);
        config.set("teams.hunters", hunterNames);
        plugin.saveConfig();
    }

    public void addRunner(String name) {
        if (!runnerNames.contains(name)) {
            runnerNames.add(name);
            hunterNames.remove(name);
            saveConfig();
        }
    }

    public void removeRunner(String name) {
        if (runnerNames.remove(name)) {
            saveConfig();
        }
    }

    public void addHunter(String name) {
        if (!hunterNames.contains(name)) {
            hunterNames.add(name);
            runnerNames.remove(name);
            saveConfig();
        }
    }

    public void removeHunter(String name) {
        if (hunterNames.remove(name)) {
            saveConfig();
        }
    }

    public void setRunners(List<String> names) {
        runnerNames = new ArrayList<>(names);
        hunterNames.removeAll(runnerNames);
        saveConfig();
    }

    public void setHunters(List<String> names) {
        hunterNames = new ArrayList<>(names);
        runnerNames.removeAll(hunterNames);
        saveConfig();
    }
    
    /**
     * Get the list of runner names
     * @return The list of runner names
     */
    public List<String> getRunnerNames() {
        return new ArrayList<>(runnerNames);
    }
    
    /**
     * Get the list of hunter names
     * @return The list of hunter names
     */
    public List<String> getHunterNames() {
        return new ArrayList<>(hunterNames);
    }
    
    /**
     * Check if a player is a runner
     * @param player The player to check
     * @return True if the player is a runner
     */
    public boolean isRunner(Player player) {
        return runnerNames.contains(player.getName());
    }
    
    /**
     * Check if a player is a hunter
     * @param player The player to check
     * @return True if the player is a hunter
     */
    public boolean isHunter(Player player) {
        return hunterNames.contains(player.getName());
    }
    
    /**
     * Get whether the swap system should use randomized intervals
     * @return True if randomized intervals should be used
     */
    public boolean isRandomizeSwap() {
        return config.getBoolean("swap.randomize", true);
    }
    
    /**
     * Get the base swap interval in seconds
     * @return The base swap interval
     */
    public int getSwapInterval() {
        return config.getInt("swap.interval", 60);
    }
    
    /**
     * Get the minimum swap interval in seconds
     * @return The minimum swap interval
     */
    public int getMinSwapInterval() {
        return config.getInt("swap.min_interval", 30);
    }
    
    /**
     * Get the maximum swap interval in seconds
     * @return The maximum swap interval
     */
    public int getMaxSwapInterval() {
        return config.getInt("swap.max_interval", 90);
    }
    
    /**
     * Get the jitter standard deviation in seconds
     * @return The jitter standard deviation
     */
    public double getJitterStdDev() {
        return config.getDouble("swap.jitter.stddev", 15);
    }
    
    /**
     * Get whether to clamp jittered intervals within min/max limits
     * @return True if jittered intervals should be clamped
     */
    public boolean isClampJitter() {
        return config.getBoolean("swap.jitter.clamp", true);
    }
    
    /**
     * Get the grace period after swaps in ticks
     * @return The grace period in ticks
     */
    public int getGracePeriodTicks() {
        return config.getInt("swap.grace_period_ticks", 40);
    }
    
    /**
     * Get whether to pause the game when a runner disconnects
     * @return True if the game should pause on disconnect
     */
    public boolean isPauseOnDisconnect() {
        return config.getBoolean("swap.pause_on_disconnect", true);
    }
    
    /**
     * Get whether safe swaps are enabled
     * @return True if safe swaps are enabled
     */
    public boolean isSafeSwapEnabled() {
        return config.getBoolean("safe_swap.enabled", true);
    }
    
    /**
     * Get the horizontal scan radius for safe swaps
     * @return The horizontal scan radius
     */
    public int getSafeSwapHorizontalRadius() {
        return config.getInt("safe_swap.horizontal_radius", 5);
    }
    
    /**
     * Get the vertical scan distance for safe swaps
     * @return The vertical scan distance
     */
    public int getSafeSwapVerticalDistance() {
        return config.getInt("safe_swap.vertical_distance", 10);
    }
    
    /**
     * Get the set of dangerous block materials
     * @return The set of dangerous block materials
     */
    public Set<Material> getDangerousBlocks() {
        return dangerousBlocks;
    }
    
    /**
     * Get the freeze mode for inactive runners
     * @return The freeze mode (EFFECTS or SPECTATOR)
     */
    public String getFreezeMode() {
        return config.getString("freeze_mode", "EFFECTS");
    }
    
    /**
     * Get whether to cancel movement for inactive runners
     * @return True if movement should be canceled
     */
    public boolean isCancelMovement() {
        return config.getBoolean("cancel.movement", true);
    }
    
    /**
     * Get whether to cancel interactions for inactive runners
     * @return True if interactions should be canceled
     */
    public boolean isCancelInteractions() {
        return config.getBoolean("cancel.interactions", true);
    }
    
    /**
     * Get how often to update the tracker in ticks
     * @return The tracker update interval in ticks
     */
    public int getTrackerUpdateTicks() {
        return config.getInt("tracker.update_ticks", 20);
    }

    /**
     * Get the list of active tracker modes
     * @return The list of tracker modes
     */
    public List<String> getTrackerModes() {
        return config.getStringList("tracker.modes");
    }

    /**
     * Get whether the compass requires the hunter and runner to be in the same world
     * @return True if the same world is required
     */
    public boolean isCompassRequiresSameWorld() {
        return config.getBoolean("tracker.compass-requires-same-world", true);
    }
    
    
    /**
     * Get whether broadcasts are enabled
     * @return True if broadcasts are enabled
     */
    public boolean isBroadcastsEnabled() {
        return config.getBoolean("broadcasts.enabled", true);
    }
    
    /**
     * Get whether to broadcast game events
     * @return True if game events should be broadcast
     */
    public boolean isBroadcastGameEvents() {
        return config.getBoolean("broadcasts.game_events", true);
    }
    
    /**
     * Get whether to broadcast team changes
     * @return True if team changes should be broadcast
     */
    public boolean isBroadcastTeamChanges() {
        return config.getBoolean("broadcasts.team_changes", true);
    }
    
    /**
     * Get whether voice chat integration is enabled
     * @return True if voice chat integration is enabled
     */
    public boolean isVoiceChatEnabled() {
        return config.getBoolean("voice_chat.enabled", true);
    }
    
    /**
     * Get whether to mute inactive runners
     * @return True if inactive runners should be muted
     */
    public boolean isMuteInactiveRunners() {
        return config.getBoolean("voice_chat.mute_inactive_runners", true);
    }

    // Effect Getters
    public Effect getSwapWarningEffect() { return swapWarningEffect; }
    public Effect getSwapExecuteEffect() { return swapExecuteEffect; }
    public Effect getGameStartEffect() { return gameStartEffect; }
    public Effect getGameEndEffect() { return gameEndEffect; }

    // Chat Isolation Getters
    public boolean isBlockRunnerChat() { return blockRunnerChat; }
    public boolean isBlockRunnerPms() { return blockRunnerPms; }
    public String getMessageBlockReply() { return messageBlockReply; }

    // World Whitelist Getter
    public List<String> getAllowedWorlds() { return allowedWorlds; }

    // --- Setters for Live GUI Editing ---

    public void setSwapInterval(int value) {
        config.set("swap.interval", value);
        saveConfig();
        loadConfig(); // Reload to update local fields
    }

    public void setRandomizeSwap(boolean value) {
        config.set("swap.randomize", value);
        saveConfig();
        loadConfig();
    }

    public void setGracePeriodTicks(int value) {
        config.set("swap.grace_period_ticks", value);
        saveConfig();
        loadConfig();
    }

    public void setTrackerModes(List<String> modes) {
        config.set("tracker.modes", modes);
        saveConfig();
        loadConfig();
    }
}