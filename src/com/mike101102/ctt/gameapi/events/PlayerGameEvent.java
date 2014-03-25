package com.mike101102.ctt.gameapi.events;

import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerEvent;

import com.mike101102.ctt.gameapi.Game;

public abstract class PlayerGameEvent extends PlayerEvent {

    private Game game;

    public PlayerGameEvent(Game game, Player player) {
        super(player);
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
