package com.mike101102.ctt.gameapi.events.player;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.inventory.meta.BookMeta;

import com.mike101102.ctt.gameapi.events.CallableEvent;

public class PlayerGameListUpdateEvent extends PlayerEvent implements CallableEvent, Cancellable {

    private BookMeta currentBookMeta;
    private BookMeta newBookMeta;
    private boolean cancelled = false;
    private static final HandlerList handlers = new HandlerList();

    /**
     * Called when the list of games is updated for/to a player
     * 
     * @param player that is receiving the update
     */
    public PlayerGameListUpdateEvent(Player player, BookMeta currentBookMeta, BookMeta newBookMeta) {
        super(player);
        this.currentBookMeta = currentBookMeta;
        this.newBookMeta = newBookMeta;
    }

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public BookMeta getCurrentBookMeta() {
        return currentBookMeta;
    }

    public BookMeta getNewBookMeta() {
        return newBookMeta;
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
