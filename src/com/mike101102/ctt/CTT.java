package com.mike101102.ctt;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.mike101102.ctt.gameapi.Game;
import com.mike101102.ctt.gameapi.GameAPIMain;
import com.mike101102.ctt.gameapi.GameListener;
import com.mike101102.ctt.gameapi.events.DebugListener;
import com.mike101102.ctt.gameapi.events.EventHandle;
import com.mike101102.ctt.gameapi.sql.SQL;
import com.mike101102.ctt.gameapi.sql.options.DatabaseOptions;
import com.mike101102.ctt.gameapi.sql.options.MySQLOptions;
import com.mike101102.ctt.gameapi.sql.options.SQLiteOptions;

public class CTT extends JavaPlugin {

    private SQL sql;
    private DatabaseOptions dop;
    private static boolean debug;

    public HashMap<String, Integer> creating_game_ids = new HashMap<String, Integer>();
    public HashMap<String, Location> creating_spawns_ids = new HashMap<String, Location>();
    public HashMap<String, Location> creating_spawns2_ids = new HashMap<String, Location>();
    public HashMap<String, Location> creating_goals_ids = new HashMap<String, Location>();

    public void onEnable() {
        saveDefaultConfig();
        debug = shouldDebug();
        String s = getConfig().getString("sql");
        if (s != null) {
            if (s.equalsIgnoreCase("mysql")) {
                debug("Selecting MySQL");
                setupMySQL();
            } else if (s.equalsIgnoreCase("sqlite")) {
                debug("Selecting SQLite");
                setupSQLite();
            } else {
                send("ERROR! SQL SET TO '" + s + "' WHICH IS UNKNOWN. PLEASE SET TO MYSQL OR SQLITE. DEFAULTING TO SQLITE!");
                setupSQLite();
            }
        } else {
            send("ERROR! SQL NOT SET TO ANYTHING IN THE CONFIG. DEFAULTING TO SQLITE!");
            setupSQLite();
        }
        sql = new SQL(this, dop);
        try {
            debug("Opening SQL connection");
            sql.open();
            sql.createTable("CREATE TABLE IF NOT EXISTS ctt (gameid INT(23), x1 INT(23), y1 INT(23), z1 INT(23), yaw1 VARCHAR(255), pitch1 VARCHAR(255), gx1 FLOAT(23), gy1 FLOAT(23), gz1 FLOAT(23), x2 INT(23), y2 INT(23), z2 INT(23), yaw2 VARCHAR(255), pitch2 VARCHAR(255), gx2 FLOAT(23), gy2 FLOAT(23), gz2 FLOAT(23), spawnworld VARCHAR(255), sx INT(23), sy INT(23), sz INT(23), signworld VARCHAR(255))");
        } catch (SQLException e) {
            e.printStackTrace();
            send("Failed to make connection with the database, disabling plugin");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        debug("Setting up games...");
        int gamesSetup = 0;
        try {
            ResultSet r = sql.query("SELECT gameid FROM ctt");
            while (r.next()) {
                try {
                    int gameid = r.getInt(1);
                    debug("Loading game " + gameid);
                    ResultSet rs = sql.query("SELECT * FROM ctt WHERE gameid=" + gameid);
                    if (dop instanceof MySQLOptions)
                        rs.first();
                    if (getServer().getWorld(rs.getString("spawnworld")) == null) {
                        send("Game ID " + gameid + "'s world does not exist. Will skip loading this game! To remove this message, delete this gameid or bring the world back.");
                        continue;
                    }
                    ArrayList<Location> spawns = new ArrayList<Location>();
                    spawns.add(new Location(getServer().getWorld(rs.getString("spawnworld")), rs.getInt("x1"), rs.getInt("y1"), rs.getInt("z1"), rs.getFloat("yaw1"), rs.getFloat("pitch1")));
                    spawns.add(new Location(getServer().getWorld(rs.getString("spawnworld")), rs.getInt("x2"), rs.getInt("y2"), rs.getInt("z2"), rs.getFloat("yaw2"), rs.getFloat("pitch2")));
                    Location sign = new Location(getServer().getWorld(rs.getString("signworld")), rs.getInt("sx"), rs.getInt("sy"), rs.getInt("sz"));
                    Location bluegoal = new Location(getServer().getWorld(rs.getString("spawnworld")), rs.getDouble("gx1"), rs.getDouble("gy1"), rs.getDouble("gz1"));
                    Location redgoal = new Location(getServer().getWorld(rs.getString("spawnworld")), rs.getDouble("gx2"), rs.getDouble("gy2"), rs.getDouble("gz2"));
                    debug(gameid + "'s spawns and locations loaded");
                    GameAPIMain.addRunner(new CTTGame(this, gameid, getMaxPlayers(), getTimeLimit(), sign, spawns, bluegoal, redgoal));
                    debug("Game " + gameid + " setup successfully");
                    gamesSetup++;
                } catch (SQLException e) {
                    e.printStackTrace();
                    send("Error while setting up one game, attempting to continue loading rest of the games");
                }
            }
        } catch (SQLException e) {
            send("Failed to load games, disabling");
            e.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        debug("Games setup: " + gamesSetup);
        getServer().getPluginManager().registerEvents(new CTTListener(this), this);
        getServer().getPluginManager().registerEvents(new GameListener(), this);
        if (debug) {
            getServer().getPluginManager().registerEvents(new DebugListener(), this);
            getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
                @Override
                public void run() {
                    CTT.debug("Games: " + GameAPIMain.getRunners().size());
                }
            }, 250L, 5000L);
        }
    }

    public void onDisable() {
        debug("Closing database connection...");
        try {
            sql.close();
            debug("Connection closed");
        } catch (SQLException e) {
            e.printStackTrace();
            send("Failed to close connection with the database");
        }
        GameAPIMain.onDisable();
        getServer().getScheduler().cancelTasks(this);
    }

    private void setupSQLite() {
        dop = new SQLiteOptions(new File(getDataFolder() + "/game_data.db"));
    }

    private void setupMySQL() {
        FileConfiguration c = getConfig();
        dop = new MySQLOptions(c.getString("MySQL.hostname"), c.getString("MySQL.port"), c.getString("MySQL.database"), c.getString("MySQL.username"), c.getString("MySQL.password"));
    }

    public static void send(String message) {
        System.out.println("[CTT] " + message);
    }

    public static void debug(String message) {
        if (debug)
            send("[Debug] " + message);
    }

    public SQL getSQL() {
        return sql;
    }

    public int getTimeLimit() {
        return getConfig().getInt("time-limit");
    }

    public int getMaxPlayers() {
        return getConfig().getInt("max-players");
    }

    private boolean shouldDebug() {
        return getConfig().getBoolean("debug");
    }

    public boolean debug() {
        return debug;
    }

    public String getKillMessage() {
        return convert(getConfig().getString("kill-message"));
    }

    public String convert(String string) {
        return ChatColor.translateAlternateColorCodes("^".charAt(0), string);
    }
    
    public void cancelCreation(Player player) {
        if (creating_game_ids.containsKey(player.getName()) || creating_spawns_ids.containsKey(player.getName()) || creating_spawns2_ids.containsKey(player.getName()) || creating_goals_ids.containsKey(player.getName())) {
            creating_game_ids.remove(player.getName());
            creating_spawns_ids.remove(player.getName());
            creating_spawns2_ids.remove(player.getName());
            creating_goals_ids.remove(player.getName());
            player.sendMessage(ChatColor.GREEN + "You have cancelled creating a game");
            debug("Cancelled creation of game for " + player.getName());
            return;
        } else {
            player.sendMessage(ChatColor.RED + "You aren't creating anything!");
            return;
        }
    }

    public void help(CommandSender s) {
        s.sendMessage(ChatColor.RED + "CTT Commands You Can Use");
        if (s.hasPermission("ctt.create")) {
            s.sendMessage(ChatColor.GOLD + "/ctt create [id]");
            s.sendMessage(ChatColor.GOLD + "/ctt setspawn");
            s.sendMessage(ChatColor.GOLD + "/ctt setgoal");
            s.sendMessage(ChatColor.GOLD + "/ctt cancel");
        }
        if (s.hasPermission("ctt.delete"))
            s.sendMessage(ChatColor.GOLD + "/ctt delete [id]");
        if (s.hasPermission("ctt.reset"))
            s.sendMessage(ChatColor.GOLD + "/ctt reset [id]");
        if (s.hasPermission("ctt.join")) {
            s.sendMessage(ChatColor.GOLD + "/join [id]");
            s.sendMessage(ChatColor.GOLD + "/leave");
        }
        if (s.hasPermission("ctt.list"))
            s.sendMessage(ChatColor.GOLD + "/games");
        if (s.hasPermission("ctt.info"))
            s.sendMessage(ChatColor.GOLD + "/gameinfo [id]");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
        debug("Command called: " + sender.getName() + "; " + command.getName());
        if (command.getName().equalsIgnoreCase("join")) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                if (!player.hasPermission("ctt.join")) {
                    player.sendMessage(ChatColor.RED + "You do not have permission (ctt.join)");
                    return true;
                }
                if (args.length == 1) {
                    try {
                        int i = Integer.parseInt(args[0]);
                        if (GameAPIMain.getRunners().containsKey(i)) {
                            Game game = GameAPIMain.getRunners().get(i);
                            for (Entry<Integer, Game> en : GameAPIMain.getRunners().entrySet()) {
                                if (game.getPlayers().contains(player.getName())) {
                                    player.sendMessage(ChatColor.RED + "You are already in that game!");
                                    return true;
                                }
                                if (en.getValue().getPlayers().contains(player.getName())) {
                                    player.sendMessage(ChatColor.RED + "You are already in game " + en.getValue().getGameId() + "!");
                                    return true;
                                }
                            }
                            if (!EventHandle.callPlayerJoinGameEvent(game, player).isCancelled()) {
                                game.addPlayer(player);
                            }
                            return true;
                        } else {
                            player.sendMessage(ChatColor.RED + "That game doesn't exist!");
                            return true;
                        }
                    } catch (Exception e) {
                        player.sendMessage(ChatColor.RED + args[0] + " is not a number");
                        return true;
                    }
                } else {
                    player.sendMessage(ChatColor.GOLD + "/join [id]");
                    return true;
                }
            } else {
                sender.sendMessage(ChatColor.RED + "You must be a player to join a game!");
                return true;
            }
        }

        if (command.getName().equalsIgnoreCase("leave")) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                for (Entry<Integer, Game> en : GameAPIMain.getRunners().entrySet()) {
                    if (en.getValue().getPlayers().contains(player.getName())) {
                        if (!EventHandle.callPlayerLeaveGameEvent(en.getValue(), player).isCancelled()) {
                            en.getValue().removePlayer(player);
                        }
                        return true;
                    }
                }
                player.sendMessage(ChatColor.RED + "You aren't in any games");
                return true;
            } else {
                sender.sendMessage(ChatColor.RED + "You must be a player to leave a game!");
                return true;
            }
        }

        if (command.getName().equalsIgnoreCase("games")) {
            if (sender.hasPermission("ctt.list")) {
                if (GameAPIMain.getRunners().size() == 0) {
                    sender.sendMessage(ChatColor.GOLD + "Games: " + ChatColor.GREEN + "None");
                    return true;
                }
                sender.sendMessage(ChatColor.GOLD + "Format: gameid:isRunning,...");
                StringBuilder sb = new StringBuilder();
                sb.append(ChatColor.GOLD + "Games: " + ChatColor.GREEN);
                for (Entry<Integer, Game> en : GameAPIMain.getRunners().entrySet()) {
                    sb.append(en.getValue().getGameId() + ":" + en.getValue().isRunning() + ",");
                }
                sb.deleteCharAt(sb.length() - 1);
                sender.sendMessage(sb.toString());
                return true;
            } else {
                sender.sendMessage(ChatColor.RED + "You do not have permission (ctt.list)");
                return true;
            }
        }

        if (command.getName().equalsIgnoreCase("gameinfo")) {
            if (sender.hasPermission("ctt.info")) {
                if (args.length == 1) {
                    try {
                        int gameid = Integer.parseInt(args[0]);
                        if (GameAPIMain.getRunners().containsKey(gameid)) {
                            Game game = GameAPIMain.getRunners().get(gameid);
                            sender.sendMessage(ChatColor.GOLD + "Players: " + ChatColor.GREEN.toString() + game.getPlayers().size() + "/" + game.getMaxPlayers());
                            sender.sendMessage(ChatColor.GOLD + "Running: " + ChatColor.GREEN.toString() + game.isRunning());
                            sender.sendMessage(ChatColor.GOLD + "Game Stage: " + ChatColor.GREEN.toString() + game.getGameStage());
                            if (game instanceof CTTGame) {
                                CTTGame g = (CTTGame) game;
                                sender.sendMessage(ChatColor.GOLD + "Scores: " + ChatColor.BLUE + g.getBlueScoreFromBlocks() + " " + ChatColor.RED + g.getRedScoreFromBlocks());
                            }
                            return true;
                        } else {
                            sender.sendMessage(ChatColor.RED.toString() + gameid + " is not a valid game ID!");
                            return true;
                        }
                    } catch (NumberFormatException e) {
                        sender.sendMessage(ChatColor.RED + args[0] + " is not a number");
                        return true;
                    }
                } else {
                    sender.sendMessage(ChatColor.GOLD + "/gameinfo [id]");
                    return true;
                }
            } else {
                sender.sendMessage(ChatColor.RED + "You do not have permission (ctt.info)");
                return true;
            }
        }

        if (args.length == 0) {
            help(sender);
            return true;
        }

        if (args[0].equalsIgnoreCase("cancel")) {
            if (sender instanceof Player) {
                cancelCreation((Player) sender);
                return true;
            } else {
                sender.sendMessage(ChatColor.RED + "You must be a player!");
                return true;
            }
        }

        else if (args[0].equalsIgnoreCase("create")) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                if (creating_game_ids.containsKey(player.getName())) {
                    player.sendMessage(ChatColor.RED + "You are already creating a game. If this is a mistake, use /ctt cancel");
                    return true;
                }
                if (player.hasPermission("ctt.create")) {
                    if (args.length == 2) {
                        int id;
                        try {
                            id = Integer.parseInt(args[1]);
                        } catch (NumberFormatException e) {
                            player.sendMessage(ChatColor.GOLD + args[1] + ChatColor.RED + " is not a number");
                            return true;
                        }
                        if (GameAPIMain.getRunners().containsKey(id)) {
                            player.sendMessage(ChatColor.GOLD.toString() + id + ChatColor.RED + " is already a game id");
                            return true;
                        } else {
                            try {
                                ResultSet rs = sql.query("SELECT * FROM ctt WHERE gameid=" + id);
                                if (rs.first()) {
                                    player.sendMessage(ChatColor.GOLD.toString() + id + ChatColor.RED + " is already a game id");
                                    return true;
                                }
                            } catch (SQLException e) {
                                e.printStackTrace();
                                player.sendMessage(ChatColor.RED + "Could not determine if the ID was in the database already or not. Please check the console for the error.");
                                return true;
                            }
                        }
                        creating_game_ids.put(player.getName(), id);
                        player.sendMessage(ChatColor.GREEN + "Game created, set blue spawn with /ctt setspawn");
                        debug("Game creation process started, ID: " + id);
                        return true;
                    } else {
                        sender.sendMessage(ChatColor.GOLD + "/ctt create [id]");
                        return true;
                    }
                } else {
                    player.sendMessage(ChatColor.RED + "You do not have permission (ctt.create)");
                    return true;
                }
            } else {
                sender.sendMessage(ChatColor.RED + "You must be a player!");
                return true;
            }
        }

        else if (args[0].equalsIgnoreCase("setspawn")) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                if (creating_game_ids.containsKey(player.getName())) {
                    if (creating_spawns2_ids.containsKey(player.getName())) {
                        player.sendMessage(ChatColor.RED + "You have already completed this step. If this is a mistake, use /ctt cancel");
                        return true;
                    }
                    if (!creating_spawns_ids.containsKey(player.getName())) {
                        creating_spawns_ids.put(player.getName(), player.getLocation());
                        player.sendMessage(ChatColor.GREEN + "Location for blue team set. Use same command again to set red team's spawn\nTo cancel, use /ctt cancel");
                        debug("Blue team location set by " + player.getName() + " for game " + creating_game_ids.get(player.getName()));
                        return true;
                    } else {
                        creating_spawns2_ids.put(player.getName(), player.getLocation());
                        player.sendMessage(ChatColor.GREEN + "Location for red team set. Use '/ctt setgoal' to set blue team's goal location");
                        debug("Red team location set by " + player.getName() + " for game " + creating_game_ids.get(player.getName()));
                        return true;
                    }
                } else {
                    player.sendMessage(ChatColor.RED + "You must be creating a game first!");
                    return true;
                }
            } else {
                sender.sendMessage(ChatColor.RED + "You must be a player!");
                return true;
            }
        }

        else if (args[0].equalsIgnoreCase("setgoal")) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                if (creating_game_ids.containsKey(player.getName())) {
                    if (creating_spawns2_ids.containsKey(player.getName())) {
                        if (!creating_goals_ids.containsKey(player.getName())) {
                            creating_goals_ids.put(player.getName(), player.getLocation());
                            player.sendMessage(ChatColor.GREEN + "Blue goal set, use same command for red goal");
                            debug("Blue team goal location set by " + player.getName() + " for game " + creating_game_ids.get(player.getName()));
                            return true;
                        } else {
                            String n = player.getName();
                            int id = creating_game_ids.get(n);
                            Location spawn1 = creating_spawns_ids.get(n);
                            Location spawn2 = creating_spawns2_ids.get(n);
                            Location goal1 = creating_goals_ids.get(n);
                            Location goal2 = player.getLocation();
                            creating_game_ids.remove(n);
                            creating_spawns_ids.remove(n);
                            creating_spawns2_ids.remove(n);
                            creating_goals_ids.remove(n);
                            debug("Red team goal location set by " + player.getName() + " for game " + id + ", setting game up..");
                            ArrayList<Location> teamspawns = new ArrayList<Location>();
                            teamspawns.add(spawn1);
                            teamspawns.add(spawn2);
                            CTTGame game = new CTTGame(this, id, getMaxPlayers(), getTimeLimit(), null, teamspawns, goal1, goal2);
                            GameAPIMain.addRunner(game);
                            debug("Game created and added to runner list");
                            try {
                                sql.query("INSERT INTO ctt (gameid, x1, y1, z1, yaw1, pitch1, gx1, gy1, gz1, x2, y2, z2, yaw2, pitch2, gx2, gy2, gz2, spawnworld, sx, sy, sz, signworld) VALUES (" + id + ", " + (int) spawn1.getX() + ", " + (int) spawn1.getY() + ", " + (int) spawn1.getZ() + ", '" + spawn1.getYaw() + "', '" + spawn1.getPitch() + "', " + goal1.getX() + ", " + goal1.getY() + ", " + goal1.getZ() + ", " + (int) spawn2.getX() + ", " + (int) spawn2.getY() + ", " + (int) spawn2.getZ() + ", '" + spawn2.getYaw() + "', '" + spawn2.getPitch() + "', " + goal2.getX() + ", " + goal2.getY() + ", " + goal2.getZ() + ", '" + spawn1.getWorld().getName() + "', " + "0, 0, 0, 'unknown-world')");
                            } catch (SQLException e) {
                                e.printStackTrace();
                                GameAPIMain.removeRunner(id);
                                player.sendMessage(ChatColor.RED + "Failed to add game to the database: " + e.getMessage());
                                return true;
                            }
                            debug("Game added to database. All is well");
                            player.sendMessage(ChatColor.GREEN + "Game created! You can create the sign now:\n[game]\n" + id);
                            return true;
                        }
                    } else {
                        player.sendMessage(ChatColor.RED + "You need to set spawns first. If this is a mistake, use /ctt cancel");
                        return true;
                    }
                } else {
                    player.sendMessage(ChatColor.RED + "You must be creating a game first!");
                    return true;
                }
            } else {
                sender.sendMessage(ChatColor.RED + "You must be a player!");
                return true;
            }
        }

        else if (args[0].equalsIgnoreCase("delete")) {
            if (sender.hasPermission("ctt.delete")) {
                if (args.length == 2) {
                    int id;
                    try {
                        id = Integer.parseInt(args[1]);
                    } catch (NumberFormatException e) {
                        sender.sendMessage(ChatColor.GOLD + args[1] + ChatColor.RED + " is not an id");
                        return true;
                    }
                    Game game = GameAPIMain.getRunners().get(id);
                    if (game != null) {
                        if (game instanceof CTTGame) {
                            debug("Removing runner for game " + id);
                            GameAPIMain.removeRunner(game.getGameId());
                            try {
                                sql.query("DELETE FROM ctt WHERE gameid=" + id);
                            } catch (SQLException e) {
                                e.printStackTrace();
                                sender.sendMessage(ChatColor.RED + "SQL error while removing the game, " + e.getMessage());
                                return true;
                            }
                            sender.sendMessage(ChatColor.GREEN + "Game has been removed");
                            debug(id + " has been removed");
                            return true;
                        } else {
                            sender.sendMessage(ChatColor.RED + "That game is not a CTT game. Use that game's plugin to delete it");
                            return true;
                        }
                    } else {
                        try {
                            ResultSet rs = sql.query("SELECT * FROM ctt WHERE gameid=" + id);
                            if (rs.first()) {
                                sql.query("DELETE FROM ctt WHERE gameid=" + id);
                                sender.sendMessage(ChatColor.GREEN + "Game has been removed");
                                debug(id + " has been removed");
                                return true;
                            }
                        } catch (SQLException e) {
                            e.printStackTrace();
                            sender.sendMessage(ChatColor.RED + "SQL error while removing the game, " + e.getMessage());
                            return true;
                        }
                        sender.sendMessage(ChatColor.RED + "That game with that id does not exist");
                        return true;
                    }
                } else {
                    sender.sendMessage(ChatColor.GOLD + "/ctt delete [id]");
                    return true;
                }
            } else {
                sender.sendMessage(ChatColor.RED + "You do not have permission (ctt.delete)");
                return true;
            }
        }

        else if (args[0].equalsIgnoreCase("reset")) {
            if (sender.hasPermission("ctt.reset")) {
                if (args.length == 2) {
                    int id;
                    try {
                        id = Integer.parseInt(args[1]);
                    } catch (NumberFormatException e) {
                        sender.sendMessage(ChatColor.GOLD + args[1] + ChatColor.RED + " is not an id");
                        return true;
                    }
                    Game game = GameAPIMain.getRunners().get(id);
                    if (game != null) {
                        if (game instanceof CTTGame) {
                            debug("Resetting game " + id);
                            ((CTTGame) game).resetGame(true);
                            sender.sendMessage(ChatColor.GOLD.toString() + id + ChatColor.GREEN + " has been reset");
                            return true;
                        } else {
                            sender.sendMessage(ChatColor.RED + "That game is not a CTT game. Use that game's plugin to delete it");
                            return true;
                        }
                    } else {
                        sender.sendMessage(ChatColor.RED + "That game with that id does not exist");
                        return true;
                    }
                } else {
                    sender.sendMessage(ChatColor.GOLD + "/ctt reset [id]");
                    return true;
                }
            } else {
                sender.sendMessage(ChatColor.RED + "You do not have permission (ctt.rest)");
                return true;
            }
        }
        help(sender);
        return true;
    }
}
