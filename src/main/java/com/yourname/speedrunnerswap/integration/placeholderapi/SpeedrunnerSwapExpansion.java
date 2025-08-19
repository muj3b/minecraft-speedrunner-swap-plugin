package com.yourname.speedrunnerswap.integration.placeholderapi;

import com.yourname.speedrunnerswap.SpeedrunnerSwap;
import com.yourname.speedrunnerswap.game.GameManager;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SpeedrunnerSwapExpansion extends PlaceholderExpansion {

    private final SpeedrunnerSwap plugin;

    public SpeedrunnerSwapExpansion(SpeedrunnerSwap plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "speedrunnerswap";
    }

    @Override
    public @NotNull String getAuthor() {
        return "Jules"; // Or your name
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true; // The values may change, but the placeholders are always available.
    }

    @Override
    public @Nullable String onPlaceholderRequest(Player player, @NotNull String params) {
        GameManager gm = plugin.getGameManager();

        switch (params) {
            case "active_runner":
                if (!gm.isGameRunning()) return "N/A";
                Player activeRunner = gm.getActiveRunner();
                return activeRunner != null ? activeRunner.getName() : "None";

            case "time_left":
                if (!gm.isGameRunning()) return "0";
                return String.valueOf(gm.getTimeUntilNextSwap());

            case "time_left_formatted":
                if (!gm.isGameRunning()) return "00:00";
                int seconds = gm.getTimeUntilNextSwap();
                return String.format("%02d:%02d", seconds / 60, seconds % 60);

            case "game_status":
                if (gm.isGameRunning()) {
                    return gm.isGamePaused() ? "Paused" : "Running";
                }
                return "Stopped";

            default:
                return null;
        }
    }
}
