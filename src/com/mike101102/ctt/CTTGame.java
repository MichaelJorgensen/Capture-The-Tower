package com.mike101102.ctt;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;

import com.mike101102.ctt.gameapi.Game;
import com.mike101102.ctt.gameapi.GameStage;
import com.mike101102.ctt.gameapi.PlayerData;
import com.mike101102.ctt.gameapi.sql.SQL;

public class CTTGame extends Game {

    private CTT plugin;
    private SQL sql;

    private int time = 0;
    private int goalCheck = 0;
    private int timeLimit;
    private int spawnDelay;

    private static ItemStack blueHelmet = new ItemStack(Material.WOOL, 1, (byte) 11);
    private static ItemStack redHelmet = new ItemStack(Material.WOOL, 1, (byte) 14);
    private static ItemStack chestplate = new ItemStack(Material.IRON_CHESTPLATE);
    private static ItemStack leggings = new ItemStack(Material.IRON_LEGGINGS);
    private static ItemStack boots = new ItemStack(Material.IRON_BOOTS);

    private Scoreboard board;
    private Score blueScore;
    private Score redScore;

    private String prefix = this.getName() + ChatColor.RESET;
    private boolean reset = false;
    private boolean lastKeepKits = false;
    private boolean con;

    private final HashMap<String, PlayerData> pd = new HashMap<String, PlayerData>();
    private HashMap<String, Kit> lastKits = new HashMap<String, Kit>();
    private final List<Kit> dK = new ArrayList<Kit>();

    private CTTTeam b = new CTTTeam(ChatColor.BLUE + "Blue Team");
    private CTTTeam r = new CTTTeam(ChatColor.RED + "Red Team");

    private Location bLoc1;
    private Location bLoc2;
    private Location bLoc3;
    private Location bLoc4;

    private Location rLoc1;
    private Location rLoc2;
    private Location rLoc3;
    private Location rLoc4;

    public CTTGame(CTT plugin, int gameid, int maxPlayers, int timeLimit, int spawnDelay, boolean con, Location signLoc, ArrayList<Location> teamspawns, Location blueGoal, Location redGoal) {
        super(plugin, gameid, maxPlayers, signLoc, ChatColor.GREEN + "[CTT]", GameStage.Waiting, teamspawns, 20L, 20L);
        this.plugin = plugin;
        sql = plugin.getSQL();
        board = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective obj = board.registerNewObjective("score", "tower");
        obj.setDisplayName("Blocks");
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);
        blueScore = obj.getScore(Bukkit.getOfflinePlayer(ChatColor.BLUE + "Blue"));
        redScore = obj.getScore(Bukkit.getOfflinePlayer(ChatColor.RED + "Red"));
        bLoc1 = blueGoal;
        bLoc2 = new Location(bLoc1.getWorld(), bLoc1.getX(), bLoc1.getY() + 1, bLoc1.getZ());
        bLoc3 = new Location(bLoc1.getWorld(), bLoc2.getX(), bLoc2.getY() + 1, bLoc2.getZ());
        bLoc4 = new Location(bLoc1.getWorld(), bLoc3.getX(), bLoc3.getY() + 1, bLoc3.getZ());
        rLoc1 = redGoal;
        rLoc2 = new Location(rLoc1.getWorld(), rLoc1.getX(), rLoc1.getY() + 1, rLoc1.getZ());
        rLoc3 = new Location(rLoc1.getWorld(), rLoc2.getX(), rLoc2.getY() + 1, rLoc2.getZ());
        rLoc4 = new Location(rLoc1.getWorld(), rLoc3.getX(), rLoc3.getY() + 1, rLoc3.getZ());
        this.timeLimit = timeLimit;
        this.spawnDelay = spawnDelay;
        this.con = con;
        resetGoalBlocks();
        for (Entry<String, Kit> en : plugin.getKits().entrySet()) {
            if (en.getValue().getPermission().equalsIgnoreCase("ctt.kit")) {
                dK.add(en.getValue());
            }
        }
        debug("Game is ready");
    }

    private void debug(String message) {
        CTT.debug("[GAME:" + getGameId() + "] " + message);
    }

    @Override
    public void addPlayer(Player player) {
        debug("Attempting to add player " + player.getName());
        if (getPlayers().size() >= getMaxPlayers()) {
            player.sendMessage(prefix + "This game is full");
            debug("Game is full, player denied");
            return;
        }
        player.setSaturation(10f);
        player.setFireTicks(0);
        player.setFlying(false);
        player.setFoodLevel(20);
        getPlayers().add(player.getName());
        if (lastKeepKits) {
            pd.put(player.getName(), new PlayerData(player, lastKits.get(player.getName())));
        } else {
            pd.put(player.getName(), new PlayerData(player, getKit(player.getName())));
        }
        if (b.getPlayers().size() > r.getPlayers().size()) {
            r.getPlayers().add(player.getName());
            resetPlayerInventory(player, pd.get(player.getName()).getKit());
            player.teleport(this.getTeamSpawns().get(1));
            if (!reset) {
                sendGameMessage(player.getDisplayName() + ChatColor.GOLD + " has joined the " + r.getName() + ChatColor.GOLD + "!");
            }
        } else {
            b.getPlayers().add(player.getName());
            resetPlayerInventory(player, pd.get(player.getName()).getKit());
            player.teleport(this.getTeamSpawns().get(0));
            if (!reset) {
                sendGameMessage(player.getDisplayName() + ChatColor.GOLD + " has joined the " + b.getName() + ChatColor.GOLD + "!");
            }
        }
        debug(player.getName() + " has been successfully added to the game");
    }

    @Override
    public void removePlayer(Player player) {
        debug("Attempting to remove player " + player.getName());
        int go = 0;
        for (ItemStack i : player.getInventory().getContents()) {
            if (i == null)
                continue;
            if (i.getType() == Material.GOLD_BLOCK) {
                go += i.getAmount();
            }
        }
        if (go > 0) {
            addBlocks(go);
        }
        plugin.getSpawnDelays().remove(player.getName());
        player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
        player.getInventory().clear();
        player.getInventory().setContents(pd.get(player.getName()).getPlayerInventory());
        player.getInventory().setArmorContents(pd.get(player.getName()).getPlayerArmor());
        GameMode m = pd.get(player.getName()).getPlayerGameMode();
        if (player.getGameMode() != m) {
            player.setGameMode(m);
        }
        player.updateInventory();
        pd.remove(player.getName());
        player.teleport(plugin.getDump());
        if (!reset) {
            sendGameMessage(player.getDisplayName() + ChatColor.RED + " has left the game");
        }
        getPlayers().remove(player.getName());
        if (b.getPlayers().contains(player.getName())) {
            b.getPlayers().remove(player.getName());
        } else {
            r.getPlayers().remove(player.getName());
        }
        debug(player.getName() + " has been removed successfully from the game");
    }

    public int getBlueScoreFromBlocks() {
        int b = 0;
        if (bLoc1.getBlock().getType() == Material.GOLD_BLOCK)
            b++;
        if (bLoc2.getBlock().getType() == Material.GOLD_BLOCK)
            b++;
        if (bLoc3.getBlock().getType() == Material.GOLD_BLOCK)
            b++;
        if (bLoc4.getBlock().getType() == Material.GOLD_BLOCK)
            b++;
        return b;
    }

    public int getRedScoreFromBlocks() {
        int r = 0;
        if (rLoc1.getBlock().getType() == Material.GOLD_BLOCK)
            r++;
        if (rLoc2.getBlock().getType() == Material.GOLD_BLOCK)
            r++;
        if (rLoc3.getBlock().getType() == Material.GOLD_BLOCK)
            r++;
        if (rLoc4.getBlock().getType() == Material.GOLD_BLOCK)
            r++;
        return r;
    }

    public void resetGoalBlocks() {
        bLoc1.getWorld().getBlockAt(bLoc1).setType(Material.GOLD_BLOCK);
        bLoc2.getWorld().getBlockAt(bLoc2).setType(Material.GOLD_BLOCK);
        bLoc3.getWorld().getBlockAt(bLoc3).setType(Material.AIR);
        bLoc4.getWorld().getBlockAt(bLoc4).setType(Material.AIR);

        rLoc1.getWorld().getBlockAt(rLoc1).setType(Material.GOLD_BLOCK);
        rLoc2.getWorld().getBlockAt(rLoc2).setType(Material.GOLD_BLOCK);
        rLoc3.getWorld().getBlockAt(rLoc3).setType(Material.AIR);
        rLoc4.getWorld().getBlockAt(rLoc4).setType(Material.AIR);
    }

    @Override
    public void gameRun() {
        if (getGameStage() != GameStage.Ingame) {
            setGameStage(GameStage.Ingame);
        }
        time++;
        b.setBlocks(getBlueScoreFromBlocks());
        r.setBlocks(getRedScoreFromBlocks());
        if (time >= timeLimit) {
            debug("Time limit reached");
            sendGameMessage(ChatColor.GOLD + "Time limit reached!");
            int bScore = b.getBlocks();
            int rScore = r.getBlocks();
            if (bScore > rScore) {
                this.sendGameMessage(ChatColor.BLUE + "Blue Team Wins!");
                blueTeamWin();
                resetGame(false, true);
                return;
            } else if (bScore < rScore) {
                this.sendGameMessage(ChatColor.RED + "Red Team Wins!");
                redTeamWin();
                resetGame(false, true);
                return;
            } else {
                this.sendGameMessage(ChatColor.GOLD + "We have a Tie!");
                resetGame(false, true);
                return;
            }
        }

        if (b.getBlocks() >= 4) {
            sendGameMessage(ChatColor.BLUE + "Blue team wins!");
            blueTeamWin();
            resetGame(false, true);
            return;
        } else if (r.getBlocks() >= 4) {
            sendGameMessage(ChatColor.RED + "Red team wins!");
            redTeamWin();
            resetGame(false, true);
            return;
        }

        if (b.getBlocks() != 2 || r.getBlocks() != 2) {
            if (b.getPlayers().size() == 0 || r.getPlayers().size() == 0) {
                sendGameMessage(ChatColor.GOLD + "A team has left!");
                debug("There is an empty team and the game is not reset, game is now resetting");
                resetGame(true, true);
            }
        }
        blueScore.setScore(b.getBlocks());
        redScore.setScore(r.getBlocks());
        for (String i : getPlayers()) {
            Player p = Bukkit.getPlayer(i);
            if (p == null) {
                continue;
            } else {
                p.setScoreboard(board);
            }
        }

        int left = timeLimit - time;
        if (left == 10) {
            this.sendGameMessage(ChatColor.RED + "10 seconds left");
        } else if (left <= 3 && left > 0) {
            this.sendGameMessage(ChatColor.RED.toString() + left);
        }

        ArrayList<String> keysToRemove = new ArrayList<String>();
        for (Entry<String, Integer> en : plugin.getSpawnDelays().entrySet()) {
            if (getPlayers().contains(en.getKey())) {
                plugin.getSpawnDelays().put(en.getKey(), en.getValue() - 1);
                Player p = Bukkit.getPlayer(en.getKey());
                int delay = en.getValue();
                if (delay > 0) {
                    p.sendMessage(ChatColor.RED + "Spawn delay: " + ChatColor.GOLD + plugin.getSpawnDelays().get(p.getName()) + ChatColor.RED + " more seconds");
                }
                if (delay < 1) {
                    keysToRemove.add(en.getKey());
                }
            }
        }
        for (String i : keysToRemove) {
            debug("Removing " + i + " from spawn delay list");
            plugin.getSpawnDelays().remove(i);
        }
    }

    @Override
    public void idleRun() {
        goalCheck++;
        if (time != 0)
            time = 0;
        if (reset)
            reset = false;
        if (goalCheck >= 10) {
            resetGoalBlocks();
            goalCheck = 0;
        }
    }

    @Override
    public void updateSign(Location l) {
        debug("Attempting to update sign location");
        try {
            if (l != null) {
                sql.query("UPDATE ctt SET sx=" + l.getX() + ", sy=" + l.getY() + ", sz=" + l.getZ() + ", signworld='" + l.getWorld().getName() + "' WHERE gameid=" + getGameId());
            } else {
                sql.query("UPDATE ctt SET sx=0, sy=0, sz=0, signworld='UNKNOWN-WORLD' WHERE gameid=" + getGameId());
            }
        } catch (SQLException e) {
            e.printStackTrace();
            debug("Sign location update failed");
            return;
        }
        debug("Sign location updated");
    }

    private void blueTeamWin() {
        for (String n : this.getPlayers()) {
            if (this.getBlueTeam().getPlayers().contains(n)) {
                PlayerStats s = plugin.getPlayerStats().get(n);
                if (s != null) {
                    s.setWins(s.getWins() + 1);
                } else {
                    s = new PlayerStats(n, 1, 0, 0, 0);
                }
                plugin.getPlayerStats().put(n, s);
            } else {
                PlayerStats s = plugin.getPlayerStats().get(n);
                if (s != null) {
                    s.setLosses(s.getLosses() + 1);
                } else {
                    s = new PlayerStats(n, 0, 1, 0, 0);
                }
                plugin.getPlayerStats().put(n, s);
            }
        }
    }

    private void redTeamWin() {
        for (String n : this.getPlayers()) {
            if (this.getBlueTeam().getPlayers().contains(n)) {
                PlayerStats s = plugin.getPlayerStats().get(n);
                if (s != null) {
                    s.setLosses(s.getLosses() + 1);
                } else {
                    s = new PlayerStats(n, 0, 1, 0, 0);
                }
                plugin.getPlayerStats().put(n, s);
            } else {
                PlayerStats s = plugin.getPlayerStats().get(n);
                if (s != null) {
                    s.setWins(s.getWins() + 1);
                } else {
                    s = new PlayerStats(n, 1, 0, 0, 0);
                }
                plugin.getPlayerStats().put(n, s);
            }
        }
    }

    public void resetScores() {
        b.setBlocks(2);
        r.setBlocks(2);
        resetGoalBlocks();
        debug("Scores reset");
    }

    public CTTTeam getBlueTeam() {
        return b;
    }

    public CTTTeam getRedTeam() {
        return r;
    }

    public void resetPlayerInventory(Player player) {
        player.getInventory().clear();
        if (b.getPlayers().contains(player.getName())) {
            player.getInventory().setHelmet(blueHelmet);
        } else {
            player.getInventory().setHelmet(redHelmet);
        }

        if (player.getGameMode() != GameMode.SURVIVAL) {
            player.setGameMode(GameMode.SURVIVAL);
        }
        Kit k = getKit(player.getName());
        if (k != null) {
            debug("Giving " + player.getName() + " kit " + k.getName());
            for (ItemStack h : k.getContents()) {
                player.getInventory().addItem(h);
            }
        }
        player.getInventory().setChestplate(chestplate);
        player.getInventory().setLeggings(leggings);
        player.getInventory().setBoots(boots);
        player.getInventory().setHeldItemSlot(0);
        player.updateInventory();
    }

    public void resetPlayerInventory(Player player, Kit k) {
        if (k == null) {
            debug("Kit provided is null, resetting for player " + player.getName());
            resetPlayerInventory(player);
            return;
        }
        debug("Resetting player inv, given kit: " + k.getName());
        player.getInventory().clear();
        if (b.getPlayers().contains(player.getName())) {
            player.getInventory().setHelmet(blueHelmet);
        } else {
            player.getInventory().setHelmet(redHelmet);
        }

        if (player.getGameMode() != GameMode.SURVIVAL) {
            player.setGameMode(GameMode.SURVIVAL);
        }
        if (k != null) {
            debug("Giving " + player.getName() + " kit " + k.getName());
            for (ItemStack h : k.getContents()) {
                player.getInventory().addItem(h);
            }
        }
        player.getInventory().setChestplate(chestplate);
        player.getInventory().setLeggings(leggings);
        player.getInventory().setBoots(boots);
        player.getInventory().setHeldItemSlot(0);
        player.updateInventory();
        pd.get(player.getName()).setKit(k);
    }

    public void addBlocks(int b) {
        debug("Adding " + b + " blocks to the game");
        if (reset) {
            debug("Blocks not added, middle of reset");
            return;
        }
        int bS;
        int rS;
        int c = 0;
        for (int i = 0; i < b; i++) {
            bS = this.getBlueScoreFromBlocks();
            rS = this.getRedScoreFromBlocks();
            if (bS == rS) {
                if (new Random().nextInt(1) == 1) {
                    addBlockToBlue();
                } else {
                    addBlockToRed();
                }
                c++;
                continue;
            } else if (bS > rS) {
                addBlockToRed();
                c++;
                continue;
            } else {
                addBlockToBlue();
                c++;
                continue;
            }
        }
        debug("Added " + c + " blocks to the game");
    }

    private void addBlockToBlue() {
        debug("Adding block to blue");
        if (bLoc1.getBlock().getType() == Material.AIR) {
            bLoc1.getBlock().setType(Material.GOLD_BLOCK);
        } else if (bLoc2.getBlock().getType() == Material.AIR) {
            bLoc2.getBlock().setType(Material.GOLD_BLOCK);
        } else if (bLoc3.getBlock().getType() == Material.AIR) {
            bLoc3.getBlock().setType(Material.GOLD_BLOCK);
        } else if (bLoc4.getBlock().getType() == Material.AIR) {
            bLoc4.getBlock().setType(Material.GOLD_BLOCK);
        }
    }

    private void addBlockToRed() {
        debug("Adding block to red");
        if (rLoc1.getBlock().getType() == Material.AIR) {
            rLoc1.getBlock().setType(Material.GOLD_BLOCK);
        } else if (rLoc2.getBlock().getType() == Material.AIR) {
            rLoc2.getBlock().setType(Material.GOLD_BLOCK);
        } else if (rLoc3.getBlock().getType() == Material.AIR) {
            rLoc3.getBlock().setType(Material.GOLD_BLOCK);
        } else if (rLoc4.getBlock().getType() == Material.AIR) {
            rLoc4.getBlock().setType(Material.GOLD_BLOCK);
        }
    }

    public Kit getKit(String p) {
        if (getPlayers().contains(p)) {
            if (pd.get(p) != null) {
                if (pd.get(p).getKit() != null) {
                    return pd.get(p).getKit();
                }
            }
            return dK.get(new Random().nextInt(dK.size()));
        } else {
            return null;
        }
    }

    public HashMap<String, PlayerData> getPlayerData() {
        return pd;
    }

    public int getSpawnDelay() {
        return spawnDelay;
    }

    public void resetGame(boolean message, boolean keepKits) {
        debug("Resetting game " + getGameId());
        reset = true;
        lastKeepKits = keepKits;
        time = 0;
        resetScores();
        lastKits.clear();
        for (Entry<String, PlayerData> en : pd.entrySet()) {
            lastKits.put(en.getKey(), en.getValue().getKit());
        }
        @SuppressWarnings("unchecked")
        ArrayList<String> list = (ArrayList<String>) this.getPlayers().clone();
        if (message && con)
            sendGameMessage(ChatColor.AQUA.toString() + ChatColor.MAGIC + "|" + ChatColor.AQUA + "Resetting game for a new round" + ChatColor.MAGIC + "|");
        for (String i : list) {
            removePlayer(Bukkit.getPlayer(i));
        }
        if (con) {
            debug("Shuffling teams..");
            Collections.shuffle(list);
            for (String i : list) {
                addPlayer(Bukkit.getPlayer(i));
            }
        }
        reset = false;
        lastKeepKits = false;
        lastKits.clear();
        debug("Reset complete");
    }
}
