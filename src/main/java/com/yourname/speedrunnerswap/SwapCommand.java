package com.yourname.speedrunnerswap;

import com.yourname.speedrunnerswap.SpeedrunnerSwap;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import java.util.Arrays;
import java.util.List;

public class SwapCommand implements CommandExecutor {
    private final SpeedrunnerSwap plugin;

    public SwapCommand(SpeedrunnerSwap plugin) { this.plugin = plugin; }

    @Override public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("speedrunnerswap.admin")) {
            sender.sendMessage(ChatColor.RED + "No permission.");
            return true;
        }
        if (args.length == 0) {
            sender.sendMessage(ChatColor.YELLOW + "/swap <start|stop|pause|resume|status|reload|gui>");
            return true;
        }
        String sub = args[0].toLowerCase();

        switch (sub) {
            case "start": plugin.manager().start(); break;
            case "stop":  plugin.manager().stop();  break;
            case "pause": plugin.manager().pause(); break;
            case "resume":plugin.manager().resume();break;
            case "status":
                sender.sendMessage(ChatColor.AQUA + "Running: " + plugin.manager().isRunning());
                break;
            case "setrunners":
                if (args.length < 2) sender.sendMessage(ChatColor.RED + "Usage: /swap setrunners <name1> [name2 ...]");
                else {
                    List<String> list = Arrays.asList(Arrays.copyOfRange(args, 1, args.length));
                    plugin.getConfig().set("runners", list);
                    plugin.saveConfig();
                    sender.sendMessage(ChatColor.GREEN + "Runners set: " + String.join(", ", list));
                }
                break;
            case "sethunters":
                if (args.length < 2) sender.sendMessage(ChatColor.RED + "Usage: /swap sethunters <name1> [name2] ...");
                else {
                    List<String> list = Arrays.asList(Arrays.copyOfRange(args, 1, args.length));
                    plugin.getConfig().set("hunters", list);
                    plugin.saveConfig();
                    sender.sendMessage(ChatColor.GREEN + "Hunters set: " + String.join(", ", list));
                }
                break;
            case "reload":
                plugin.reloadAndRebuild();
                sender.sendMessage(ChatColor.GREEN + "SpeedrunnerSwap reloaded.");
                break;
            case "gui":
                if (!plugin.getConfig().getBoolean("gui.enabled", true)) {
                    sender.sendMessage(ChatColor.RED + "GUI disabled in config.");
                } else if (sender instanceof org.bukkit.entity.Player p) {
                    plugin.guiHub().open(p);
                } else sender.sendMessage(ChatColor.RED + "Players only.");
                break;
            default:
                sender.sendMessage(ChatColor.RED + "Unknown subcommand.");
        }
        return true;
    }
}
