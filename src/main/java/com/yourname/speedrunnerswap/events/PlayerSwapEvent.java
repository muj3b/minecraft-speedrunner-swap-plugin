package com.yourname.speedrunnerswap.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class PlayerSwapEvent extends Event {

    private static final HandlerList handlers = new HandlerList();
    private final Player previousRunner;
    private final Player newRunner;

    public PlayerSwapEvent(Player previousRunner, Player newRunner) {
        this.previousRunner = previousRunner;
        this.newRunner = newRunner;
    }

    public Player getPreviousRunner() {
        return previousRunner;
    }

    public Player getNewRunner() {
        return newRunner;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
