package com.yourname.speedrunnerswap.game;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.yourname.speedrunnerswap.PlayerState;
import com.yourname.speedrunnerswap.SpeedrunnerSwap;
import com.yourname.speedrunnerswap.SwapManager;
import com.yourname.speedrunnerswap.util.gson.ItemStackAdapter;
import com.yourname.speedrunnerswap.util.gson.LocationAdapter;
import com.yourname.speedrunnerswap.util.gson.PotionEffectAdapter;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

public class GameStateManager {

    private final SpeedrunnerSwap plugin;
    private final Gson gson;
    private final File stateFile;

    public GameStateManager(SpeedrunnerSwap plugin) {
        this.plugin = plugin;
        this.stateFile = new File(plugin.getDataFolder(), "game_state.json");

        this.gson = new GsonBuilder()
                .registerTypeAdapter(Location.class, new LocationAdapter())
                .registerTypeAdapter(ItemStack.class, new ItemStackAdapter())
                .registerTypeAdapter(PotionEffect.class, new PotionEffectAdapter())
                .enableComplexMapKeySerialization()
                .setPrettyPrinting()
                .create();
    }

    public void saveState(SwapManager swapManager) {
        if (!swapManager.isRunning()) {
            if (stateFile.exists()) {
                stateFile.delete();
            }
            return;
        }

        SerializableGameState state = new SerializableGameState();
        state.setRunnerUuids(swapManager.getRunnerUuids());
        state.setHunterNames(swapManager.getHunterNames());
        state.setActiveRunnerIndex(swapManager.getActiveIndex());
        state.setSecondsLeft(swapManager.getSecondsLeft());
        state.setPlayerStates(swapManager.getSharedPlayerStates());
        state.setPaused(swapManager.isPaused());

        try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(stateFile), StandardCharsets.UTF_8))) {
            gson.toJson(state, writer);
            plugin.getLogger().info("Game state saved.");
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save game state: " + e.getMessage());
        }
    }

    public SerializableGameState loadState() {
        if (!stateFile.exists()) {
            return null;
        }
        try (Reader reader = new BufferedReader(new InputStreamReader(new FileInputStream(stateFile), StandardCharsets.UTF_8))) {
            SerializableGameState state = gson.fromJson(reader, SerializableGameState.class);
            plugin.getLogger().info("Game state loaded from file.");
            return state;
        } catch (IOException e) {
            plugin.getLogger().severe("Could not load game state: " + e.getMessage());
            return null;
        }
    }

    public boolean hasSavedState() {
        return stateFile.exists() && stateFile.length() > 0;
    }
}
