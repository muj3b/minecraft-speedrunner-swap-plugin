package com.yourname.speedrunnerswap;

import com.yourname.speedrunnerswap.events.*;
import com.yourname.speedrunnerswap.game.SerializableGameState;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public class SwapManager implements Listener {
    public static final PotionEffect BLINDNESS = new PotionEffect(PotionEffectType.BLINDNESS, Integer.MAX_VALUE, 1, false, false, false);
    private static final PotionEffect ROOT_SLOW = new PotionEffect(PotionEffectType.SLOW, Integer.MAX_VALUE, 255, false, false, false);
    private static final PotionEffect HEAVY_JUMP = new PotionEffect(PotionEffectType.JUMP_BOOST, Integer.MAX_VALUE, 128, false, false, false);

    private final SpeedrunnerSwap plugin;
    private List<String> runnerNames;
    private List<String> hunterNames;
    private final boolean pauseOnDisconnect, safeSwapEnabled, randomize, jitterEnabled, clamp, cancelMove, cancelInteract;
    private final int graceTicks, vLimit, hRadius, interval, min, max, stddev;
    private final String freezeMode;
    private final MuteManager muteManager;
    private final HunterTracker tracker;
    private final EffectsManager effectsManager;

    private final Set<UUID> frozen = new HashSet<>();
    private List<Player> runnersOnline = new ArrayList<>();
    private int activeIndex = 0;
    private boolean running = false, paused = false;
    private Map<UUID, PlayerState> sharedPlayerStates = new HashMap<>();
    private BukkitRunnable swapLoop;
    private int secondsLeft;

    public SwapManager(SpeedrunnerSwap plugin, FileConfiguration c, MuteManager muteManager) {
        this.plugin = plugin;
        this.muteManager = muteManager;
        this.effectsManager = plugin.effectsManager();

        // Load config values
        this.runnerNames = c.getStringList("teams.runners");
        this.hunterNames = c.getStringList("teams.hunters");
        this.randomize = c.getBoolean("swap.randomize", false);
        this.interval = c.getInt("swap.interval_seconds", 60);
        this.min = c.getInt("swap.min_seconds", 45);
        this.max = c.getInt("swap.max_seconds", 90);
        this.jitterEnabled = c.getBoolean("swap.jitter.enabled", true);
        this.stddev = c.getInt("swap.jitter.stddev_seconds", 12);
        this.clamp = c.getBoolean("swap.jitter.clamp_to_min_max", true);
        this.pauseOnDisconnect = c.getBoolean("swap.pause_on_disconnect", true);
        this.graceTicks = c.getInt("swap.grace_period_ticks", 60);
        this.freezeMode = c.getString("blackout.freeze_mode", "EFFECTS").toUpperCase();
        this.cancelMove = c.getBoolean("blackout.cancel_movement", true);
        this.cancelInteract = c.getBoolean("blackout.cancel_interactions", true);
        this.safeSwapEnabled = c.getBoolean("safe_swap.enabled", true);
        this.vLimit = c.getInt("safe_swap.vertical_scan_limit", 16);
        this.hRadius = c.getInt("safe_swap.horizontal_scan_radius", 4);

        this.tracker = new HunterTracker(plugin);
    }

    // Getters for state and config
    public boolean isRunning() { return running; }
    public boolean isPaused() { return paused; }
    public boolean isFrozen(UUID id) { return frozen.contains(id); }
    public boolean cancelMovement() { return cancelMove; }
    public boolean cancelInteractions() { return cancelInteract; }
    public Player getActiveRunner() { return runnersOnline.isEmpty() ? null : runnersOnline.get(Math.min(activeIndex, runnersOnline.size() - 1)); }
    public int getSecondsLeft() { return secondsLeft; }
    public int getActiveIndex() { return activeIndex; }
    public List<UUID> getRunnerUuids() { return runnerNames.stream().map(name -> Bukkit.getOfflinePlayer(name).getUniqueId()).collect(Collectors.toList()); }
    public List<String> getHunterNames() { return hunterNames; }
    public Map<UUID, PlayerState> getSharedPlayerStates() { return sharedPlayerStates; }

    public void start() {
        if (running) return;
        resolveRunners();
        if (runnersOnline.size() < 1) { msgAll(ChatColor.RED + "Need at least 1 online runner to start."); return; }

        running = true;
        paused = false;
        activeIndex = 0;

        Player activeRunner = getActiveRunner();
        sharedPlayerStates.clear();
        for(Player p : runnersOnline) {
            sharedPlayerStates.put(p.getUniqueId(), new PlayerState(p));
        }

        applyToAll(this::unfreeze, this::applyFreeze);
        if (muteManager != null) muteManager.isolateRunners(runnersOnline);

        announce(ChatColor.GREEN + "Speedrunner Swap started! Active runner: " + activeRunner.getName());
        effectsManager.playGameStart(runnersOnline);
        Bukkit.getPluginManager().callEvent(new GameStartEvent());

        startLoop();
        tracker.start();
    }

    public void stop() {
        if (!running) return;

        List<Player> allPlayers = new ArrayList<>(runnersOnline);
        allPlayers.addAll(getHuntersOnline());

        running = false;
        paused = false;
        if (swapLoop != null) swapLoop.cancel();
        tracker.stop();

        applyToAll(this::unfreeze, this::unfreeze); // Unfreeze everyone
        if (muteManager != null) muteManager.clear();

        msgAll(ChatColor.YELLOW + "Speedrunner Swap stopped.");
        effectsManager.playGameEnd(allPlayers);
        Bukkit.getPluginManager().callEvent(new GameStopEvent());
    }

    public void pause() {
        if (running && !paused) {
            paused = true;
            if (swapLoop != null) swapLoop.cancel();
            msgAll(ChatColor.YELLOW + "Swap paused.");
            Bukkit.getPluginManager().callEvent(new GamePauseEvent());
        }
    }

    public void resume() {
        if (running && paused) {
            paused = false;
            startLoop();
            msgAll(ChatColor.GREEN + "Swap resumed.");
            Bukkit.getPluginManager().callEvent(new GameResumeEvent());
        }
    }

    private void startLoop() {
        this.secondsLeft = secondsOrInterval();
        if (swapLoop != null) swapLoop.cancel();

        swapLoop = new BukkitRunnable() {
            @Override public void run() {
                if (!running || paused) {
                    cancel();
                    return;
                }
                if (!ensureMinRunnersOnline()) {
                    if (pauseOnDisconnect) pause();
                    cancel();
                    return;
                }
                if (secondsLeft <= 10 && secondsLeft > 0 && secondsLeft % 2 == 0) {
                     effectsManager.playSwapWarning(runnersOnline);
                }
                if (secondsLeft <= 0) {
                    performSwap();
                    cancel(); // a new loop will be started
                    return;
                }
                secondsLeft--;
            }
        };
        swapLoop.runTaskTimer(plugin, 0L, 20L);
    }

    private void performSwap() {
        Player previousRunner = getActiveRunner();
        if (previousRunner != null) {
            sharedPlayerStates.put(previousRunner.getUniqueId(), new PlayerState(previousRunner));
        }

        activeIndex = (activeIndex + 1) % runnersOnline.size();
        Player nextActive = getActiveRunner();

        PlayerState stateToApply = sharedPlayerStates.get(previousRunner.getUniqueId());
        if(stateToApply != null) {
            if (safeSwapEnabled) {
                var safeLoc = SafeTeleport.findSafe(stateToApply.getLocation(), vLimit, hRadius);
                if (safeLoc != null) nextActive.teleport(safeLoc);
                else nextActive.teleport(stateToApply.getLocation());
            } else {
                 nextActive.teleport(stateToApply.getLocation());
            }
            stateToApply.apply(nextActive);
        }

        if (graceTicks > 0) nextActive.setNoDamageTicks(Math.max(nextActive.getNoDamageTicks(), graceTicks));

        applyToAll(this::unfreeze, this::applyFreeze);

        tracker.updateTarget(nextActive);
        announce(ChatColor.LIGHT_PURPLE + "Swapped! Active runner: " + nextActive.getName());
        effectsManager.playSwapExecute(runnersOnline, nextActive);
        Bukkit.getPluginManager().callEvent(new PlayerSwapEvent(previousRunner, nextActive));

        startLoop(); // Start next cycle
    }

    private void applyToAll(java.util.function.Consumer<Player> active, java.util.function.Consumer<Player> inactive) {
        Player activePlayer = getActiveRunner();
        for (Player p : runnersOnline) {
            if (p.equals(activePlayer)) active.accept(p);
            else inactive.accept(p);
        }
    }

    private void resolveRunners() {
        runnersOnline.clear();
        for (String name : runnerNames) {
            Player p = Bukkit.getPlayerExact(name);
            if (p != null && p.isOnline()) runnersOnline.add(p);
        }
    }

    private List<Player> getHuntersOnline() {
        return hunterNames.stream().map(Bukkit::getPlayerExact).filter(Objects::nonNull).collect(Collectors.toList());
    }

    private boolean ensureMinRunnersOnline() {
        resolveRunners();
        return !runnersOnline.isEmpty();
    }

    private int secondsOrInterval() {
        if (!randomize) return interval;
        return jitterEnabled ? Jitter.gaussianSeconds(interval, stddev, min, max, clamp) : ThreadLocalRandom.current().nextInt(min, max + 1);
    }

    private void applyFreeze(Player p) {
        if (p == null) return;
        frozen.add(p.getUniqueId());
        if ("SPECTATOR".equals(freezeMode)) p.setGameMode(GameMode.SPECTATOR);
        else {
            if (cancelMove) { p.setWalkSpeed(0f); p.setFlySpeed(0f); }
            p.addPotionEffect(BLINDNESS); p.addPotionEffect(ROOT_SLOW); p.addPotionEffect(HEAVY_JUMP);
        }
    }

    private void unfreeze(Player p) {
        if (p == null) return;
        frozen.remove(p.getUniqueId());
        if (p.getGameMode() != GameMode.SURVIVAL) p.setGameMode(GameMode.SURVIVAL);
        if (cancelMove) { p.setWalkSpeed(0.2f); p.setFlySpeed(0.1f); }
        p.removePotionEffect(BLINDNESS.getType()); p.removePotionEffect(ROOT_SLOW.getType()); p.removePotionEffect(HEAVY_JUMP.getType());
    }

    private void msgAll(String s) { if (plugin.getConfig().getBoolean("broadcasts.enabled", true)) Bukkit.broadcastMessage(s); }
    private void announce(String s) { msgAll(s); }

    public void loadState(SerializableGameState state) {
        this.runnerNames = state.getRunnerUuids().stream().map(uuid -> Bukkit.getOfflinePlayer(uuid).getName()).collect(Collectors.toList());
        this.hunterNames = state.getHunterNames();
        this.activeIndex = state.getActiveRunnerIndex();
        this.secondsLeft = state.getSecondsLeft();
        this.sharedPlayerStates = state.getPlayerStates();
        this.paused = state.isPaused();
        this.running = true;

        resolveRunners();
        if (runnersOnline.isEmpty()) {
            stop();
            return;
        }

        applyToAll(this::unfreeze, this::applyFreeze);
        if (muteManager != null) muteManager.isolateRunners(runnersOnline);
        tracker.start();

        if (!paused) {
            startLoop();
        }

        msgAll(ChatColor.GOLD + "Game state loaded from previous session.");
    }

    @EventHandler public void onQuit(PlayerQuitEvent e) {
        if (running && runnerNames.contains(e.getPlayer().getName()) && pauseOnDisconnect) {
            if (getActiveRunner().equals(e.getPlayer())) {
                 msgAll(ChatColor.RED + "Active runner " + e.getPlayer().getName() + " disconnected. Pausing…");
                 pause();
            }
        }
    }

    @EventHandler public void onJoin(PlayerJoinEvent e) {
        if (running && paused && runnerNames.contains(e.getPlayer().getName())) {
            msgAll(ChatColor.YELLOW + e.getPlayer().getName() + " rejoined. Use /swap resume when ready.");
        }
    }
}
