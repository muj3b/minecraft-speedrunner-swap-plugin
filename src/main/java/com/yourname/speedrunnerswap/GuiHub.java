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

public class GuiHub implements Listener {
    private final SpeedrunnerSwap plugin;
    private final TeamSelectMenu teams;
    private final SettingsMenu settings;
    private final String title;

    public GuiHub(SpeedrunnerSwap plugin) {
        this.plugin = plugin;
        this.teams = new TeamSelectMenu(plugin);
        this.settings = new SettingsMenu(plugin);
        this.title = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("gui.title_main", "&dSpeedrunner Swap"));
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void open(Player p) {
        Inventory inv = Bukkit.createInventory(null, 9, title);
        inv.setItem(3, createButton(Material.PLAYER_HEAD, "&bTeam Selector"));
        inv.setItem(5, createButton(Material.COMPARATOR, "&aSettings"));
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
        if (e.getWhoClicked() instanceof Player p) {
            ItemStack clicked = e.getCurrentItem();
            if (clicked == null || clicked.getType() == Material.AIR) return;
            String displayName = ChatColor.stripColor(clicked.getItemMeta().getDisplayName());
            if (displayName.equalsIgnoreCase("Team Selector")) teams.open(p);
            if (displayName.equalsIgnoreCase("Settings")) settings.open(p);
        }
    }
}
