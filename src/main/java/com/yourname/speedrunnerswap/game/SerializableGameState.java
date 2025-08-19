package com.yourname.speedrunnerswap.game;

import com.yourname.speedrunnerswap.PlayerState;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class SerializableGameState {
    private List<UUID> runnerUuids;
    private List<String> hunterNames; // Storing names is safer for hunters
    private int activeRunnerIndex;
    private int secondsLeft;
    private Map<UUID, PlayerState> playerStates;
    private boolean isPaused;

    // Getters and Setters
    public List<UUID> getRunnerUuids() { return runnerUuids; }
    public void setRunnerUuids(List<UUID> runnerUuids) { this.runnerUuids = runnerUuids; }
    public List<String> getHunterNames() { return hunterNames; }
    public void setHunterNames(List<String> hunterNames) { this.hunterNames = hunterNames; }
    public int getActiveRunnerIndex() { return activeRunnerIndex; }
    public void setActiveRunnerIndex(int activeRunnerIndex) { this.activeRunnerIndex = activeRunnerIndex; }
    public int getSecondsLeft() { return secondsLeft; }
    public void setSecondsLeft(int secondsLeft) { this.secondsLeft = secondsLeft; }
    public Map<UUID, PlayerState> getPlayerStates() { return playerStates; }
    public void setPlayerStates(Map<UUID, PlayerState> playerStates) { this.playerStates = playerStates; }
    public boolean isPaused() { return isPaused; }
    public void setPaused(boolean paused) { isPaused = paused; }
}
