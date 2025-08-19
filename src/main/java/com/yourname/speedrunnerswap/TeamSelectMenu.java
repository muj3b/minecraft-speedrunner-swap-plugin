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
import java.util.ArrayList;
import java.util.List;

public class TeamSelectMenu implements Listener {
    private final SpeedrunnerSwap plugin;
    private final String title;
    private final int rows;

    public TeamSelectMenu(SpeedrunnerSwap plugin) {
        this.plugin = plugin;
        this.title = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("gui.title_teams", "&bTeam Selector"));
        this.rows = plugin.getConfig().getInt("gui.rows", 5);
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void open(Player p) {
        Inventory inv = Bukkit.createInventory(null, rows * 9, title);
        int slot = 0;
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (slot >= inv.getSize() - 9) break;
            ItemStack head = new ItemStack(Material.PLAYER_HEAD);
            ItemMeta meta = head.getItemMeta();
            meta.setDisplayName(ChatColor.AQUA + online.getName());
            meta.setLore(List.of(ChatColor.YELLOW + "Left-click: Add to Runners", ChatColor.GOLD + "Right-click: Add to Hunters"));
            head.setItemMeta(meta);
            inv.setItem(slot++, head);
        }
        inv.setItem(inv.getSize() - 5, createButton(Material.BARRIER, "&cClear Teams"));
        inv.setItem(inv.getSize() - 1, createButton(Material.WRITABLE_BOOK, "&aSave & Reload Config"));
        p.openInventory(inv);
    }

    private ItemStack createButton(Material mat, String name) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
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

        if (displayName.equalsIgnoreCase("Clear Teams")) {
            plugin.getConfig().set("runners", new ArrayList<>());
            plugin.getConfig().set("hunters", new ArrayList<>());
            plugin.saveConfig();
            p.sendMessage(ChatColor.YELLOW + "Teams cleared. Use '/swap reload' to apply.");
            p.closeInventory();
        } else if (displayName.equalsIgnoreCase("Save & Reload Config")) {
            plugin.reloadAndRebuild();
            p.sendMessage(ChatColor.GREEN + "Config saved and reloaded.");
            p.closeInventory();
        } else if (clicked.getType() == Material.PLAYER_HEAD) {
            String targetName = displayName;
            if (e.isLeftClick()) { // Add to runners
                List<String> runners = plugin.getConfig().getStringList("runners");
                if (!runners.contains(targetName)) {
                    runners.add(targetName);
                    plugin.getConfig().set("runners", runners);
                    plugin.saveConfig();
                    p.sendMessage(ChatColor.AQUA + targetName + " added to Runners.");
                } else {
                    p.sendMessage(ChatColor.YELLOW + targetName + " is already a Runner.");
                }
            } else if (e.isRightClick()) { // Add to hunters
                List<String> hunters = plugin.getConfig().getStringList("hunters");
                if (!hunters.contains(targetName)) {
                    hunters.add(targetName);
                    plugin.getConfig().set("hunters", hunters);
                    plugin.saveConfig();
                    p.sendMessage(ChatColor.GOLD + targetName + " added to Hunters.");
                } else {
                    p.sendMessage(ChatColor.YELLOW + targetName + " is already a Hunter.");
                }
            }
        }
    }
}
