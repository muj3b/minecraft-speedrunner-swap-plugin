package com.yourname.speedrunnerswap.game;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class SerializableGameState {
    private List<UUID> runnerUuids;
    private List<UUID> hunterUuids;
    private int activeRunnerIndex;
    private long remainingSwapTime;
    private Map<UUID, PlayerState> playerStates;
    private boolean isPaused;

    // Getters and setters

    public List<UUID> getRunnerUuids() {
        return runnerUuids;
    }

    public void setRunnerUuids(List<UUID> runnerUuids) {
        this.runnerUuids = runnerUuids;
    }

    public List<UUID> getHunterUuids() {
        return hunterUuids;
    }

    public void setHunterUuids(List<UUID> hunterUuids) {
        this.hunterUuids = hunterUuids;
    }

    public int getActiveRunnerIndex() {
        return activeRunnerIndex;
    }

    public void setActiveRunnerIndex(int activeRunnerIndex) {
        this.activeRunnerIndex = activeRunnerIndex;
    }

    public long getRemainingSwapTime() {
        return remainingSwapTime;
    }

    public void setRemainingSwapTime(long remainingSwapTime) {
        this.remainingSwapTime = remainingSwapTime;
    }

    public Map<UUID, PlayerState> getPlayerStates() {
        return playerStates;
    }

    public void setPlayerStates(Map<UUID, PlayerState> playerStates) {
        this.playerStates = playerStates;
    }

    public boolean isPaused() {
        return isPaused;
    }

    public void setPaused(boolean isPaused) {
        this.isPaused = isPaused;
    }
}
