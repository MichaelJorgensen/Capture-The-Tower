package com.mike101102.ctt;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import org.bukkit.scheduler.BukkitRunnable;

import com.mike101102.ctt.gameapi.sql.SQL;

public class StatsUpdater extends BukkitRunnable {

    private CTT plugin;
    private SQL sql;
    private ArrayList<PlayerStats> pt;
    private boolean stats;

    public StatsUpdater(CTT plugin) {
        this.plugin = plugin;
        stats = plugin.stats();
    }

    @SuppressWarnings("unchecked")
    @Override
    public void run() {
        if (!stats)
            return;
        sql = plugin.getSQL();
        pt = (ArrayList<PlayerStats>) plugin.getPlayerStatsToBeUpdated().clone();
        plugin.getPlayerStatsToBeUpdated().clear();
        CTT.debug("Amount of stats to update: " + pt.size());
        ResultSet rs;
        for (PlayerStats s : pt) {
            CTT.debug("Updating stats for " + s.getName());
            try {
                rs = sql.query("SELECT * FROM ctt_stats WHERE player='" + s.getName() + "'");
                if (rs.next()) {
                    int wins = rs.getInt("wins") + s.getWins();
                    int losses = rs.getInt("losses") + s.getLosses();
                    int kills = rs.getInt("kills") + s.getKills();
                    int deaths = rs.getInt("deaths") + s.getDeaths();
                    sql.query("UPDATE ctt_stats SET wins=" + wins + ", losses=" + losses + ", kills=" + kills + ", deaths=" + deaths + " WHERE player='" + s.getName() + "'");
                } else {
                    sql.query("INSERT INTO ctt_stats (player, wins, losses, kills, deaths) VALUES ('" + s.getName() + "', " + s.getWins() + ", " + s.getLosses() + ", " + s.getKills() + ", " + s.getDeaths() + ")");
                }
            } catch (SQLException e) {
                e.printStackTrace();
                CTT.send("Failed to update stats for " + s.getName());
                continue;
            }
        }
    }
}
