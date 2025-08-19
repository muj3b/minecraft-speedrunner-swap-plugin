package com.yourname.speedrunnerswap.integration.placeholderapi;

import com.yourname.speedrunnerswap.SpeedrunnerSwap;
import com.yourname.speedrunnerswap.SwapManager;
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
        return "Jules";
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public @Nullable String onPlaceholderRequest(Player player, @NotNull String params) {
        SwapManager sm = plugin.manager();
        if (sm == null) return "N/A";

        switch (params) {
            case "active_runner":
                if (!sm.isRunning()) return "N/A";
                Player activeRunner = sm.getActiveRunner();
                return activeRunner != null ? activeRunner.getName() : "None";

            case "time_left":
                if (!sm.isRunning()) return "0";
                return String.valueOf(sm.getSecondsLeft());

            case "time_left_formatted":
                if (!sm.isRunning()) return "00:00";
                int seconds = sm.getSecondsLeft();
                return String.format("%02d:%02d", seconds / 60, seconds % 60);

            case "game_status":
                if (sm.isRunning()) {
                    return sm.isPaused() ? "Paused" : "Running";
                }
                return "Stopped";

            default:
                return null;
        }
    }
}
