package com.yourname.speedrunnerswap.util.gson;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.io.IOException;

public class PotionEffectAdapter extends TypeAdapter<PotionEffect> {

    @Override
    public void write(JsonWriter out, PotionEffect effect) throws IOException {
        if (effect == null) {
            out.nullValue();
            return;
        }
        out.beginObject();
        out.name("type").value(effect.getType().getName());
        out.name("duration").value(effect.getDuration());
        out.name("amplifier").value(effect.getAmplifier());
        out.name("ambient").value(effect.isAmbient());
        out.name("particles").value(effect.hasParticles());
        out.name("icon").value(effect.hasIcon());
        out.endObject();
    }

    @Override
    public PotionEffect read(JsonReader in) throws IOException {
        in.beginObject();
        PotionEffectType type = null;
        int duration = 0;
        int amplifier = 0;
        boolean ambient = false;
        boolean particles = true;
        boolean icon = true;

        while (in.hasNext()) {
            switch (in.nextName()) {
                case "type":
                    type = PotionEffectType.getByName(in.nextString());
                    break;
                case "duration":
                    duration = in.nextInt();
                    break;
                case "amplifier":
                    amplifier = in.nextInt();
                    break;
                case "ambient":
                    ambient = in.nextBoolean();
                    break;
                case "particles":
                    particles = in.nextBoolean();
                    break;
                case "icon":
                    icon = in.nextBoolean();
                    break;
            }
        }
        in.endObject();

        if (type != null) {
            return new PotionEffect(type, duration, amplifier, ambient, particles, icon);
        }
        return null;
    }
}
