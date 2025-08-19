package com.yourname.speedrunnerswap.game;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.yourname.speedrunnerswap.SpeedrunnerSwap;
import com.yourname.speedrunnerswap.util.gson.ItemStackAdapter;
import com.yourname.speedrunnerswap.util.gson.LocationAdapter;
import com.yourname.speedrunnerswap.util.gson.PotionEffectAdapter;
import com.yourname.speedrunnerswap.util.gson.VectorAdapter;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.util.Vector;
import org.bukkit.entity.Player;


import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;
import java.util.UUID;

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
                .registerTypeAdapter(Vector.class, new VectorAdapter())
                .setPrettyPrinting()
                .create();
    }

    public void saveState(GameManager gameManager) {
        if (!gameManager.isGameRunning()) {
            // If a game is not running, ensure no old state file lingers.
            if (stateFile.exists()) {
                stateFile.delete();
            }
            return;
        }

        SerializableGameState state = new SerializableGameState();
        state.setRunnerUuids(gameManager.getRunners().stream().map(Player::getUniqueId).collect(Collectors.toList()));
        state.setHunterUuids(gameManager.getHunters().stream().map(Player::getUniqueId).collect(Collectors.toList()));
        state.setActiveRunnerIndex(gameManager.getActiveRunnerIndex());
        state.setRemainingSwapTime(gameManager.getRemainingSwapTimeMillis());
        state.setPlayerStates(gameManager.getPlayerStates());
        state.setPaused(gameManager.isGamePaused());


        try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(stateFile), StandardCharsets.UTF_8))) {
            gson.toJson(state, writer);
            plugin.getLogger().info("Game state saved.");
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save game state to " + stateFile.getName());
            e.printStackTrace();
        }
    }

    public SerializableGameState loadState() {
        if (!stateFile.exists()) {
            return null;
        }

        try (Reader reader = new BufferedReader(new InputStreamReader(new FileInputStream(stateFile), StandardCharsets.UTF_8))) {
            SerializableGameState state = gson.fromJson(reader, SerializableGameState.class);
            plugin.getLogger().info("Game state loaded.");
            return state;
        } catch (IOException e) {
            plugin.getLogger().severe("Could not load game state from " + stateFile.getName());
            e.printStackTrace();
            return null;
        }
    }

    public boolean hasSavedState() {
        return stateFile.exists();
    }
}
