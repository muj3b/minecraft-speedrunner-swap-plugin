package com.yourname.speedrunnerswap;

import com.yourname.speedrunnerswap.SwapManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class FreezeGuard implements Listener {
    private final SwapManager manager;
    public FreezeGuard(SwapManager manager) { this.manager = manager; }

    private boolean isFrozen(Player p) { return manager.isFrozen(p.getUniqueId()); }

    @EventHandler public void onMove(PlayerMoveEvent e) {
        if (manager.cancelMovement() && isFrozen(e.getPlayer()) && e.hasChangedPosition()) {
            e.setTo(e.getFrom());
        }
    }
    @EventHandler public void onInteract(PlayerInteractEvent e) { if (manager.cancelInteractions() && isFrozen(e.getPlayer())) e.setCancelled(true); }
    @EventHandler public void onDrop(PlayerDropItemEvent e) { if (isFrozen(e.getPlayer())) e.setCancelled(true); }
    @EventHandler public void onPickup(EntityPickupItemEvent e) { if (e.getEntity() instanceof Player p && isFrozen(p)) e.setCancelled(true); }
    @EventHandler public void onClick(InventoryClickEvent e) { if (e.getWhoClicked() instanceof Player p && isFrozen(p)) e.setCancelled(true); }
    @EventHandler public void onBreak(BlockBreakEvent e) { if (isFrozen(e.getPlayer())) e.setCancelled(true); }
    @EventHandler public void onPlace(BlockPlaceEvent e) { if (isFrozen(e.getPlayer())) e.setCancelled(true); }
    @EventHandler public void onDamage(EntityDamageByEntityEvent e) { if (e.getDamager() instanceof Player p && isFrozen(p)) e.setCancelled(true); }
    @EventHandler public void onCommand(PlayerCommandPreprocessEvent e) {
        if (isFrozen(e.getPlayer()) && !e.getMessage().toLowerCase().startsWith("/swap")) {
            e.setCancelled(true);
        }
    }
}
