package com.mike101102.ctt.gameapi.events.game;

import org.bukkit.Bukkit;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

import com.mike101102.ctt.gameapi.Game;
import com.mike101102.ctt.gameapi.events.CallableEvent;
import com.mike101102.ctt.gameapi.events.GameEvent;

public class GameRunnerRemoveEvent extends GameEvent implements CallableEvent, Cancellable {

    private boolean cancelled = false;
    private static final HandlerList handlers = new HandlerList();

    /**
     * Called when a game is removed from the running list of games
     * 
     * @param game to be removed
     */
    public GameRunnerRemoveEvent(Game game) {
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

    @Override
    public void call() {
        Bukkit.getPluginManager().callEvent(this);
    }
}
