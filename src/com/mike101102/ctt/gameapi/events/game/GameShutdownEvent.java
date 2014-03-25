package com.mike101102.ctt.gameapi.events.game;

import org.bukkit.Bukkit;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

import com.mike101102.ctt.gameapi.Game;
import com.mike101102.ctt.gameapi.events.CallableEvent;
import com.mike101102.ctt.gameapi.events.GameEvent;

public class GameShutdownEvent extends GameEvent implements CallableEvent, Cancellable {

    private boolean cancelled = false;
    private static final HandlerList handlers = new HandlerList();

    /**
     * Called when a game is shutdown, usually during a reload or shutdown of
     * the server or if the game is deleted
     * 
     * @param game that is being shutdown
     */
    public GameShutdownEvent(Game game) {
        super(game);
    }

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    public void call() {
        Bukkit.getPluginManager().callEvent(this);
    }
}
