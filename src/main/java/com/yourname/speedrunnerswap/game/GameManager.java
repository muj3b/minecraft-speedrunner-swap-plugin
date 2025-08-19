package com.yourname.speedrunnerswap.game;

import com.yourname.speedrunnerswap.SpeedrunnerSwap;
import com.yourname.speedrunnerswap.events.*;
import com.yourname.speedrunnerswap.utils.ActionBarUtil;
import com.yourname.speedrunnerswap.utils.PlayerStateUtil;
import com.yourname.speedrunnerswap.utils.SafeLocationFinder;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public class GameManager {

    private final SpeedrunnerSwap plugin;
    private boolean gameRunning;
    private boolean gamePaused;
    private Player activeRunner;
    private int activeRunnerIndex;
    private List<Player> runners;
    private List<Player> hunters;
    private BukkitTask swapTask;
    private BukkitTask actionBarTask;
    private long nextSwapTime;
    private Map<UUID, PlayerState> playerStates;

    public GameManager(SpeedrunnerSwap plugin) {
        this.plugin = plugin;
        this.gameRunning = false;
        this.gamePaused = false;
        this.activeRunnerIndex = 0;
        this.runners = new ArrayList<>();
        this.hunters = new ArrayList<>();
        this.playerStates = new HashMap<>();
    }

    public boolean startGame() {
        if (gameRunning) {
            return false;
        }
        loadTeams();
        if (runners.size() < 1) {
            return false;
        }
        gameRunning = true;
        gamePaused = false;
        activeRunnerIndex = 0;
        activeRunner = runners.get(activeRunnerIndex);
        saveAllPlayerStates();
        applyInactiveEffects();
        scheduleNextSwap();
        startActionBarUpdates();
        plugin.getTrackerManager().startTracking();
        if (plugin.getConfigManager().isBroadcastGameEvents()) {
            Bukkit.broadcastMessage("§a[SpeedrunnerSwap] Game started! Active runner: " + activeRunner.getName());
        }
        plugin.getEffectsManager().playGameStart(new ArrayList<>(runners));
        Bukkit.getPluginManager().callEvent(new GameStartEvent());
        return true;
    }

    public void stopGame() {
        if (!gameRunning) {
            return;
        }

        List<Player> allPlayers = new ArrayList<>();
        allPlayers.addAll(runners);
        allPlayers.addAll(hunters);

        if (swapTask != null) swapTask.cancel();
        if (actionBarTask != null) actionBarTask.cancel();
        plugin.getTrackerManager().stopTracking();
        restoreAllPlayerStates();
        gameRunning = false;
        gamePaused = false;
        activeRunner = null;
        playerStates.clear();
        plugin.getGameStateManager().saveState(this); // This will delete the state file
        if (plugin.getConfigManager().isBroadcastGameEvents()) {
            Bukkit.broadcastMessage("§c[SpeedrunnerSwap] Game stopped!");
        }
        plugin.getEffectsManager().playGameEnd(allPlayers);
        Bukkit.getPluginManager().callEvent(new GameStopEvent());
    }

    public boolean pauseGame() {
        if (!gameRunning || gamePaused) {
            return false;
        }
        if (swapTask != null) swapTask.cancel();
        gamePaused = true;
        plugin.getGameStateManager().saveState(this);
        if (plugin.getConfigManager().isBroadcastGameEvents()) {
            Bukkit.broadcastMessage("§e[SpeedrunnerSwap] Game paused!");
        }
        Bukkit.getPluginManager().callEvent(new GamePauseEvent());
        return true;
    }

    public boolean resumeGame() {
        if (!gameRunning || !gamePaused) {
            return false;
        }
        gamePaused = false;
        scheduleNextSwap(getRemainingSwapTimeMillis());
        if (plugin.getConfigManager().isBroadcastGameEvents()) {
            Bukkit.broadcastMessage("§a[SpeedrunnerSwap] Game resumed!");
        }
        Bukkit.getPluginManager().callEvent(new GameResumeEvent());
        return true;
    }

    public void performSwap() {
        if (!gameRunning || gamePaused || runners.isEmpty()) return;

        if (activeRunner != null && activeRunner.isOnline()) {
            savePlayerState(activeRunner);
        }

        Player previousRunner = activeRunner;
        Player nextRunner = findNextOnlineRunner();

        if (nextRunner == null) {
            pauseGame();
            Bukkit.broadcastMessage("§e[SpeedrunnerSwap] No runners online. Pausing game.");
            return;
        }

        PlayerState stateToApply = playerStates.get(previousRunner.getUniqueId());
        Location swapLocation = previousRunner.getLocation();

        if (plugin.getConfigManager().isSafeSwapEnabled()) {
            Location safeLocation = SafeLocationFinder.findSafeLocation(swapLocation,
                    plugin.getConfigManager().getSafeSwapHorizontalRadius(),
                    plugin.getConfigManager().getSafeSwapVerticalDistance(),
                    plugin.getConfigManager().getDangerousBlocks());
            if (safeLocation != null) {
                swapLocation = safeLocation;
            }
        }

        nextRunner.teleport(swapLocation);

        if (stateToApply != null) {
            PlayerStateUtil.applyPlayerState(nextRunner, stateToApply);
        }

        int gracePeriodTicks = plugin.getConfigManager().getGracePeriodTicks();
        if (gracePeriodTicks > 0) {
            nextRunner.setInvulnerable(true);
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (nextRunner.isOnline()) nextRunner.setInvulnerable(false);
            }, gracePeriodTicks);
        }

        activeRunner = nextRunner;
        applyInactiveEffects();
        scheduleNextSwap();

        if (plugin.getConfigManager().isBroadcastsEnabled()) {
            Bukkit.broadcastMessage("§6[SpeedrunnerSwap] Swapped from " + previousRunner.getName() + " to " + activeRunner.getName() + "!");
        }
        plugin.getEffectsManager().playSwapExecute(new ArrayList<>(runners), activeRunner);
        Bukkit.getPluginManager().callEvent(new PlayerSwapEvent(previousRunner, nextRunner));
    }

    private Player findNextOnlineRunner() {
        int initialIndex = activeRunnerIndex;
        do {
            activeRunnerIndex = (activeRunnerIndex + 1) % runners.size();
            Player next = runners.get(activeRunnerIndex);
            if (next.isOnline()) {
                return next;
            }
        } while (activeRunnerIndex != initialIndex);
        return null;
    }

    private void scheduleNextSwap() {
        long intervalSeconds;
        if (plugin.getConfigManager().isRandomizeSwap()) {
            double mean = plugin.getConfigManager().getSwapInterval();
            double stdDev = plugin.getConfigManager().getJitterStdDev();
            double jitteredInterval = ThreadLocalRandom.current().nextGaussian() * stdDev + mean;
            if (plugin.getConfigManager().isClampJitter()) {
                int min = plugin.getConfigManager().getMinSwapInterval();
                int max = plugin.getConfigManager().getMaxSwapInterval();
                jitteredInterval = Math.max(min, Math.min(max, jitteredInterval));
            }
            intervalSeconds = Math.round(jitteredInterval);
        } else {
            intervalSeconds = plugin.getConfigManager().getSwapInterval();
        }
        scheduleNextSwap(intervalSeconds * 1000);
    }

    private void scheduleNextSwap(long remainingMillis) {
        if (swapTask != null) swapTask.cancel();

        // Schedule the main swap task
        long intervalTicks = Math.max(1, remainingMillis / 50);
        nextSwapTime = System.currentTimeMillis() + remainingMillis;
        swapTask = Bukkit.getScheduler().runTaskLater(plugin, this::performSwap, intervalTicks);

        // Schedule the 10-second warning if applicable
        if (remainingMillis > 10000) {
            long warningTicks = intervalTicks - 200; // 200 ticks = 10 seconds
            if (warningTicks > 0) {
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    // Check if game is still running and not paused before sending warning
                    if (gameRunning && !gamePaused) {
                        plugin.getEffectsManager().playSwapWarning(new ArrayList<>(runners));
                    }
                }, warningTicks);
            }
        }
    }

    private void startActionBarUpdates() {
        if (actionBarTask != null) actionBarTask.cancel();
        actionBarTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (!gameRunning) return;
            for (Player player : Bukkit.getOnlinePlayers()) {
                updateActionBar(player);
            }
        }, 0L, 5L);
    }

    private void updateActionBar(Player player) {
        if (!gameRunning) return;
        String message;
        if (gamePaused) {
            message = "§e§lGAME PAUSED";
        } else if (player.equals(activeRunner)) {
            String status = player.isSneaking() ? " §7[Sneaking]" : (player.isSprinting() ? " §b[Sprinting]" : "");
            message = "§a§lACTIVE §f| §eNext swap: §f" + getTimeUntilNextSwap() + "s" + status;
        } else if (runners.contains(player)) {
            message = "§c§lINACTIVE §f| §eNext swap: §f" + getTimeUntilNextSwap() + "s";
        } else if (hunters.contains(player)) {
            // The TrackerManager now handles the hunter's action bar.
            // This space can be used for other hunter-specific info if needed in the future.
            message = "§c§lHUNTING: " + activeRunner.getName();
        } else {
            message = "§7§lSPECTATOR §f| §eActive: §f" + (activeRunner != null ? activeRunner.getName() : "None");
        }
        ActionBarUtil.sendActionBar(player, message);
    }

    private void applyInactiveEffects() {
        String freezeMode = plugin.getConfigManager().getFreezeMode();
        boolean useVoiceChat = plugin.getConfigManager().isVoiceChatEnabled() && plugin.getConfigManager().isMuteInactiveRunners();

        for (Player runner : runners) {
            if (runner.equals(activeRunner)) {
                // Clear inactive effects
                runner.removePotionEffect(PotionEffectType.BLINDNESS);
                runner.removePotionEffect(PotionEffectType.DARKNESS);
                runner.removePotionEffect(PotionEffectType.SLOW);
                runner.removePotionEffect(PotionEffectType.JUMP_BOOST);
                if (runner.getGameMode() != GameMode.SURVIVAL && runner.getGameMode() != GameMode.CREATIVE) {
                    runner.setGameMode(GameMode.SURVIVAL);
                }
                if (useVoiceChat) plugin.getVoiceChatIntegration().unmutePlayer(runner);
            } else {
                // Apply inactive effects
                if (freezeMode.equalsIgnoreCase("EFFECTS")) {
                    runner.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, Integer.MAX_VALUE, 1, false, false, false));
                    runner.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS, Integer.MAX_VALUE, 1, false, false, false));
                    runner.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, Integer.MAX_VALUE, 255, false, false, false));
                    runner.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, Integer.MAX_VALUE, 128, false, false, false));
                } else if (freezeMode.equalsIgnoreCase("SPECTATOR")) {
                    runner.setGameMode(GameMode.SPECTATOR);
                }
                if (useVoiceChat) plugin.getVoiceChatIntegration().mutePlayer(runner);
            }
        }
    }

    private void loadTeams() {
        runners.clear();
        hunters.clear();
        plugin.getConfigManager().getRunnerNames().stream()
                .map(Bukkit::getPlayerExact).filter(Objects::nonNull).forEach(runners::add);
        plugin.getConfigManager().getHunterNames().stream()
                .map(Bukkit::getPlayerExact).filter(Objects::nonNull).forEach(hunters::add);
    }

    private void saveAllPlayerStates() {
        for (Player runner : runners) {
            savePlayerState(runner);
        }
    }

    private void savePlayerState(Player player) {
        if (player != null && player.isOnline()) {
            playerStates.put(player.getUniqueId(), PlayerStateUtil.capturePlayerState(player));
        }
    }

    private void restoreAllPlayerStates() {
        boolean useVoiceChat = plugin.getConfigManager().isVoiceChatEnabled() && plugin.getConfigManager().isMuteInactiveRunners();

        for (UUID uuid : playerStates.keySet()) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null && player.isOnline()) {
                // Restore original state (inventory, location, etc.)
                restorePlayerState(player);

                // Clean up any game-specific effects
                player.removePotionEffect(PotionEffectType.BLINDNESS);
                player.removePotionEffect(PotionEffectType.DARKNESS);
                player.removePotionEffect(PotionEffectType.SLOW);
                player.removePotionEffect(PotionEffectType.JUMP_BOOST);

                if (player.getGameMode() == GameMode.SPECTATOR) {
                    player.setGameMode(GameMode.SURVIVAL);
                }

                if (useVoiceChat) {
                    plugin.getVoiceChatIntegration().unmutePlayer(player);
                }
            }
        }
        playerStates.clear();
    }

    private void restorePlayerState(Player player) {
        if (player != null && player.isOnline()) {
            PlayerState state = playerStates.get(player.getUniqueId());
            if (state != null) {
                PlayerStateUtil.applyPlayerState(player, state);
            }
        }
    }

    public void setRunners(List<Player> players) {
        plugin.getConfigManager().setRunners(players.stream().map(Player::getName).collect(Collectors.toList()));
        loadTeams();
        if (plugin.getConfigManager().isBroadcastTeamChanges()) {
            Bukkit.broadcastMessage("§a[SpeedrunnerSwap] Runners set: " + players.stream().map(Player::getName).collect(Collectors.joining(", ")));
        }
    }

    public void setHunters(List<Player> players) {
        plugin.getConfigManager().setHunters(players.stream().map(Player::getName).collect(Collectors.toList()));
        loadTeams();
        if (plugin.getConfigManager().isBroadcastTeamChanges()) {
            Bukkit.broadcastMessage("§c[SpeedrunnerSwap] Hunters set: " + players.stream().map(Player::getName).collect(Collectors.joining(", ")));
        }
    }

    public boolean isActiveRunner(Player player) {
        return gameRunning && activeRunner != null && activeRunner.equals(player);
    }

    public boolean isRunner(Player player) {
        return runners.contains(player);
    }

    public boolean isHunter(Player player) {
        return hunters.contains(player);
    }

    public Player getActiveRunner() { return activeRunner; }
    public List<Player> getRunners() { return new ArrayList<>(runners); }
    public List<Player> getHunters() { return new ArrayList<>(hunters); }
    public boolean isGameRunning() { return gameRunning; }
    public boolean isGamePaused() { return gamePaused; }
    public int getActiveRunnerIndex() { return activeRunnerIndex; }
    public Map<UUID, PlayerState> getPlayerStates() { return playerStates; }

    public int getTimeUntilNextSwap() {
        if (!gameRunning || gamePaused) return 0;
        return Math.max(0, (int) ((nextSwapTime - System.currentTimeMillis()) / 1000));
    }

    public long getRemainingSwapTimeMillis() {
        if (!gameRunning || gamePaused) return 0;
        return Math.max(0, nextSwapTime - System.currentTimeMillis());
    }

    public void shutdown() {
        if (gameRunning && !gamePaused) {
            pauseGame();
        }
    }

    public void loadGameFromState(SerializableGameState state) {
        // This method now handles offline players by storing UUIDs and resolving them on load.
        // The actual player lists will only contain online players.
        List<UUID> runnerUuids = state.getRunnerUuids();
        List<UUID> hunterUuids = state.getHunterUuids();

        runners = runnerUuids.stream().map(Bukkit::getPlayer).filter(Objects::nonNull).collect(Collectors.toList());
        hunters = hunterUuids.stream().map(Bukkit::getPlayer).filter(Objects::nonNull).collect(Collectors.toList());

        playerStates = state.getPlayerStates();
        activeRunnerIndex = state.getActiveRunnerIndex();
        gamePaused = state.isPaused();
        gameRunning = true;

        if (runners.isEmpty()) {
             Bukkit.broadcastMessage("§c[SpeedrunnerSwap] Could not load game, no runners are online.");
             stopGame();
             return;
        }

        if (activeRunnerIndex >= runners.size()) {
            activeRunnerIndex = 0; // Reset to first online runner
        }
        activeRunner = runners.get(activeRunnerIndex);

        startActionBarUpdates();
        plugin.getTrackerManager().startTracking();
        applyInactiveEffects();

        if (!gamePaused) {
            scheduleNextSwap(state.getRemainingSwapTime());
        }

        Bukkit.broadcastMessage("§a[SpeedrunnerSwap] Previous game state loaded. Game is " + (gamePaused ? "paused" + (runners.size() != runnerUuids.size() ? " (some runners offline)" : "") : "running") + ".");
    }
}