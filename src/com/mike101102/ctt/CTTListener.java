package com.mike101102.ctt;

import java.util.ArrayList;
import java.util.Map.Entry;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import com.mike101102.ctt.gameapi.Game;
import com.mike101102.ctt.gameapi.GameAPIMain;

public class CTTListener implements Listener {

    private CTT plugin;
    private ArrayList<String> okayNoArgCommands = new ArrayList<String>();
    private ArrayList<String> okayArgCommands = new ArrayList<String>();

    public CTTListener(CTT plugin) {
        this.plugin = plugin;
        okayNoArgCommands.add("/l");
        okayNoArgCommands.add("/leave");
        okayArgCommands.add("/j");
        okayArgCommands.add("/join");
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerDeath(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            if ((player.getHealth() - event.getDamage()) > 0)
                return;
            for (Entry<Integer, Game> en : GameAPIMain.getRunners().entrySet()) {
                if (!(en.getValue() instanceof CTTGame))
                    continue;
                CTTGame g = (CTTGame) en.getValue();
                int go = 0;
                for (ItemStack i : player.getInventory().getContents()) {
                    if (i == null) continue;
                    if (i.getType() == Material.GOLD_BLOCK) {
                        go += i.getAmount();
                    }
                }
                if (go > 0) {
                    boolean j = true;
                    if (event instanceof EntityDamageByEntityEvent) {
                        EntityDamageByEntityEvent e = (EntityDamageByEntityEvent) event;
                        if (e.getDamager() instanceof Player) {
                            ((Player) e.getDamager()).getInventory().addItem(new ItemStack(Material.GOLD_BLOCK, go));
                            j = false;
                        }
                    }
                    if (j) {
                        g.addBlocks(go);
                    }
                }
                g.resetPlayerInventory(player);
                if (g.getPlayers().contains(player.getName())) {
                    if (g.getBlueTeam().getPlayers().contains(player.getName())) {
                        player.teleport(g.getTeamSpawns().get(0));
                    } else {
                        player.teleport(g.getTeamSpawns().get(1));
                    }
                    event.setCancelled(true);
                    player.setHealth(player.getMaxHealth());
                    player.setSaturation(10f);
                    player.setFireTicks(1);
                    player.setFoodLevel(20);
                    if (event instanceof EntityDamageByEntityEvent && ((EntityDamageByEntityEvent) event).getDamager() instanceof Player) {
                        EntityDamageByEntityEvent ev = (EntityDamageByEntityEvent) event;
                        Player p = (Player) ev.getDamager();
                        g.sendGameMessage(plugin.getKillMessage().replaceAll("%killer%", p.getDisplayName()).replaceAll("%victim%", player.getDisplayName()));
                    } else {
                        g.sendGameMessage(player.getDisplayName() + ChatColor.GOLD + " has died");
                    }
                    CTT.debug(player.getName() + " has been killed, player reset complete");
                    return;
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        if (player.hasPermission("GameAPI.join")) {
            if (okayNoArgCommands.contains(event.getMessage().toLowerCase()))
                return;
            if (okayArgCommands.contains(event.getMessage().toLowerCase().split(" ")[0]))
                return;
            for (Entry<Integer, Game> en : GameAPIMain.getRunners().entrySet()) {
                if (!(en.getValue() instanceof CTTGame))
                    continue;
                CTTGame g = (CTTGame) en.getValue();
                if (g.getPlayers().contains(player.getName())) {
                    player.sendMessage(ChatColor.RED + "You can't use commands while in game! To leave, use /l");
                    event.setCancelled(true);
                    CTT.debug(player.getName() + " has been denied the use of the command: " + event.getMessage());
                    return;
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockDestroy(BlockBreakEvent event) {
        for (Entry<Integer, Game> en : GameAPIMain.getRunners().entrySet()) {
            if (en.getValue() instanceof CTTGame) {
                if (en.getValue().getPlayers().contains(event.getPlayer().getName())) {
                    if (event.getBlock().getType() != Material.GOLD_BLOCK) {
                        event.setCancelled(true);
                    } else {
                        event.setCancelled(true);
                        event.getBlock().setType(Material.AIR);
                        event.getPlayer().getInventory().addItem(new ItemStack(Material.GOLD_BLOCK));
                    }
                }
            }
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        for (Entry<Integer, Game> en : GameAPIMain.getRunners().entrySet()) {
            if (en.getValue() instanceof CTTGame) {
                if (en.getValue().getPlayers().contains(event.getPlayer().getName())) {
                    Block a = event.getBlockAgainst();
                    if (a.getType() != Material.OBSIDIAN && a.getType() != Material.GOLD_BLOCK) {
                        event.setCancelled(true);
                        event.getPlayer().updateInventory();
                    }
                }
            }
        }
    }
    
    @EventHandler()
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (plugin.creating_game_ids.containsKey(event.getPlayer().getName())) {
            CTT.debug("Cancelling creation for " + event.getPlayer().getName() + " because he/she has left the game");
            plugin.cancelCreation(event.getPlayer());
        }
    }
    
    @EventHandler
    public void onPlayerDrop(PlayerDropItemEvent event) {
        for (Entry<Integer, Game> en : GameAPIMain.getRunners().entrySet()) {
            if (en.getValue() instanceof CTTGame) {
                if (en.getValue().getPlayers().contains(event.getPlayer().getName())) {
                    event.setCancelled(true);
                }
            }
        }
    }
}
