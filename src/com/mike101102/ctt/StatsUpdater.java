package com.mike101102.ctt;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map.Entry;

import org.bukkit.scheduler.BukkitRunnable;

import com.mike101102.ctt.gameapi.sql.SQL;

public class StatsUpdater extends BukkitRunnable {

    private CTT plugin;
    private SQL sql;
    private boolean stats;

    public StatsUpdater(CTT plugin) throws SQLException {
        this.plugin = plugin;
        stats = plugin.stats();
        sql = plugin.getSQL();

        ResultSet rs = sql.query("SELECT * FROM ctt_stats");
        while (rs.next()) {
            plugin.getPlayerStats().put(rs.getString("player"), new PlayerStats(rs.getString("player"), rs.getInt("wins"), rs.getInt("losses"), rs.getInt("kills"), rs.getInt("deaths")));
        }
        updateTops();
    }

    private void updateTops() {
        try {
            CTT.debug("Updating Top Kills");
            ResultSet rs = sql.query("SELECT player,kills FROM ctt_stats ORDER BY kills DESC");
            plugin.getTopKills().clear();
            while (rs.next()) {
                plugin.getTopKills().put(rs.getString("player"), rs.getInt("kills"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            CTT.debug("Updating Top Wins");
            ResultSet rs = sql.query("SELECT player,wins FROM ctt_stats ORDER BY wins DESC");
            plugin.getTopWins().clear();
            while (rs.next()) {
                plugin.getTopWins().put(rs.getString("player"), rs.getInt("wins"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        if (!stats)
            return;
        try {
            ResultSet r = sql.query("SELECT * FROM ctt_stats");
            HashMap<String, PlayerStats> p = new HashMap<String, PlayerStats>();
            while (r.next()) {
                p.put(r.getString("player"), new PlayerStats(r.getString("player"), r.getInt("wins"), r.getInt("losses"), r.getInt("kills"), r.getInt("deaths")));
            }

            for (Entry<String, PlayerStats> en : plugin.getPlayerStats().entrySet()) {
                PlayerStats s = en.getValue();
                if (p.get(s.getName()) != null) {
                    if (s.equals(p.get(s.getName()))) {
                        CTT.debug(en.getKey() + "'s stats haven't changed");
                        continue;
                    }
                }
                try {
                    ResultSet rs = sql.query("SELECT * FROM ctt_stats WHERE player='" + s.getName() + "'");
                    if (rs.next()) {
                        sql.query("UPDATE ctt_stats SET wins=" + s.getWins() + ", losses=" + s.getLosses() + ", kills=" + s.getKills() + ", deaths=" + s.getDeaths() + " WHERE player='" + s.getName() + "'");
                    } else {
                        sql.query("INSERT INTO ctt_stats (player, wins, losses, kills, deaths) VALUES ('" + s.getName() + "', " + s.getWins() + ", " + s.getLosses() + ", " + s.getKills() + ", " + s.getDeaths() + ")");
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                    CTT.send("Failed to update stats for " + s.getName());
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        updateTops();
    }
}
