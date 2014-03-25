package com.mike101102.ctt.gameapi.events;

import org.bukkit.event.Event;

import com.mike101102.ctt.gameapi.Game;

public abstract class GameEvent extends Event {

    private Game game;

    public GameEvent(Game game) {
        this.game = game;
    }

    /**
     * Gets the game involved in the event
     * 
     * @return Game
     */
    public final Game getGame() {
        return game;
    }
}
