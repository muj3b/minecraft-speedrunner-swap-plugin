package com.yourname.speedrunnerswap.commands;

import com.yourname.speedrunnerswap.SpeedrunnerSwap;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class SwapCommand implements CommandExecutor, TabCompleter {
    
    private final SpeedrunnerSwap plugin;
    
    public SwapCommand(SpeedrunnerSwap plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            // Open the main GUI if no arguments provided
            if (sender instanceof Player) {
                plugin.getGuiManager().openMainMenu((Player) sender);
                return true;
            } else {
                sender.sendMessage("§cThis command can only be used by players.");
                return false;
            }
        }
        
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "start":
                return handleStart(sender);
            case "stop":
                return handleStop(sender);
            case "pause":
                return handlePause(sender);
            case "resume":
                return handleResume(sender);
            case "status":
                return handleStatus(sender);
            case "setrunners":
                return handleSetRunners(sender, Arrays.copyOfRange(args, 1, args.length));
            case "sethunters":
                return handleSetHunters(sender, Arrays.copyOfRange(args, 1, args.length));
            case "reload":
                return handleReload(sender);
            case "gui":
                return handleGui(sender);
            default:
                sender.sendMessage("§cUnknown subcommand. Use /swap for help.");
                return false;
        }
    }
    
    private boolean handleStart(CommandSender sender) {
        if (!sender.hasPermission("speedrunnerswap.command")) {
            sender.sendMessage("§cYou don't have permission to use this command.");
            return false;
        }
        
        if (plugin.getGameManager().isGameRunning()) {
            sender.sendMessage("§cThe game is already running.");
            return false;
        }

        if (sender instanceof Player) {
            Player player = (Player) sender;
            List<String> allowedWorlds = plugin.getConfigManager().getAllowedWorlds();
            if (allowedWorlds != null && !allowedWorlds.isEmpty() && !allowedWorlds.contains(player.getWorld().getName())) {
                sender.sendMessage("§cYou cannot start the game in this world. Please go to one of the allowed worlds.");
                return false;
            }
        }
        
        boolean success = plugin.getGameManager().startGame();
        if (success) {
            sender.sendMessage("§aGame started successfully.");
        } else {
            sender.sendMessage("§cFailed to start the game. Make sure there are runners set.");
        }
        
        return success;
    }
    
    private boolean handleStop(CommandSender sender) {
        if (!sender.hasPermission("speedrunnerswap.command")) {
            sender.sendMessage("§cYou don't have permission to use this command.");
            return false;
        }
        
        if (!plugin.getGameManager().isGameRunning()) {
            sender.sendMessage("§cThe game is not running.");
            return false;
        }
        
        plugin.getGameManager().stopGame();
        sender.sendMessage("§aGame stopped.");
        
        return true;
    }
    
    private boolean handlePause(CommandSender sender) {
        if (!sender.hasPermission("speedrunnerswap.command")) {
            sender.sendMessage("§cYou don't have permission to use this command.");
            return false;
        }
        
        if (!plugin.getGameManager().isGameRunning()) {
            sender.sendMessage("§cThe game is not running.");
            return false;
        }
        
        if (plugin.getGameManager().isGamePaused()) {
            sender.sendMessage("§cThe game is already paused.");
            return false;
        }
        
        boolean success = plugin.getGameManager().pauseGame();
        if (success) {
            sender.sendMessage("§aGame paused.");
        } else {
            sender.sendMessage("§cFailed to pause the game.");
        }
        
        return success;
    }
    
    private boolean handleResume(CommandSender sender) {
        if (!sender.hasPermission("speedrunnerswap.command")) {
            sender.sendMessage("§cYou don't have permission to use this command.");
            return false;
        }
        
        if (!plugin.getGameManager().isGameRunning()) {
            sender.sendMessage("§cThe game is not running.");
            return false;
        }
        
        if (!plugin.getGameManager().isGamePaused()) {
            sender.sendMessage("§cThe game is not paused.");
            return false;
        }
        
        boolean success = plugin.getGameManager().resumeGame();
        if (success) {
            sender.sendMessage("§aGame resumed.");
        } else {
            sender.sendMessage("§cFailed to resume the game.");
        }
        
        return success;
    }
    
    private boolean handleStatus(CommandSender sender) {
        if (!sender.hasPermission("speedrunnerswap.command")) {
            sender.sendMessage("§cYou don't have permission to use this command.");
            return false;
        }
        
        sender.sendMessage("§6=== SpeedrunnerSwap Status ===");
        sender.sendMessage("§eGame Running: §f" + plugin.getGameManager().isGameRunning());
        sender.sendMessage("§eGame Paused: §f" + plugin.getGameManager().isGamePaused());
        
        if (plugin.getGameManager().isGameRunning()) {
            Player activeRunner = plugin.getGameManager().getActiveRunner();
            sender.sendMessage("§eActive Runner: §f" + (activeRunner != null ? activeRunner.getName() : "None"));
            sender.sendMessage("§eTime Until Next Swap: §f" + plugin.getGameManager().getTimeUntilNextSwap() + "s");
            
            List<Player> runners = plugin.getGameManager().getRunners();
            List<Player> hunters = plugin.getGameManager().getHunters();
            
            sender.sendMessage("§eRunners: §f" + runners.stream()
                    .map(Player::getName)
                    .collect(Collectors.joining(", ")));
            
            sender.sendMessage("§eHunters: §f" + hunters.stream()
                    .map(Player::getName)
                    .collect(Collectors.joining(", ")));
        }
        
        return true;
    }
    
    private boolean handleSetRunners(CommandSender sender, String[] playerNames) {
        if (!sender.hasPermission("speedrunnerswap.command")) {
            sender.sendMessage("§cYou don't have permission to use this command.");
            return false;
        }
        
        if (playerNames.length == 0) {
            sender.sendMessage("§cUsage: /swap setrunners <player1> [player2] [player3] ...");
            return false;
        }
        
        List<Player> players = new ArrayList<>();
        for (String name : playerNames) {
            Player player = Bukkit.getPlayerExact(name);
            if (player != null) {
                players.add(player);
            } else {
                sender.sendMessage("§cPlayer not found: " + name);
            }
        }
        
        if (players.isEmpty()) {
            sender.sendMessage("§cNo valid players specified.");
            return false;
        }
        
        plugin.getGameManager().setRunners(players);
        sender.sendMessage("§aRunners set: " + players.stream()
                .map(Player::getName)
                .collect(Collectors.joining(", ")));
        
        return true;
    }
    
    private boolean handleSetHunters(CommandSender sender, String[] playerNames) {
        if (!sender.hasPermission("speedrunnerswap.command")) {
            sender.sendMessage("§cYou don't have permission to use this command.");
            return false;
        }
        
        if (playerNames.length == 0) {
            sender.sendMessage("§cUsage: /swap sethunters <player1> [player2] [player3] ...");
            return false;
        }
        
        List<Player> players = new ArrayList<>();
        for (String name : playerNames) {
            Player player = Bukkit.getPlayerExact(name);
            if (player != null) {
                players.add(player);
            } else {
                sender.sendMessage("§cPlayer not found: " + name);
            }
        }
        
        if (players.isEmpty()) {
            sender.sendMessage("§cNo valid players specified.");
            return false;
        }
        
        plugin.getGameManager().setHunters(players);
        sender.sendMessage("§aHunters set: " + players.stream()
                .map(Player::getName)
                .collect(Collectors.joining(", ")));
        
        return true;
    }
    
    private boolean handleReload(CommandSender sender) {
        if (!sender.hasPermission("speedrunnerswap.admin")) {
            sender.sendMessage("§cYou don't have permission to use this command.");
            return false;
        }
        
        // Stop the game if it's running
        if (plugin.getGameManager().isGameRunning()) {
            plugin.getGameManager().stopGame();
        }
        
        // Reload the config
        plugin.getConfigManager().loadConfig();
        sender.sendMessage("§aConfiguration reloaded.");
        
        return true;
    }
    
    private boolean handleGui(CommandSender sender) {
        if (!sender.hasPermission("speedrunnerswap.command")) {
            sender.sendMessage("§cYou don't have permission to use this command.");
            return false;
        }
        
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cThis command can only be used by players.");
            return false;
        }
        
        plugin.getGuiManager().openMainMenu((Player) sender);
        return true;
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            // Subcommands
            List<String> subCommands = Arrays.asList("start", "stop", "pause", "resume", "status", "setrunners", "sethunters", "reload", "gui");
            for (String subCommand : subCommands) {
                if (subCommand.startsWith(args[0].toLowerCase())) {
                    completions.add(subCommand);
                }
            }
        } else if (args.length > 1) {
            // Player names for setrunners and sethunters
            if (args[0].equalsIgnoreCase("setrunners") || args[0].equalsIgnoreCase("sethunters")) {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    String name = player.getName();
                    if (name.toLowerCase().startsWith(args[args.length - 1].toLowerCase())) {
                        completions.add(name);
                    }
                }
            }
        }
        
        return completions;
    }
}