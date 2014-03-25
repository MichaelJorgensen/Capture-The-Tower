package com.mike101102.ctt.gameapi.events.game;

import org.bukkit.Bukkit;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

import com.mike101102.ctt.gameapi.Game;
import com.mike101102.ctt.gameapi.events.CallableEvent;
import com.mike101102.ctt.gameapi.events.GameEvent;

public class GameRunningChangeEvent extends GameEvent implements CallableEvent, Cancellable {

    private boolean newRunState;
    private boolean cancelled = false;
    private static final HandlerList handlers = new HandlerList();

    /**
     * Called when a game's running state is changed
     * 
     * @param game that has a new running state
     * @param newRunState the new running state
     */
    public GameRunningChangeEvent(Game game, boolean newRunState) {
        super(game);
        this.newRunState = newRunState;
    }

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public boolean getNewRunState() {
        return newRunState;
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
