package com.yourname.speedrunnerswap.gui;

import com.yourname.speedrunnerswap.SpeedrunnerSwap;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;

public class GuiManager {
    
    private final SpeedrunnerSwap plugin;
    
    public GuiManager(SpeedrunnerSwap plugin) {
        this.plugin = plugin;
    }
    
    public void openMainMenu(Player player) {
        String title = plugin.getConfigManager().getGuiMainMenuTitle();
        int rows = plugin.getConfigManager().getGuiMainMenuRows();
        
        Inventory inventory = Bukkit.createInventory(null, rows * 9, title);
        
        // Team selector button
        ItemStack teamSelector = createItem(Material.PLAYER_HEAD, "§e§lTeam Selector", 
                "§7Click to open the team selection menu");
        inventory.setItem(11, teamSelector);
        
        // Settings button
        ItemStack settings = createItem(Material.COMPARATOR, "§a§lSettings", 
                "§7Click to configure plugin settings");
        inventory.setItem(15, settings);
        
        // Game control buttons
        if (plugin.getGameManager().isGameRunning()) {
            if (plugin.getGameManager().isGamePaused()) {
                ItemStack resume = createItem(Material.LIME_CONCRETE, "§a§lResume Game", 
                        "§7Click to resume the game");
                inventory.setItem(22, resume);
            } else {
                ItemStack pause = createItem(Material.YELLOW_CONCRETE, "§e§lPause Game", 
                        "§7Click to pause the game");
                inventory.setItem(22, pause);
            }
            
            ItemStack stop = createItem(Material.RED_CONCRETE, "§c§lStop Game", 
                    "§7Click to stop the game");
            inventory.setItem(31, stop);
        } else {
            ItemStack start = createItem(Material.GREEN_CONCRETE, "§a§lStart Game", 
                    "§7Click to start the game");
            inventory.setItem(22, start);
        }
        
        // Status button
        ItemStack status = createItem(Material.COMPASS, "§b§lGame Status", 
                getStatusLore());
        inventory.setItem(40, status);
        
        player.openInventory(inventory);
    }
    
    public void openTeamSelector(Player player) {
        String title = plugin.getConfigManager().getGuiTeamSelectorTitle();
        int rows = plugin.getConfigManager().getGuiTeamSelectorRows();
        
        Inventory inventory = Bukkit.createInventory(null, rows * 9, title);
        
        // Back button
        ItemStack back = createItem(Material.ARROW, "§7§lBack", 
                "§7Return to main menu");
        inventory.setItem(0, back);
        
        // Runner team button
        ItemStack runnerTeam = createItem(Material.DIAMOND_BOOTS, "§b§lRunners", 
                "§7Click to select players as runners");
        inventory.setItem(2, runnerTeam);
        
        // Hunter team button
        ItemStack hunterTeam = createItem(Material.IRON_SWORD, "§c§lHunters", 
                "§7Click to select players as hunters");
        inventory.setItem(6, hunterTeam);
        
        // Player heads
        int slot = 18;
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (slot >= inventory.getSize()) break;
            
            ItemStack playerHead = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta meta = (SkullMeta) playerHead.getItemMeta();
            meta.setOwningPlayer(onlinePlayer);
            meta.setDisplayName("§f" + onlinePlayer.getName());
            
            List<String> lore = new ArrayList<>();
            if (plugin.getGameManager().isRunner(onlinePlayer)) {
                lore.add("§bCurrently a Runner");
            } else if (plugin.getGameManager().isHunter(onlinePlayer)) {
                lore.add("§cCurrently a Hunter");
            } else {
                lore.add("§7Click to assign to a team");
            }
            meta.setLore(lore);
            
            playerHead.setItemMeta(meta);
            inventory.setItem(slot++, playerHead);
        }
        
        player.openInventory(inventory);
    }
    
    public void openSettingsMenu(Player player) {
        String title = plugin.getConfigManager().getGuiSettingsTitle();
        Inventory inventory = Bukkit.createInventory(null, 3 * 9, title);

        // Back button
        inventory.setItem(0, createItem(Material.ARROW, "§c§lBack", "§7Return to the main menu."));

        // Swap Interval
        inventory.setItem(10, createItem(Material.CLOCK, "§eSwap Interval",
                "§7Current: §f" + plugin.getConfigManager().getSwapInterval() + "s",
                "§aLeft-click to increase by 5s",
                "§cRight-click to decrease by 5s"));

        // Randomize Swap
        boolean isRandom = plugin.getConfigManager().isRandomizeSwap();
        inventory.setItem(11, createItem(isRandom ? Material.LIME_DYE : Material.GRAY_DYE, "§eRandomize Swap",
                "§7Current: " + (isRandom ? "§aEnabled" : "§cDisabled"),
                "§7Click to toggle."));

        // Grace Period
        inventory.setItem(12, createItem(Material.IRON_BOOTS, "§eGrace Period",
                "§7Current: §f" + String.format("%.1f", plugin.getConfigManager().getGracePeriodTicks() / 20.0) + "s",
                "§aLeft-click to increase by 0.5s",
                "§cRight-click to decrease by 0.5s"));
        
        // Tracker Modes
        List<String> modes = plugin.getConfigManager().getTrackerModes();
        inventory.setItem(14, createItem(Material.COMPASS, "§eTracker: Compass",
                "§7Current: " + (modes.contains("COMPASS") ? "§aEnabled" : "§cDisabled"),
                "§7Click to toggle."));
        inventory.setItem(15, createItem(Material.GLOWSTONE_DUST, "§eTracker: Glowing",
                "§7Current: " + (modes.contains("GLOWING") ? "§aEnabled" : "§cDisabled"),
                "§7Click to toggle."));
        inventory.setItem(16, createItem(Material.PAPER, "§eTracker: Action Bar",
                "§7Current: " + (modes.contains("ACTION_BAR") ? "§aEnabled" : "§cDisabled"),
                "§7Click to toggle."));

        player.openInventory(inventory);
    }
    
    private ItemStack createItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        
        if (lore.length > 0) {
            List<String> loreList = new ArrayList<>();
            for (String line : lore) {
                loreList.add(line);
            }
            meta.setLore(loreList);
        }
        
        item.setItemMeta(meta);
        return item;
    }
    
    private List<String> getStatusLore() {
        List<String> lore = new ArrayList<>();
        
        lore.add("§7Game Running: " + (plugin.getGameManager().isGameRunning() ? "§aYes" : "§cNo"));
        lore.add("§7Game Paused: " + (plugin.getGameManager().isGamePaused() ? "§eYes" : "§aNo"));
        
        if (plugin.getGameManager().isGameRunning()) {
            Player activeRunner = plugin.getGameManager().getActiveRunner();
            lore.add("§7Active Runner: §f" + (activeRunner != null ? activeRunner.getName() : "None"));
            lore.add("§7Next Swap: §f" + plugin.getGameManager().getTimeUntilNextSwap() + "s");
            
            lore.add("");
            lore.add("§bRunners:");
            for (Player runner : plugin.getGameManager().getRunners()) {
                lore.add("§7- §f" + runner.getName());
            }
            
            lore.add("");
            lore.add("§cHunters:");
            for (Player hunter : plugin.getGameManager().getHunters()) {
                lore.add("§7- §f" + hunter.getName());
            }
        }
        
        return lore;
    }
}