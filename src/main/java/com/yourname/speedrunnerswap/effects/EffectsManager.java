package com.yourname.speedrunnerswap.effects;

import com.yourname.speedrunnerswap.SpeedrunnerSwap;
import com.yourname.speedrunnerswap.config.ConfigManager;
import com.yourname.speedrunnerswap.config.Effect;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.Collection;

public class EffectsManager {

    private final SpeedrunnerSwap plugin;
    private final ConfigManager configManager;

    public EffectsManager(SpeedrunnerSwap plugin) {
        this.plugin = plugin;
        this.configManager = plugin.getConfigManager();
    }

    public void playSwapWarning(Collection<Player> players) {
        playEffect(players, configManager.getSwapWarningEffect(), null);
    }

    public void playSwapExecute(Collection<Player> players, Player newRunner) {
        playEffect(players, configManager.getSwapExecuteEffect(), newRunner);
    }

    public void playGameStart(Collection<Player> players) {
        playEffect(players, configManager.getGameStartEffect(), null);
    }

    public void playGameEnd(Collection<Player> players) {
        playEffect(players, configManager.getGameEndEffect(), null);
    }

    private void playEffect(Collection<Player> players, Effect effect, Player newRunner) {
        if (!effect.enabled()) {
            return;
        }

        // Play sound
        if (effect.sound() != null && !effect.sound().isEmpty()) {
            try {
                Sound sound = Sound.valueOf(effect.sound().toUpperCase());
                for (Player player : players) {
                    player.playSound(player.getLocation(), sound, effect.volume(), effect.pitch());
                }
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Invalid sound name in config.yml: " + effect.sound());
            }
        }

        // Show title
        if (effect.title() != null && !effect.title().isEmpty()) {
            String title = ChatColor.translateAlternateColorCodes('&', effect.title());
            String subtitle = ChatColor.translateAlternateColorCodes('&', effect.subtitle());

            if (newRunner != null) {
                title = title.replace("%new_runner%", newRunner.getName());
                subtitle = subtitle.replace("%new_runner%", newRunner.getName());
            }

            for (Player player : players) {
                player.sendTitle(title, subtitle, 10, 70, 20); // Default fade in/stay/fade out times
            }
        }
    }
}
