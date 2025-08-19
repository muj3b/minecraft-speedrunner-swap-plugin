package com.yourname.speedrunnerswap.tracking;

import com.yourname.speedrunnerswap.SpeedrunnerSwap;
import com.yourname.speedrunnerswap.utils.ActionBarUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.List;
import java.util.Objects;

public class TrackerManager {

    private final SpeedrunnerSwap plugin;
    private BukkitTask trackerTask;
    private Team glowingTeam;
    private static final String TEAM_NAME = "srswap_glow";

    public TrackerManager(SpeedrunnerSwap plugin) {
        this.plugin = plugin;
    }

    public void startTracking() {
        if (trackerTask != null) {
            trackerTask.cancel();
        }

        List<String> modes = plugin.getConfigManager().getTrackerModes();
        if (modes.isEmpty()) {
            return; // No tracking modes enabled
        }

        if (modes.contains("GLOWING")) {
            setupScoreboardTeam();
        }

        int updateTicks = plugin.getConfigManager().getTrackerUpdateTicks();

        trackerTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            Player activeRunner = plugin.getGameManager().getActiveRunner();
            if (activeRunner == null || !activeRunner.isOnline() || !plugin.getGameManager().isGameRunning()) {
                if (modes.contains("GLOWING")) {
                     // Ensure glowing is turned off if game stops or runner is null
                    if (glowingTeam != null && !glowingTeam.getEntries().isEmpty()) {
                        glowingTeam.getEntries().forEach(entry -> {
                            Player p = Bukkit.getPlayer(entry);
                            if (p != null) p.setGlowing(false);
                        });
                    }
                }
                return;
            }

            if (modes.contains("GLOWING")) {
                updateGlowing(activeRunner);
            }

            for (Player hunter : plugin.getGameManager().getHunters()) {
                if (hunter != null && hunter.isOnline()) {
                    if (modes.contains("COMPASS")) {
                        updateCompass(hunter, activeRunner);
                    }
                    if (modes.contains("ACTION_BAR")) {
                        updateActionBar(hunter, activeRunner);
                    }
                }
            }
        }, 0L, updateTicks);
    }

    public void stopTracking() {
        if (trackerTask != null) {
            trackerTask.cancel();
            trackerTask = null;
        }
        cleanupScoreboardTeam();
    }

    private void setupScoreboardTeam() {
        Scoreboard board = Bukkit.getScoreboardManager().getMainScoreboard();
        glowingTeam = board.getTeam(TEAM_NAME);
        if (glowingTeam == null) {
            glowingTeam = board.registerNewTeam(TEAM_NAME);
        }
        glowingTeam.setColor(ChatColor.WHITE); // Color of the glow, can be configured
        glowingTeam.setCanSeeFriendlyInvisibles(true);
    }

    private void cleanupScoreboardTeam() {
        if (glowingTeam != null) {
            // Make a copy to avoid ConcurrentModificationException
            for (String entry : List.copyOf(glowingTeam.getEntries())) {
                 Player p = Bukkit.getPlayer(entry);
                 if (p != null) p.setGlowing(false);
                 glowingTeam.removeEntry(entry);
            }
            try {
                glowingTeam.unregister();
            } catch (IllegalStateException e) {
                // Team already unregistered, ignore
            }
            glowingTeam = null;
        }
    }

    private void updateGlowing(Player activeRunner) {
        if (glowingTeam == null) return;

        if (!glowingTeam.hasEntry(activeRunner.getName())) {
            glowingTeam.addEntry(activeRunner.getName());
        }
        activeRunner.setGlowing(true);

        for (Player hunter : plugin.getGameManager().getHunters()) {
            if (hunter != null && hunter.isOnline() && !glowingTeam.hasEntry(hunter.getName())) {
                glowingTeam.addEntry(hunter.getName());
            }
        }
    }

    private void updateCompass(Player hunter, Player activeRunner) {
        boolean sameWorldRequired = plugin.getConfigManager().isCompassRequiresSameWorld();
        if (sameWorldRequired && !Objects.equals(hunter.getWorld(), activeRunner.getWorld())) {
            return;
        }
        hunter.setCompassTarget(activeRunner.getLocation());
    }

    private void updateActionBar(Player hunter, Player activeRunner) {
        Location loc = activeRunner.getLocation();
        String message = "§6§lHUNTER §f| §eTarget: §f" + activeRunner.getName() +
                " §7(§f" + loc.getBlockX() + "§7, §f" + loc.getBlockY() + "§7, §f" + loc.getBlockZ() + "§7)";
        ActionBarUtil.sendActionBar(hunter, message);
    }
}