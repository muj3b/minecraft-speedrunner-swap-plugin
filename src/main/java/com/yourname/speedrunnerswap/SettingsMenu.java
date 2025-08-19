package com.yourname.speedrunnerswap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import java.util.List;

public class SettingsMenu implements Listener {
    private final SpeedrunnerSwap plugin;
    private final String title;

    public SettingsMenu(SpeedrunnerSwap plugin) {
        this.plugin = plugin;
        this.title = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("gui.title_settings", "&aSettings"));
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void open(Player p) {
        Inventory inv = Bukkit.createInventory(null, 27, title);
        inv.setItem(10, createToggle("swap.randomize", "&eRandomize Swaps", Material.CLOCK));
        inv.setItem(11, createValue("swap.interval_seconds", "&bInterval", Material.PAPER, "s"));
        inv.setItem(12, createValue("swap.min_seconds", "&bMin Time", Material.IRON_INGOT, "s"));
        inv.setItem(13, createValue("swap.max_seconds", "&bMax Time", Material.GOLD_INGOT, "s"));
        inv.setItem(14, createToggle("swap.jitter.enabled", "&eJitter Enabled", Material.FEATHER));
        inv.setItem(15, createToggle("safe_swap.enabled", "&eSafe Swap", Material.SHIELD));
        inv.setItem(16, createToggle("tracker.enabled", "&eHunter Tracker", Material.COMPASS));
        p.openInventory(inv);
    }

    private ItemStack createToggle(String path, String name, Material mat) {
        boolean enabled = plugin.getConfig().getBoolean(path);
        ItemStack item = new ItemStack(enabled ? mat : Material.GUNPOWDER);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
        meta.setLore(List.of(enabled ? ChatColor.GREEN + "Enabled" : ChatColor.RED + "Disabled", ChatColor.YELLOW + "Click to toggle"));
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createValue(String path, String name, Material mat, String unit) {
        int value = plugin.getConfig().getInt(path);
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
        meta.setLore(List.of(ChatColor.AQUA + "Current: " + value + unit, ChatColor.YELLOW + "Left-click: +5", ChatColor.YELLOW + "Right-click: -5"));
        item.setItemMeta(meta);
        return item;
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!e.getView().getTitle().equals(title)) return;
        e.setCancelled(true);
        Player p = (Player) e.getWhoClicked();
        ItemStack clicked = e.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta()) return;

        String displayName = ChatColor.stripColor(clicked.getItemMeta().getDisplayName());
        String path = switch(displayName) {
            case "Randomize Swaps" -> "swap.randomize";
            case "Jitter Enabled" -> "swap.jitter.enabled";
            case "Safe Swap" -> "safe_swap.enabled";
            case "Hunter Tracker" -> "tracker.enabled";
            case "Interval" -> "swap.interval_seconds";
            case "Min Time" -> "swap.min_seconds";
            case "Max Time" -> "swap.max_seconds";
            default -> null;
        };

        if (path != null) {
            if (plugin.getConfig().isBoolean(path)) {
                plugin.getConfig().set(path, !plugin.getConfig().getBoolean(path));
            } else if (plugin.getConfig().isInt(path)) {
                int current = plugin.getConfig().getInt(path);
                int change = e.isLeftClick() ? 5 : -5;
                plugin.getConfig().set(path, Math.max(1, current + change));
            }
            plugin.saveConfig();
            open(p); // Refresh GUI
        }
    }
}
