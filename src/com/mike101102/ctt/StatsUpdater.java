package com.mike101102.ctt;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.UUID;

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
            plugin.getPlayerStats().put(UUID.fromString(rs.getString("player")), new PlayerStats(UUID.fromString(rs.getString("player")), rs.getInt("wins"), rs.getInt("losses"), rs.getInt("kills"), rs.getInt("deaths")));
        }
        updateTops();
        updatePlayerStatNames();
    }

    private void updateTops() {
        try {
            CTT.debug("Updating Top Kills");
            ResultSet rs = sql.query("SELECT player,kills FROM ctt_stats ORDER BY kills DESC");
            plugin.getTopKills().clear();
            int r = 1;
            while (rs.next()) {
                plugin.getTopKills().put(UUID.fromString(rs.getString("player")), new Top(rs.getInt("kills"), r));
                r++;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            CTT.debug("Updating Top Wins");
            ResultSet rs = sql.query("SELECT player,wins FROM ctt_stats ORDER BY wins DESC");
            plugin.getTopWins().clear();
            int r = 1;
            while (rs.next()) {
                plugin.getTopWins().put(UUID.fromString(rs.getString("player")), new Top(rs.getInt("wins"), r));
                r++;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updatePlayerStatNames() {
        CTT.debug("Running Name Fetcher");
        NameFetcher nf = new NameFetcher(new ArrayList<UUID>(plugin.getPlayerStats().keySet()));
        plugin.getPlayerStatNames().clear();
        try {
            CTT.debug("Calling Mojang");
            plugin.getPlayerStatNames().putAll(nf.call());
            CTT.debug("Call successful");
        } catch (Exception e) {
            CTT.debug("Mojang didn't pickup: " + e.getMessage());
        }
    }

    @Override
    public void run() {
        if (!stats)
            return;
        try {
            CTT.debug("Updating stats");
            ResultSet r = sql.query("SELECT * FROM ctt_stats");
            HashMap<UUID, PlayerStats> p = new HashMap<UUID, PlayerStats>();
            while (r.next()) {
                p.put(UUID.fromString(r.getString("player")), new PlayerStats(UUID.fromString(r.getString("player")), r.getInt("wins"), r.getInt("losses"), r.getInt("kills"), r.getInt("deaths")));
            }

            updatePlayerStatNames();
            for (Entry<UUID, PlayerStats> en : plugin.getPlayerStats().entrySet()) {
                PlayerStats s = en.getValue();
                if (p.get(s.getUniqueId()) != null) {
                    if (s.equals(p.get(s.getUniqueId()))) {
                        CTT.debug(en.getKey() + "'s stats haven't changed");
                        continue;
                    }
                }
                try {
                    ResultSet rs = sql.query("SELECT * FROM ctt_stats WHERE player='" + s.getUniqueId() + "'");
                    if (rs.next()) {
                        sql.query("UPDATE ctt_stats SET wins=" + s.getWins() + ", losses=" + s.getLosses() + ", kills=" + s.getKills() + ", deaths=" + s.getDeaths() + " WHERE player='" + s.getUniqueId() + "'");
                    } else {
                        sql.query("INSERT INTO ctt_stats (player, wins, losses, kills, deaths) VALUES ('" + s.getUniqueId() + "', " + s.getWins() + ", " + s.getLosses() + ", " + s.getKills() + ", " + s.getDeaths() + ")");
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                    CTT.send("Failed to update stats for " + s.getUniqueId());
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        updateTops();
    }
}
