package com.mike101102.ctt;

public class PlayerStats {

    private String name;
    private int wins;
    private int losses;
    private int kills;
    private int deaths;

    /**
     * Only provide what has changed, everything else set to 0 for no change
     * 
     * @param name
     * @param wins
     * @param losses
     * @param kills
     * @param deaths
     */
    public PlayerStats(String name, int wins, int losses, int kills, int deaths) {
        this.name = name;
        this.wins = wins;
        this.losses = losses;
        this.kills = kills;
        this.deaths = deaths;
    }

    public String getName() {
        return name;
    }

    public int getWins() {
        return wins;
    }

    public int getLosses() {
        return losses;
    }

    public int getKills() {
        return kills;
    }

    public int getDeaths() {
        return deaths;
    }
}
