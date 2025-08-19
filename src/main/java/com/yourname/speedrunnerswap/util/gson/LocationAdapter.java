package com.yourname.speedrunnerswap.util.gson;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.io.IOException;
import java.util.UUID;

public class LocationAdapter extends TypeAdapter<Location> {

    @Override
    public void write(JsonWriter out, Location location) throws IOException {
        if (location == null) {
            out.nullValue();
            return;
        }
        out.beginObject();
        out.name("world").value(location.getWorld().getUID().toString());
        out.name("x").value(location.getX());
        out.name("y").value(location.getY());
        out.name("z").value(location.getZ());
        out.name("yaw").value(location.getYaw());
        out.name("pitch").value(location.getPitch());
        out.endObject();
    }

    @Override
    public Location read(JsonReader in) throws IOException {
        in.beginObject();
        World world = null;
        double x = 0, y = 0, z = 0;
        float yaw = 0, pitch = 0;
        while (in.hasNext()) {
            switch (in.nextName()) {
                case "world":
                    world = Bukkit.getWorld(UUID.fromString(in.nextString()));
                    break;
                case "x":
                    x = in.nextDouble();
                    break;
                case "y":
                    y = in.nextDouble();
                    break;
                case "z":
                    z = in.nextDouble();
                    break;
                case "yaw":
                    yaw = (float) in.nextDouble();
                    break;
                case "pitch":
                    pitch = (float) in.nextDouble();
                    break;
            }
        }
        in.endObject();
        return new Location(world, x, y, z, yaw, pitch);
    }
}
