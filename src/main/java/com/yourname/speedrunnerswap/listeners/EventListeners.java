package com.yourname.speedrunnerswap.listeners;

import com.yourname.speedrunnerswap.SpeedrunnerSwap;
import com.yourname.speedrunnerswap.game.GameManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class EventListeners implements Listener {

    private final SpeedrunnerSwap plugin;
    private final List<String> pmCommands = Arrays.asList("/msg", "/tell", "/w", "/whisper", "/pm", "/t");

    public EventListeners(SpeedrunnerSwap plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        GameManager gm = plugin.getGameManager();

        if (gm.isGameRunning() && gm.isRunner(player) && gm.getActiveRunner() == player && plugin.getConfigManager().isPauseOnDisconnect()) {
            gm.pauseGame();
            Bukkit.broadcastMessage("§e[SpeedrunnerSwap] §cGame paused because active runner disconnected.");
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        GameManager gm = plugin.getGameManager();

        if (!gm.isGameRunning() || !plugin.getConfigManager().isBlockRunnerChat()) {
            return;
        }

        // Block chat between runners
        if (gm.isRunner(player)) {
            Set<Player> runnerRecipients = event.getRecipients().stream()
                    .filter(gm::isRunner)
                    .collect(Collectors.toSet());

            if (!runnerRecipients.isEmpty()) {
                // If the sender is the only runner, let them talk to non-runners
                if (runnerRecipients.size() == 1 && runnerRecipients.contains(player)) {
                    return;
                }
                event.getRecipients().removeAll(runnerRecipients);
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfigManager().getMessageBlockReply()));
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        if (!plugin.getGameManager().isGameRunning() || !plugin.getConfigManager().isBlockRunnerPms()) {
            return;
        }

        Player sender = event.getPlayer();
        if (!plugin.getGameManager().isRunner(sender)) {
            return;
        }

        String[] parts = event.getMessage().split(" ");
        String command = parts[0].toLowerCase();

        if (pmCommands.contains(command) && parts.length > 1) {
            Player recipient = Bukkit.getPlayer(parts[1]);
            if (recipient != null && plugin.getGameManager().isRunner(recipient)) {
                if (sender.equals(recipient)) return; // Allow messaging self
                event.setCancelled(true);
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfigManager().getMessageBlockReply()));
            }
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        GameManager gm = plugin.getGameManager();

        if (gm.isGameRunning() && gm.isRunner(player) && gm.getActiveRunner() != player) {
            if (event.getFrom().getX() != event.getTo().getX() || event.getFrom().getY() != event.getTo().getY() || event.getFrom().getZ() != event.getTo().getZ()) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        GameManager gm = plugin.getGameManager();

        if (gm.isGameRunning() && gm.isRunner(player) && gm.getActiveRunner() != player) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryClick(org.bukkit.event.inventory.InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        String title = event.getView().getTitle();

        if (title.equals(plugin.getConfigManager().getSettingsTitle())) {
            event.setCancelled(true);
            if (event.getCurrentItem() == null) return;

            switch (event.getCurrentItem().getType()) {
                case ARROW:
                    plugin.getGuiManager().openMainMenu(player);
                    break;
                case CLOCK: // Swap Interval
                    int currentInterval = plugin.getConfigManager().getSwapInterval();
                    int newInterval = event.isLeftClick() ? currentInterval + 5 : Math.max(5, currentInterval - 5);
                    plugin.getConfigManager().setSwapInterval(newInterval);
                    break;
                case LIME_DYE:
                case GRAY_DYE: // Randomize Swap
                    plugin.getConfigManager().setRandomizeSwap(!plugin.getConfigManager().isRandomizeSwap());
                    break;
                case IRON_BOOTS: // Grace Period
                    int currentGrace = plugin.getConfigManager().getGracePeriodTicks();
                    int newGrace = event.isLeftClick() ? currentGrace + 10 : Math.max(0, currentGrace - 10); // 0.5s increments
                    plugin.getConfigManager().setGracePeriodTicks(newGrace);
                    break;
                case COMPASS: // Tracker: Compass
                    toggleTrackerMode("COMPASS");
                    break;
                case GLOWSTONE_DUST: // Tracker: Glowing
                    toggleTrackerMode("GLOWING");
                    break;
                case PAPER: // Tracker: Action Bar
                    toggleTrackerMode("ACTION_BAR");
                    break;
            }
            // Refresh the settings menu after a change
            if (event.getCurrentItem().getType() != org.bukkit.Material.ARROW) {
                plugin.getGuiManager().openSettingsMenu(player);
            }
        } else if (title.equals(plugin.getConfigManager().getMainMenuTitle())) {
            event.setCancelled(true);
            if (event.getCurrentItem() == null) return;
            switch (event.getCurrentItem().getType()) {
                case PLAYER_HEAD:
                    plugin.getGuiManager().openTeamSelector(player);
                    break;
                case COMPARATOR:
                    plugin.getGuiManager().openSettingsMenu(player);
                    break;
            }
        } else if (title.equals(plugin.getConfigManager().getTeamSelectorTitle())) {
            event.setCancelled(true);
            if (event.getCurrentItem() == null) return;
            if (event.getCurrentItem().getType() == org.bukkit.Material.PLAYER_HEAD) {
                String playerName = ChatColor.stripColor(event.getCurrentItem().getItemMeta().getDisplayName());
                if (event.isLeftClick()) { // Add to runners
                    plugin.getConfigManager().addRunner(playerName);
                } else if (event.isRightClick()) { // Add to hunters
                    plugin.getConfigManager().addHunter(playerName);
                } else if (event.getClick() == org.bukkit.event.inventory.ClickType.MIDDLE) { // Remove from teams
                    plugin.getConfigManager().removeRunner(playerName);
                    plugin.getConfigManager().removeHunter(playerName);
                }
                plugin.getGuiManager().openTeamSelector(player); // Refresh
            } else if (event.getCurrentItem().getType() == org.bukkit.Material.ARROW) {
                 plugin.getGuiManager().openMainMenu(player);
            }
        }
    }

    private void toggleTrackerMode(String mode) {
        List<String> modes = new java.util.ArrayList<>(plugin.getConfigManager().getTrackerModes());
        if (modes.contains(mode)) {
            modes.remove(mode);
        } else {
            modes.add(mode);
        }
        plugin.getConfigManager().setTrackerModes(modes);
    }
}