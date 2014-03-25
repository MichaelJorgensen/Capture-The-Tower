package com.mike101102.ctt.gameapi;

import org.bukkit.ChatColor;

public enum GameStage {

    /**
     * If the game is waiting for players (inactive) or if the game is waiting
     * for more players (active)
     */
    Waiting,

    /**
     * If the game is currently in its lobby. Not all games have lobbies
     */
    Lobby,

    /**
     * If the game is waiting for something before starting the actual game.
     * PreGame and Waiting are very similar.
     */
    PreGame,

    /**
     * Used only when the game is active and players are actually playing. Never
     * use when inactive
     */
    Ingame;

    public static GameStage getFrom(String args) {
        switch (args.toLowerCase()) {
        case "waiting":
            return GameStage.Waiting;
        case "lobby":
            return GameStage.Lobby;
        case "pregame":
            return GameStage.PreGame;
        case "ingame":
            return GameStage.Ingame;
        default:
            return GameStage.Lobby;
        }
    }

    @Override
    public String toString() {
        switch (this) {
        case Waiting:
            return ChatColor.DARK_GREEN + "Waiting";
        case PreGame:
            return ChatColor.GOLD + "PreGame";
        case Ingame:
            return ChatColor.DARK_RED + "Ingame";
        case Lobby:
            return ChatColor.DARK_GREEN + "Lobby";
        default:
            return null;
        }
    }
}
