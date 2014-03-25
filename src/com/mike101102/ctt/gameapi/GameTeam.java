package com.mike101102.ctt.gameapi;

import java.util.ArrayList;

public class GameTeam {

    private String name;
    private final ArrayList<String> teamPlayers = new ArrayList<String>();

    /**
     * Represents a Game Team, the player list will be created (not null) and
     * will be empty
     * 
     * @param name of the team
     */
    public GameTeam(String name) {
        this.name = name;
    }

    /**
     * Returns the name of the team
     * 
     * @return String
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the list of the player names on this team
     * 
     * @return ArrayList<String>
     */
    public ArrayList<String> getPlayers() {
        return teamPlayers;
    }

    /**
     * Adds the player to the list
     * 
     * @param player name that should be added
     */
    public void addPlayerToList(String player) {
        this.teamPlayers.add(player);
    }

    /**
     * Removes the player from the team's list
     * 
     * @param player name that should be removed
     * @return true if the player was on the list and was removed, otherwise
     *         false
     */
    public boolean removePlayerFromList(String player) {
        return this.teamPlayers.remove(player);
    }
}
