package com.yourname.speedrunnerswap;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import java.util.ArrayList;
import java.util.Collection;

public class PlayerState {
    private final ItemStack[] inventory, armor;
    private final ItemStack offhand;
    private final double health, absorption;
    private final int foodLevel, fireTicks, air, totalExp;
    private final float saturation;
    private final Location location;
    private final Collection<PotionEffect> effects;

    public PlayerState(Player p) {
        this.inventory = cloneItems(p.getInventory().getContents());
        this.armor = cloneItems(p.getInventory().getArmorContents());
        this.offhand = p.getInventory().getItemInOffHand() != null ? p.getInventory().getItemInOffHand().clone() : null;
        this.health = p.getHealth();
        this.absorption = p.getAbsorptionAmount();
        this.foodLevel = p.getFoodLevel();
        this.saturation = p.getSaturation();
        this.totalExp = p.getTotalExperience();
        this.location = p.getLocation().clone();
        this.fireTicks = p.getFireTicks();
        this.air = p.getRemainingAir();
        this.effects = new ArrayList<>(p.getActivePotionEffects());
    }

    public void apply(Player p) {
        p.getInventory().setContents(cloneItems(inventory));
        p.getInventory().setArmorContents(cloneItems(armor));
        p.getInventory().setItemInOffHand(offhand != null ? offhand.clone() : null);
        p.setHealth(Math.min(health, p.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue()));
        p.setAbsorptionAmount(absorption);
        p.setFoodLevel(foodLevel);
        p.setSaturation(saturation);
        p.setTotalExperience(totalExp);
        p.teleport(location);
        p.setFireTicks(fireTicks);
        p.setRemainingAir(air);
        p.getActivePotionEffects().forEach(e -> p.removePotionEffect(e.getType()));
        p.addPotionEffects(effects);
        p.setGameMode(GameMode.SURVIVAL);
    }

    private ItemStack[] cloneItems(ItemStack[] items) {
        if (items == null) return null;
        ItemStack[] clone = new ItemStack[items.length];
        for (int i = 0; i < items.length; i++) {
            if (items[i] != null) clone[i] = items[i].clone();
        }
        return clone;
    }

    public Location getLocation() {
        return location;
    }
}
