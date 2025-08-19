package com.yourname.speedrunnerswap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class MuteManager implements Listener {
    private final Set<UUID> isolatedRunners = new HashSet<>();

    public MuteManager(SpeedrunnerSwap plugin) {}

    public void isolateRunners(List<Player> runners) {
        isolatedRunners.clear();
        for (Player p : runners) isolatedRunners.add(p.getUniqueId());
    }

    public void clear() { isolatedRunners.clear(); }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChat(AsyncPlayerChatEvent e) {
        if (isolatedRunners.isEmpty()) return;
        Player sender = e.getPlayer();
        if (isolatedRunners.contains(sender.getUniqueId())) {
            e.getRecipients().removeIf(recipient -> isolatedRunners.contains(recipient.getUniqueId()));
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onCommand(PlayerCommandPreprocessEvent e) {
        if (isolatedRunners.isEmpty()) return;
        String msg = e.getMessage().toLowerCase();
        String[] parts = msg.split("\\s+");
        if (parts.length < 2) return;

        String cmd = parts[0];
        if (cmd.equals("/msg") || cmd.equals("/tell") || cmd.equals("/w")) {
            Player sender = e.getPlayer();
            Player target = Bukkit.getPlayerExact(parts[1]);
            if (sender != null && target != null && isolatedRunners.contains(sender.getUniqueId()) && isolatedRunners.contains(target.getUniqueId())) {
                sender.sendMessage(ChatColor.RED + "You cannot message other runners during the game.");
                e.setCancelled(true);
            }
        }
    }
}
