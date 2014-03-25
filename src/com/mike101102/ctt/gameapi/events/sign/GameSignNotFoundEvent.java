package com.mike101102.ctt.gameapi.events.sign;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.event.HandlerList;

import com.mike101102.ctt.gameapi.Game;
import com.mike101102.ctt.gameapi.events.CallableEvent;
import com.mike101102.ctt.gameapi.events.GameEvent;
import com.mike101102.ctt.gameapi.events.SignEvent;

public class GameSignNotFoundEvent extends GameEvent implements CallableEvent, SignEvent {

    private Location signLoc;
    private static final HandlerList handlers = new HandlerList();

    /**
     * Called when a game goes to update its sign and it's not found i.e. the
     * sign has been destroyed
     * 
     * @param game the sign was assigned to
     * @param signLoc the location of where the sign should be
     */
    public GameSignNotFoundEvent(Game game, Location signLoc) {
        super(game);
        this.signLoc = signLoc;
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

    public void call() {
        Bukkit.getPluginManager().callEvent(this);
    }
}
