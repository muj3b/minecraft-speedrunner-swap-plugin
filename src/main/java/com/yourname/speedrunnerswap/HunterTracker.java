package com.yourname.speedrunnerswap;

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
import java.util.stream.Collectors;

public class HunterTracker {

    private final SpeedrunnerSwap plugin;
    private BukkitTask trackerTask;
    private Team glowingTeam;
    private Player currentTarget;
    private static final String TEAM_NAME = "srswap_glow";

    public HunterTracker(SpeedrunnerSwap plugin) {
        this.plugin = plugin;
    }

    public void start() {
        if (trackerTask != null) {
            trackerTask.cancel();
        }

        List<String> modes = plugin.getConfig().getStringList("tracker.modes");
        if (modes.isEmpty()) {
            return;
        }

        if (modes.contains("GLOWING")) {
            setupScoreboardTeam();
        }

        int updateTicks = plugin.getConfig().getInt("tracker.update_ticks", 20);

        trackerTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (currentTarget == null || !currentTarget.isOnline() || !plugin.manager().isRunning()) {
                if (modes.contains("GLOWING")) {
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
                updateGlowing(currentTarget);
            }

            for (Player hunter : getHuntersOnline()) {
                if (hunter != null && hunter.isOnline()) {
                    if (modes.contains("COMPASS")) {
                        updateCompass(hunter, currentTarget);
                    }
                    if (modes.contains("ACTION_BAR")) {
                        updateActionBar(hunter, currentTarget);
                    }
                }
            }
        }, 0L, updateTicks);
    }

    public void updateTarget(Player target) {
        this.currentTarget = target;
    }

    public void stop() {
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
        glowingTeam.setColor(ChatColor.WHITE);
        glowingTeam.setCanSeeFriendlyInvisibles(true);
    }

    private void cleanupScoreboardTeam() {
        if (glowingTeam != null) {
            for (String entry : List.copyOf(glowingTeam.getEntries())) {
                 Player p = Bukkit.getPlayer(entry);
                 if (p != null) p.setGlowing(false);
                 glowingTeam.removeEntry(entry);
            }
            try {
                glowingTeam.unregister();
            } catch (IllegalStateException e) {
                // Ignore
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

        for (Player hunter : getHuntersOnline()) {
            if (hunter != null && hunter.isOnline() && !glowingTeam.hasEntry(hunter.getName())) {
                glowingTeam.addEntry(hunter.getName());
            }
        }
    }

    private void updateCompass(Player hunter, Player activeRunner) {
        boolean sameWorldRequired = plugin.getConfig().getBoolean("tracker.compass-requires-same-world", true);
        if (sameWorldRequired && !Objects.equals(hunter.getWorld(), activeRunner.getWorld())) {
            return;
        }
        hunter.setCompassTarget(activeRunner.getLocation());
    }

    private void updateActionBar(Player hunter, Player activeRunner) {
        Location loc = activeRunner.getLocation();
        String message = String.format("§6§lHUNTER §f| §eTarget: §f%s §7(§f%d§7, §f%d§7, §f%d§7)",
                activeRunner.getName(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
        ActionBarUtil.sendActionBar(hunter, message);
    }

    private List<Player> getHuntersOnline() {
        return plugin.getConfig().getStringList("hunters").stream()
                .map(Bukkit::getPlayerExact)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
}
