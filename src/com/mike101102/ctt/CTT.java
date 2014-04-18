package com.mike101102.ctt;

import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.mcstats.Metrics;

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
    private boolean stats;
    private StatsUpdater su;
    private ArrayList<Integer> okayIds;

    public HashMap<String, Integer> creating_game_ids = new HashMap<String, Integer>();
    public HashMap<String, Location> creating_spawns_ids = new HashMap<String, Location>();
    public HashMap<String, Location> creating_spawns2_ids = new HashMap<String, Location>();
    public HashMap<String, Location> creating_goals_ids = new HashMap<String, Location>();

    private final HashMap<String, PlayerStats> playerStats = new HashMap<String, PlayerStats>();
    private final HashMap<String, Kit> kits = new HashMap<String, Kit>();
    private final HashMap<String, Integer> spawnDelays = new HashMap<String, Integer>();
    private final LinkedHashMap<String, Top> topWins = new LinkedHashMap<String, Top>();
    private final LinkedHashMap<String, Top> topKills = new LinkedHashMap<String, Top>();

    public void onEnable() {
        if (new File("plugins/CaptureTheTower/config.yml").exists()) {
            debug("Config found, copying any missing defaults");
            getConfig().addDefault("kits.Warrior", null);
            getConfig().addDefault("kits.Archer", null);
            getConfig().addDefault("kits.Runner", null);
            getConfig().options().copyDefaults(true);
            saveConfig();
        } else {
            debug("Config not found, copying default config");
            saveDefaultConfig();
        }
        debug = shouldDebug();
        stats = getConfig().getBoolean("enable-stats");
        String s = getConfig().getString("sql");
        okayIds = getOkayIdsFromConfig();
        debug("Loading kits");
        for (String i : getConfig().getConfigurationSection("kits").getKeys(false)) {
            List<ItemStack> t = new ArrayList<ItemStack>();
            for (String j : getConfig().getString("kits." + i + ".contents").replaceAll(" ", "").split(",")) {
                ItemStack u = convertItem(j);
                if (u != null) {
                    t.add(u);
                }
            }
            kits.put(i.toLowerCase(), new Kit(i, getConfig().getString("kits." + i + ".permission"), t));
        }
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
            sql.createTable("CREATE TABLE IF NOT EXISTS ctt_stats (player VARCHAR(255), wins INT(23), losses INT(23), kills INT(23), deaths INT(23))");
        } catch (SQLException e) {
            e.printStackTrace();
            send("Failed to make connection with the database, disabling plugin");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        debug("Setting up games...");
        int gamesSetup = 0;
        int spawnDelay = getSpawnDelayFromConfig();
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
                        debug("World given: " + rs.getString("spawnworld"));
                        continue;
                    }
                    ArrayList<Location> spawns = new ArrayList<Location>();
                    spawns.add(new Location(getServer().getWorld(rs.getString("spawnworld")), rs.getInt("x1"), rs.getInt("y1"), rs.getInt("z1"), rs.getFloat("yaw1"), rs.getFloat("pitch1")));
                    spawns.add(new Location(getServer().getWorld(rs.getString("spawnworld")), rs.getInt("x2"), rs.getInt("y2"), rs.getInt("z2"), rs.getFloat("yaw2"), rs.getFloat("pitch2")));
                    Location sign = new Location(getServer().getWorld(rs.getString("signworld")), rs.getInt("sx"), rs.getInt("sy"), rs.getInt("sz"));
                    Location bluegoal = new Location(getServer().getWorld(rs.getString("spawnworld")), rs.getDouble("gx1"), rs.getDouble("gy1"), rs.getDouble("gz1"));
                    Location redgoal = new Location(getServer().getWorld(rs.getString("spawnworld")), rs.getDouble("gx2"), rs.getDouble("gy2"), rs.getDouble("gz2"));
                    debug(gameid + "'s spawns and locations loaded");
                    GameAPIMain.addRunner(new CTTGame(this, gameid, getMaxPlayers(), getTimeLimit(), spawnDelay, sign, spawns, bluegoal, redgoal));
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
        if (stats) {
            try {
                su = new StatsUpdater(this);
            } catch (SQLException e) {
                e.printStackTrace();
                send("Failed to load stats, disabling");
                stats = false;
                getServer().getPluginManager().disablePlugin(this);
                return;
            }
            getServer().getScheduler().scheduleAsyncRepeatingTask(this, su, 6000L, 6000L);
        }
        try {
            Metrics m = new Metrics(this);
            m.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void onDisable() {
        if (stats) {
            send("!!! Updating stats, this may take a moment... !!!");
            su.run();
        }
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

    private ArrayList<Integer> getOkayIdsFromConfig() {
        ArrayList<Integer> q = new ArrayList<Integer>();
        for (String i : getConfig().getString("gold-block-ids").replaceAll(" ", "").split(",")) {
            int y;
            try {
                y = Integer.valueOf(i);
            } catch (NumberFormatException e) {
                continue;
            }
            debug("Adding okay id " + y);
            q.add(y);
        }
        return q;
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

    public LinkedHashMap<String, Top> getTopWins() {
        return topWins;
    }

    public LinkedHashMap<String, Top> getTopKills() {
        return topKills;
    }

    public ArrayList<Integer> getOkayIds() {
        return okayIds;
    }
    
    public HashMap<String, Integer> getSpawnDelays() {
        return spawnDelays;
    }

    public boolean stats() {
        return stats;
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
    
    public int getSpawnDelayFromConfig() {
        return getConfig().getInt("spawn-delay");
    }

    private boolean shouldDebug() {
        return getConfig().getBoolean("debug");
    }

    public boolean debug() {
        return debug;
    }

    public HashMap<String, PlayerStats> getPlayerStats() {
        return playerStats;
    }

    public String getKillMessage() {
        return convert(getConfig().getString("kill-message"));
    }

    public String convert(String string) {
        return ChatColor.translateAlternateColorCodes("^".charAt(0), string);
    }

    public HashMap<String, Kit> getKits() {
        return kits;
    }

    public ItemStack convertItem(String i) {
        ItemStack a;
        try {
            String[] b = i.split(";");
            a = new ItemStack(Material.getMaterial(Integer.parseInt(b[0])));
            try {
                if (b[1].equalsIgnoreCase("r")) {
                    a.setAmount(new Random().nextInt(33));
                } else {
                    a.setAmount(Integer.parseInt(b[1]));
                }
                a.setDurability(Short.parseShort(b[2]));
            } catch (Exception e) {
            }
        } catch (Exception e) {
            return null;
        }
        return a;
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
        if (s.hasPermission("ctt.stats")) {
            s.sendMessage(ChatColor.GOLD + "/ctt top");
            s.sendMessage(ChatColor.GOLD + "/ctt stats (name)");
        }
        if (s.hasPermission("ctt.kit")) {
            s.sendMessage(ChatColor.GOLD + "/ctt kit [name]");
            s.sendMessage(ChatColor.GOLD + "/ctt kits");
        }
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
                    String w = player.getWorld().getName();
                    int i = 0;
                    Game g = null;
                    for (Entry<Integer, Game> en : GameAPIMain.getRunners().entrySet()) {
                        if (en.getValue().getTeamSpawns().get(0).getWorld().getName().equalsIgnoreCase(w)) {
                            i++;
                            if (i > 1) {
                                break;
                            }
                            g = en.getValue();
                        }
                    }
                    if (i == 1 && !g.getPlayers().contains(player.getName())) {
                        if (!EventHandle.callPlayerJoinGameEvent(g, player).isCancelled()) {
                            g.addPlayer(player);
                            return true;
                        }
                    }
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
                                if (rs.next()) {
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
                            CTTGame game = new CTTGame(this, id, getMaxPlayers(), getTimeLimit(), getSpawnDelayFromConfig(), null, teamspawns, goal1, goal2);
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
                            if (rs.next()) {
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
                            ((CTTGame) game).resetGame(true, false);
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

        else if (args[0].equalsIgnoreCase("top")) {
            if (sender.hasPermission("ctt.stats")) {
                StringBuilder sb = new StringBuilder();
                sb.append(ChatColor.GOLD + "Top Wins: " + ChatColor.GREEN);
                int cap = 0;
                for (Entry<String, Top> en : topWins.entrySet()) {
                    if (cap < 10) {
                        sb.append(ChatColor.RED.toString() + (cap + 1) + ". " + ChatColor.GREEN + en.getKey() + " (" + en.getValue().getValue() + "), ");
                    } else {
                        break;
                    }
                    cap++;
                }
                sb.deleteCharAt(sb.length() - 1);
                sb.deleteCharAt(sb.length() - 1);
                sb.append("\n");
                sb.append(ChatColor.GOLD + "Top Kills: " + ChatColor.GREEN);
                cap = 0;
                for (Entry<String, Top> en : topKills.entrySet()) {
                    if (cap < 10) {
                        sb.append(ChatColor.RED.toString() + (cap + 1) + ". " + ChatColor.GREEN + en.getKey() + " (" + en.getValue().getValue() + "), ");
                    } else {
                        break;
                    }
                    cap++;
                }
                sb.deleteCharAt(sb.length() - 1);
                sb.deleteCharAt(sb.length() - 1);
                sender.sendMessage(sb.toString());
                return true;
            } else {
                sender.sendMessage(ChatColor.RED + "You do not have permission (ctt.stats)");
                return true;
            }
        }

        else if (args[0].equalsIgnoreCase("stats")) {
            if (sender.hasPermission("ctt.stats")) {
                PlayerStats s = null;
                if (args.length == 1) {
                    s = playerStats.get(sender.getName());
                } else if (args.length == 2) {
                    s = playerStats.get(args[1]);
                } else {
                    sender.sendMessage(ChatColor.GOLD + "/ctt stats (name)");
                    return true;
                }
                if (s == null) {
                    sender.sendMessage(ChatColor.RED + "You don't have any stats yet. Play some games first!");
                    return true;
                }
                StringBuilder sb = new StringBuilder();
                sb.append(ChatColor.GREEN + "Stats for " + ChatColor.RED + s.getName() + "\n");
                if (topWins.containsKey(s.getName())) {
                    sb.append(ChatColor.GREEN + "Wins: " + ChatColor.GOLD + s.getWins() + ChatColor.RED + " (Rank " + ChatColor.GOLD + topWins.get(s.getName()).getRank() + ChatColor.RED + ")\n");
                } else {
                    sb.append(ChatColor.GREEN + "Wins: " + ChatColor.GOLD + s.getWins() + "\n");
                }
                sb.append(ChatColor.GREEN + "Losses: " + s.getLosses() + "\n");
                if (topKills.containsKey(s.getName())) {
                    sb.append(ChatColor.GREEN + "Kills: " + ChatColor.GOLD + s.getKills() + ChatColor.RED + " (Rank " + ChatColor.GOLD + topKills.get(s.getName()).getRank() + ChatColor.RED + ")\n");
                } else {
                    sb.append(ChatColor.GREEN + "Kills: " + ChatColor.GOLD + s.getKills());
                }
                sb.append(ChatColor.GREEN + "Deaths: " + ChatColor.GOLD + s.getDeaths());
                sender.sendMessage(sb.toString());
                return true;
            } else {
                sender.sendMessage(ChatColor.RED + "You do not have permission (ctt.stats)");
                return true;
            }
        }

        else if (args[0].equalsIgnoreCase("kit")) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                if (player.hasPermission("ctt.kit")) {
                    if (args.length == 2) {
                        if (kits.get(args[1].toLowerCase()) != null) {
                            Kit i = kits.get(args[1].toLowerCase());
                            if (player.hasPermission(i.getPermission())) {
                                for (Entry<Integer, Game> en : GameAPIMain.getRunners().entrySet()) {
                                    if (en.getValue() instanceof CTTGame) {
                                        if (en.getValue().getPlayers().contains(player.getName())) {
                                            CTTGame g = (CTTGame) en.getValue();
                                            g.resetPlayerInventory(player, i);
                                            player.sendMessage(ChatColor.GOLD + i.getName() + ChatColor.GREEN + " has been selected");
                                            return true;
                                        }
                                    }
                                }
                                player.sendMessage(ChatColor.RED + "You must be in a game to choose a kit");
                                return true;
                            } else {
                                player.sendMessage(ChatColor.RED + "You do not have permission (" + i.getPermission() + ")");
                                return true;
                            }
                        } else {
                            player.sendMessage(ChatColor.GOLD + args[1] + ChatColor.RED + " is not a valid kit");
                            return true;
                        }
                    } else {
                        player.sendMessage(ChatColor.GOLD + "/ctt kit [name]");
                        return true;
                    }
                } else {
                    player.sendMessage(ChatColor.RED + "You do not have permission (ctt.kit)");
                    return true;
                }
            } else {
                sender.sendMessage(ChatColor.RED + "You must be a player to pick a kit!");
                return true;
            }
        }

        else if (args[0].equalsIgnoreCase("kits")) {
            StringBuilder sb = new StringBuilder();
            sb.append(ChatColor.RED + "Kits you can use\n" + ChatColor.GOLD);
            for (Entry<String, Kit> en : kits.entrySet()) {
                if (sender.hasPermission(en.getValue().getPermission())) {
                    sb.append(en.getValue().getName() + ", ");
                }
            }
            sb.deleteCharAt(sb.length() - 1);
            sb.deleteCharAt(sb.length() - 1);
            sender.sendMessage(sb.toString());
            return true;
        }
        help(sender);
        return true;
    }
}
