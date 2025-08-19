package com.yourname.speedrunnerswap;

import com.yourname.speedrunnerswap.SpeedrunnerSwap;
import com.yourname.speedrunnerswap.SwapManager;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public class BlackoutUI {
    private final SpeedrunnerSwap plugin;
    private final Player viewer;
    private Player activeRunner;
    private final boolean showStatus;
    private BukkitRunnable task;

    public BlackoutUI(SpeedrunnerSwap plugin, Player viewer, Player activeRunner, int secondsLeft, boolean showStatus) {
        this.plugin = plugin;
        this.viewer = viewer;
        this.activeRunner = activeRunner;
        this.showStatus = showStatus;
        task = new BukkitRunnable() {
            int left = Math.max(0, secondsLeft);
            @Override public void run() {
                if (!viewer.isOnline() || left < 0) { stop(); cancel(); return; }
                if (!viewer.hasPotionEffect(PotionEffectType.BLINDNESS)) viewer.addPotionEffect(SwapManager.BLINDNESS);
                String msg = "Swap in " + left + "s";
                if (showStatus && activeRunner != null && activeRunner.isOnline()) {
                    String sneaking = activeRunner.isSneaking() ? "Sneak: Yes" : "Sneak: No";
                    String sprint = activeRunner.isSprinting() ? "Sprint: Yes" : "Sprint: No";
                    msg += "  |  " + sneaking + "  |  " + sprint;
                }
                viewer.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(msg));
                left--;
            }
        };
    }

    public void start() { task.runTaskTimer(plugin, 0L, 20L); }
    public void stop() {
        if (task != null && !task.isCancelled()) task.cancel();
        if (viewer.isOnline()) viewer.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(" "));
    }
    public void updateActiveRunner(Player current) { this.activeRunner = current; }
}
