package com.mike101102.ctt.gameapi.events.sign;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

import com.mike101102.ctt.gameapi.Game;
import com.mike101102.ctt.gameapi.events.CallableEvent;
import com.mike101102.ctt.gameapi.events.GameEvent;
import com.mike101102.ctt.gameapi.events.SignEvent;

public class GameSignUpdateEvent extends GameEvent implements CallableEvent, Cancellable, SignEvent {

    private Location signLoc;
    private boolean cancelled = false;
    private static final HandlerList handlers = new HandlerList();

    public GameSignUpdateEvent(Game game, Location signLoc) {
        super(game);
    }

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public Location getSignLocation() {
        return signLoc;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    public void call() {
        Bukkit.getPluginManager().callEvent(this);
    }
}
