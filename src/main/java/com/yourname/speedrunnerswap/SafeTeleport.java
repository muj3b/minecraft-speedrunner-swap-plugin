package com.yourname.speedrunnerswap;

import org.bukkit.Location;
import org.bukkit.Material;

public class SafeTeleport {
    public static Location findSafe(Location base, int vLimit, int hRadius) {
        if (base == null || base.getWorld() == null) return null;
        Location c = verticalScan(base.clone(), vLimit);
        if (isSafe(c)) return c;
        for (int r = 1; r <= hRadius; r++) {
            for (int dx = -r; dx <= r; dx++) {
                for (int dz = -r; dz <= r; dz++) {
                    if (Math.abs(dx) != r && Math.abs(dz) != r) continue;
                    Location here = base.clone().add(dx, 0, dz);
                    here = verticalScan(here, vLimit);
                    if (isSafe(here)) return here;
                }
            }
        }
        return base;
    }

    private static Location verticalScan(Location loc, int limit) {
        Location c = loc.clone();
        for (int i = 0; i <= limit; i++) { if (isSafe(c)) return c; c.add(0, 1, 0); }
        c = loc.clone();
        for (int i = 0; i <= limit; i++) { if (isSafe(c)) return c; c.add(0, -1, 0); }
        return loc;
    }

    private static boolean isSafe(Location l) {
        if (l == null || l.getWorld() == null) return false;
        Material feet = l.getBlock().getType();
        Material head = l.clone().add(0, 1, 0).getBlock().getType();
        Material below = l.clone().add(0, -1, 0).getBlock().getType();
        return isNonSolid(feet) && isNonSolid(head) && below.isSolid() && !isDanger(below);
    }

    private static boolean isNonSolid(Material m) { return !m.isSolid() || m == Material.AIR || m.isTransparent(); }
    private static boolean isDanger(Material m) { return m == Material.LAVA || m == Material.CACTUS || m == Material.MAGMA_BLOCK; }
}
