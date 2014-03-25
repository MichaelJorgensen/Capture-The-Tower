package com.mike101102.ctt.gameapi.events.game;

import org.bukkit.Bukkit;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

import com.mike101102.ctt.gameapi.Game;
import com.mike101102.ctt.gameapi.GameStage;
import com.mike101102.ctt.gameapi.events.CallableEvent;
import com.mike101102.ctt.gameapi.events.GameEvent;

public class GameStageChangeEvent extends GameEvent implements CallableEvent, Cancellable {

    private GameStage newGameStage;
    private boolean cancelled = false;
    private static final HandlerList handlers = new HandlerList();

    /**
     * Called when a game's game stage changes
     * 
     * @param game that has a new game stage
     * @param newGameStage the new game stage
     */
    public GameStageChangeEvent(Game game, GameStage newGameStage) {
        super(game);
        this.newGameStage = newGameStage;
    }

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public GameStage getNewGameStage() {
        return newGameStage;
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
