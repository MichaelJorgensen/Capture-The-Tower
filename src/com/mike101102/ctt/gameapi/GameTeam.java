package com.mike101102.ctt.gameapi;

import java.util.ArrayList;
import java.util.UUID;

public class GameTeam {

    private String name;
    private final ArrayList<UUID> teamPlayers = new ArrayList<UUID>();

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
     * @return ArrayList<UUID>
     */
    public ArrayList<UUID> getPlayers() {
        return teamPlayers;
    }

    /**
     * Adds the player to the list
     * 
     * @param player name that should be added
     */
    public void addPlayerToList(UUID player) {
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
