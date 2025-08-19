package com.yourname.speedrunnerswap.util.gson;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.bukkit.util.Vector;

import java.io.IOException;

public class VectorAdapter extends TypeAdapter<Vector> {

    @Override
    public void write(JsonWriter out, Vector vector) throws IOException {
        if (vector == null) {
            out.nullValue();
            return;
        }
        out.beginObject();
        out.name("x").value(vector.getX());
        out.name("y").value(vector.getY());
        out.name("z").value(vector.getZ());
        out.endObject();
    }

    @Override
    public Vector read(JsonReader in) throws IOException {
        in.beginObject();
        double x = 0, y = 0, z = 0;
        while (in.hasNext()) {
            switch (in.nextName()) {
                case "x":
                    x = in.nextDouble();
                    break;
                case "y":
                    y = in.nextDouble();
                    break;
                case "z":
                    z = in.nextDouble();
                    break;
            }
        }
        in.endObject();
        return new Vector(x, y, z);
    }
}
