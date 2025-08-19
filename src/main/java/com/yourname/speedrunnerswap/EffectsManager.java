package com.yourname.speedrunnerswap;

import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Collection;

public class EffectsManager {

    private final SpeedrunnerSwap plugin;

    public EffectsManager(SpeedrunnerSwap plugin) {
        this.plugin = plugin;
    }

    public void playSwapWarning(Collection<Player> players) {
        playEffect(players, "effects.swap-warning", null);
    }

    public void playSwapExecute(Collection<Player> players, Player newRunner) {
        playEffect(players, "effects.swap-execute", newRunner);
    }

    public void playGameStart(Collection<Player> players) {
        playEffect(players, "effects.game-start", null);
    }

    public void playGameEnd(Collection<Player> players) {
        playEffect(players, "effects.game-end", null);
    }

    private void playEffect(Collection<Player> players, String configPath, Player newRunner) {
        ConfigurationSection section = plugin.getConfig().getConfigurationSection(configPath);
        if (section == null || !section.getBoolean("enabled", false)) {
            return;
        }

        // Play sound
        String soundName = section.getString("sound", "");
        if (!soundName.isEmpty()) {
            try {
                Sound sound = Sound.valueOf(soundName.toUpperCase());
                float volume = (float) section.getDouble("volume", 1.0);
                float pitch = (float) section.getDouble("pitch", 1.0);
                for (Player player : players) {
                    player.playSound(player.getLocation(), sound, volume, pitch);
                }
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Invalid sound name in config.yml: " + soundName);
            }
        }

        // Show title
        String title = section.getString("title", "");
        if (!title.isEmpty()) {
            String subtitle = section.getString("subtitle", "");
            if (newRunner != null) {
                title = title.replace("%new_runner%", newRunner.getName());
                subtitle = subtitle.replace("%new_runner%", newRunner.getName());
            }

            title = ChatColor.translateAlternateColorCodes('&', title);
            subtitle = ChatColor.translateAlternateColorCodes('&', subtitle);

            for (Player player : players) {
                player.sendTitle(title, subtitle, 10, 70, 20);
            }
        }
    }
}
