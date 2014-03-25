package com.mike101102.ctt.gameapi;

import java.util.ArrayList;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public interface GameAPI {

    /**
     * Called every set amount of ticks (period) when the game is not running
     */
    public void idleRun();

    /**
     * Called every set amount of ticks (period) when the game is running
     */
    public void gameRun();

    /**
     * This method is called when a player attempts to join your game <b>If
     * successful, add player to the player list</b>
     */
    public void addPlayer(Player player);

    /**
     * This method is called when a player attempts to leave your game <b>If
     * successful, remove the player from the player list</b>
     */
    public void removePlayer(Player player);

    /**
     * Called when the game's sign changes location so you can update it on a
     * permanent file
     * 
     * @param newLocation
     */
    public void updateSign(Location newLocation);

    /**
     * Updates the game's sign in-game with the current game's values
     */
    public void updateGameSign();

    /**
     * Gets the unique ID of the game
     * 
     * @return int the game's ID
     */
    public int getGameId();

    /**
     * Returns true if the game is running. The game will be running if there
     * are players in it, if not, then it will return false
     * 
     * @return boolean
     */
    public boolean isRunning();

    /**
     * Sets if the game is running or not. This is done automatically in the
     * game class
     * 
     * @param running true if the game is running
     */
    public void setRunning(boolean running);

    /**
     * Gets an array list of the team's spawns
     * 
     * @return ArrayList<Location>
     */
    public ArrayList<Location> getTeamSpawns();

    /**
     * Gets the list of queued players
     * 
     * @deprecated Currently the GameAPI does nothing but store queued players
     * @return ArrayList<String>
     */
    @Deprecated
    public ArrayList<String> getQueuedPlayers();

    /**
     * Gets the maximum amount of players
     * 
     * @return int max players
     */
    public int getMaxPlayers();

    /**
     * Gets the current game stage
     * 
     * @return GameStage
     */
    public GameStage getGameStage();

    /**
     * Sets the game's game stage
     * 
     * @param gamestage to set the game to
     */
    public void setGameStage(GameStage gamestage);

    /**
     * Sets the maximum amount of players
     * 
     * @param maxPlayers
     */
    public void setMaxPlayers(int maxPlayers);

    /**
     * Gets the name of the game. Will be displayed on the game's sign
     * automatically
     * 
     * @return String
     */
    public String getName();

    /**
     * Gets the list of players
     * 
     * @return ArrayList<String>
     */
    public ArrayList<String> getPlayers();

    /**
     * Sets the game signs location
     * 
     * @param signLoc the location of the sign
     */
    public void setSignLocation(Location signLoc);

    /**
     * Gets the location of the game's sign
     * 
     * @return Location
     */
    public Location getSignLocation();

    /**
     * Shuts down the game by stopping its task and kicking all players out of
     * the game
     */
    public void shutdown();

    /**
     * Sends the given message to all the players in the game Uses the set
     * game's name as the prefix
     * 
     * @param message that you want delivered
     */
    public void sendGameMessage(String message);

    /**
     * Sends the given message to the given team Uses the set game's name as the
     * prefix
     * 
     * @param team that should receive the message
     * @param message that you want delivered
     */
    public void sendTeamMessage(GameTeam team, String message);
}
