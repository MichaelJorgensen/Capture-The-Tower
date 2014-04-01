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
    
    /**
     * Own equals method
     */
    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        else if (!(o instanceof PlayerStats)) {
            return false;
        }
        PlayerStats s = (PlayerStats) o;
        if (s.getName().equalsIgnoreCase(name) && s.getWins() == wins && s.getLosses() == losses && s.getKills() == kills && s.getDeaths() == deaths) {
            return true;
        } else {
            return false;
        }
    }

    public String getName() {
        return name;
    }

    public int getWins() {
        return wins;
    }
    
    public void setWins(int wins) {
        this.wins = wins;
    }

    public int getLosses() {
        return losses;
    }
    
    public void setLosses(int losses) {
        this.losses = losses;
    }

    public int getKills() {
        return kills;
    }
    
    public void setKills(int kills) {
        this.kills = kills;
    }

    public int getDeaths() {
        return deaths;
    }
    
    public void setDeaths(int deaths) {
        this.deaths = deaths;
    }
}
