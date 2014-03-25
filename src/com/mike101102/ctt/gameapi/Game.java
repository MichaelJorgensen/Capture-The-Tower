package com.mike101102.ctt.gameapi;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import com.mike101102.ctt.CTT;
import com.mike101102.ctt.gameapi.events.EventHandle;

public abstract class Game extends BukkitRunnable implements GameAPI {

    private JavaPlugin plugin;

    private int gameid;
    private int maxPlayers;
    private int signTracker = 0;
    private int taskID;

    private boolean running = false;
    private boolean shutdown = false;

    private Location signLoc;
    private String gameName;
    private GameStage gamestage;
    private GameStage defaultStage;

    private final ArrayList<String> playerlist = new ArrayList<String>();
    private ArrayList<String> queuedplayers = new ArrayList<String>();
    private ArrayList<Location> teamspawns;

    /**
     * Represents a game. The constructor will automatically schedule the game
     * with Bukkit's scheduler.
     * 
     * @param plugin that is using this
     * @param gameid a unique identification for the game, used for identifying
     *            the arena in-game and in the database
     * @param maxPlayers the maximum amount of players allowed in the game
     * @param signLoc location of the sign used for joining the game
     * @param gameName the name of the game (pun intended), will be displayed on
     *            the game's sign
     * @param gamestage the game stage that the game should start in and <b>the
     *            default game stage</b>
     * @param teamspawns all of the different spawn points, team 1 is at index
     *            0, 2 at 1, 3 at 2, etc
     * @param delay the delay in ticks before the game's scheduler begins
     * @param period how often the run() is called in ticks
     */
    public Game(JavaPlugin plugin, int gameid, int maxPlayers, Location signLoc, String gameName, GameStage gamestage, ArrayList<Location> teamspawns, long delay, long period) {
        if (plugin == null) {
            throw new IllegalArgumentException("Plugin cannot be null!");
        }
        if (!plugin.isEnabled()) {
            throw new IllegalArgumentException(plugin.getName() + " is not enabled and is trying to setup games!");
        }
        if (gameid < 1) {
            throw new IllegalArgumentException("Game ID cannot be less than 1, given: " + gameid);
        }
        if (maxPlayers < 1) {
            throw new IllegalArgumentException("Max players cannot be less than 1, given: " + maxPlayers);
        }
        if (gameName == null || gameName == "") {
            throw new IllegalArgumentException("The game's name cannot be null or blank!");
        }
        if (teamspawns == null || teamspawns.isEmpty()) {
            throw new IllegalArgumentException("The game's spawn points cannot be null or empty!");
        }
        this.plugin = plugin;
        this.gameid = gameid;
        this.maxPlayers = maxPlayers;
        this.signLoc = signLoc;
        this.gameName = gameName;
        this.gamestage = gamestage;
        this.defaultStage = gamestage;
        this.teamspawns = teamspawns;
        taskID = plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, this, delay, period);
    }

    @Override
    public final void run() {
        if (shutdown) {
            CTT.debug("Attempting to shutdown game " + getGameId() + " again");
            plugin.getServer().getScheduler().cancelTask(taskID);
            return;
        }
        signTracker++;
        if (signTracker == 2) {
            signTracker = 0;
            updateGameSign();
        }

        if (playerlist.size() == 0 && gamestage != defaultStage) {
            CTT.debug("Set game " + getGameId() + " to not running because there aren't any more players, setting to default game stage: " + defaultStage);
            setGameStage(defaultStage);
            setRunning(false);
        }

        if (!running && playerlist.size() > 0) {
            setRunning(true);
        }

        if (!running) {
            idleRun();
        }

        if (running) {
            gameRun();
        }
    }

    public abstract void idleRun();

    public abstract void gameRun();

    public abstract void addPlayer(Player player);

    public abstract void removePlayer(Player player);

    public abstract void updateSign(Location newLocation);

    public void updateGameSign() {
        if (signLoc == null)
            return;
        if (EventHandle.callGameSignUpdateEvent(this, signLoc).isCancelled())
            return;
        Block block = null;
        try {
            block = plugin.getServer().getWorld(signLoc.getWorld().getName()).getBlockAt(signLoc);
        } catch (NullPointerException e) {
            EventHandle.callGameSignNotFoundEvent(this, signLoc);
            signLoc = null;
            return;
        }
        if (block.getState() instanceof Sign) {
            Sign sign = (Sign) block.getState();
            String l = sign.getLine(2);
            int originalSize;
            int originalMax;
            try {
                originalSize = Integer.parseInt(l.split("/")[0]);
                originalMax = Integer.parseInt(l.split("/")[1]);
            } catch (Exception e) {
                return;
            }

            if (originalSize != playerlist.size()) {
                sign.setLine(2, playerlist.size() + "/" + maxPlayers);
            }

            if (originalMax != playerlist.size()) {
                sign.setLine(2, playerlist.size() + "/" + maxPlayers);
            }

            if (!sign.getLine(3).equalsIgnoreCase(getGameStage().toString())) {
                sign.setLine(3, gamestage.toString());
            }
            sign.update();
        } else {
            EventHandle.callGameSignNotFoundEvent(this, signLoc);
            signLoc = null;
            return;
        }
    }

    public int getGameId() {
        return gameid;
    }

    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        if (!EventHandle.callGameRunningChangeEvent(this, running).isCancelled()) {
            this.running = running;
        }
    }

    public ArrayList<Location> getTeamSpawns() {
        return teamspawns;
    }

    public ArrayList<String> getQueuedPlayers() {
        return queuedplayers;
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }

    public void setMaxPlayers(int maxPlayers) {
        this.maxPlayers = maxPlayers;
    }

    public GameStage getGameStage() {
        return gamestage;
    }

    public void setGameStage(GameStage gamestage) {
        if (this.gamestage != gamestage) {
            if (!EventHandle.callGameStageChangeEvent(this, gamestage).isCancelled()) {
                this.gamestage = gamestage;
            }
        }
    }

    public String getName() {
        return gameName;
    }

    public ArrayList<String> getPlayers() {
        return playerlist;
    }

    public void setSignLocation(Location signLoc) {
        this.signLoc = signLoc;
        this.updateSign(signLoc);
    }

    public Location getSignLocation() {
        return signLoc;
    }

    public void shutdown() {
        if (!EventHandle.callGameShutdownEvent(this).isCancelled()) {
            @SuppressWarnings("unchecked")
            ArrayList<String> list = (ArrayList<String>) getPlayers().clone();
            for (String i : list) {
                Player p = Bukkit.getPlayer(i);
                if (p == null)
                    continue;
                CTT.debug("Shutting down " + gameid + ", removing player " + p.getName());
                removePlayer(p);
            }
            shutdown = true;
            Bukkit.getScheduler().cancelTask(taskID);
        }
    }

    public void sendGameMessage(String message) {
        for (String i : getPlayers()) {
            Player p = Bukkit.getPlayer(i);
            if (p == null)
                continue;
            p.sendMessage(gameName + " " + ChatColor.RESET + message);
        }
    }

    public void sendTeamMessage(GameTeam team, String message) {
        for (String i : team.getPlayers()) {
            Player p = Bukkit.getPlayer(i);
            if (p == null)
                continue;
            p.sendMessage(gameName + " " + ChatColor.RESET + message);
        }
    }
}
