package com.yourname.speedrunnerswap.util.gson;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import org.bukkit.inventory.ItemStack;

import java.io.IOException;
import java.util.Base64;

public class ItemStackAdapter extends TypeAdapter<ItemStack> {

    @Override
    public void write(JsonWriter out, ItemStack itemStack) throws IOException {
        if (itemStack == null) {
            out.nullValue();
            return;
        }
        byte[] serialized = itemStack.serializeAsBytes();
        out.value(Base64.getEncoder().encodeToString(serialized));
    }

    @Override
    public ItemStack read(JsonReader in) throws IOException {
        if (in.peek() == JsonToken.NULL) {
            in.nextNull();
            return null;
        }
        byte[] decoded = Base64.getDecoder().decode(in.nextString());
        return ItemStack.deserializeBytes(decoded);
    }
}
