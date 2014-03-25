package com.mike101102.ctt.gameapi.events.player;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

import com.mike101102.ctt.gameapi.Game;
import com.mike101102.ctt.gameapi.events.CallableEvent;
import com.mike101102.ctt.gameapi.events.PlayerGameEvent;

public class PlayerJoinGameEvent extends PlayerGameEvent implements CallableEvent, Cancellable {

    private boolean cancelled = false;
    private static final HandlerList handlers = new HandlerList();

    public PlayerJoinGameEvent(Game game, Player player) {
        super(game, player);
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
