package com.mike101102.ctt.gameapi;

import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.ChatColor;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.mike101102.ctt.CTT;
import com.mike101102.ctt.gameapi.events.EventHandle;
import com.mike101102.ctt.gameapi.events.player.PlayerGameListUpdateEvent;

public class GameListener implements Listener {

    public GameListener() {
    }

    @EventHandler()
    public void onPlayerQuit(PlayerQuitEvent event) {
        for (Map.Entry<Integer, Game> en : GameAPIMain.getRunners().entrySet()) {
            if (en.getValue().getPlayers().contains(event.getPlayer().getName())) {
                CTT.debug("Player " + event.getPlayer().getName() + " has left and is being kicked out of game " + en.getValue().getGameId());
                if (!EventHandle.callPlayerLeaveGameEvent(en.getValue(), event.getPlayer()).isCancelled()) {
                    en.getValue().removePlayer(event.getPlayer());
                }
                return;
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.getBlock().getState() instanceof Sign) {
            Sign sign = (Sign) event.getBlock().getState();
            if (sign.getLine(1).startsWith("Game ") && sign.getLine(2).contains("/")) {
                try {
                    int gameid = Integer.parseInt(sign.getLine(1).split("Game ")[1]);
                    if (GameAPIMain.getRunners().containsKey(gameid)) {
                        GameAPIMain.getRunners().get(gameid).setSignLocation(null);
                        event.getPlayer().sendMessage(ChatColor.GOLD + "Sign unregistered");
                        return;
                    }
                } catch (NumberFormatException e) {
                    return;
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (!event.getPlayer().hasPermission("ctt.join"))
                return;
            if (event.getClickedBlock().getState() instanceof Sign) {
                Sign sign = (Sign) event.getClickedBlock().getState();
                if (sign.getLine(1).startsWith("Game ") && sign.getLine(2).contains("/")) {
                    try {
                        int gameid = Integer.parseInt(sign.getLine(1).split("Game ")[1]);
                        if (GameAPIMain.getRunners().containsKey(gameid)) {
                            Game game = GameAPIMain.getRunners().get(gameid);
                            if (!game.getPlayers().contains(event.getPlayer().getName())) {
                                for (Entry<Integer, Game> en : GameAPIMain.getRunners().entrySet()) {
                                    if (en.getValue().getPlayers().contains(event.getPlayer().getName())) {
                                        event.getPlayer().sendMessage(ChatColor.RED + "You are already in game " + en.getValue().getGameId());
                                        event.setCancelled(true);
                                        return;
                                    }
                                }
                                if (!EventHandle.callPlayerJoinGameEvent(game, event.getPlayer()).isCancelled()) {
                                    game.addPlayer(event.getPlayer());
                                }
                                return;
                            } else {
                                event.getPlayer().sendMessage(ChatColor.RED + "You are already in this game!");
                                event.setCancelled(true);
                                return;
                            }
                        } else {
                            event.getPlayer().sendMessage(ChatColor.RED + "That game doesn't exist!");
                            event.setCancelled(true);
                            return;
                        }
                    } catch (Exception e) {
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onSignChange(SignChangeEvent event) {
        if (event.getLine(0).equalsIgnoreCase("[game]")) {
            Player player = event.getPlayer();
            if (player.hasPermission("ctt.create")) {
                try {
                    int gameid = Integer.parseInt(event.getLine(1));
                    if (GameAPIMain.getRunners().containsKey(gameid)) {
                        Game game = GameAPIMain.getRunners().get(gameid);
                        if (game.getSignLocation() == null) {
                            game.setSignLocation(event.getBlock().getLocation());
                            CTT.debug("New sign location for game " + gameid);
                            event.setLine(0, game.getName());
                            event.setLine(1, "Game " + gameid);
                            event.setLine(2, game.getPlayers().size() + "/" + game.getMaxPlayers());
                            event.setLine(3, game.getGameStage().toString());
                            player.sendMessage(ChatColor.GREEN + "Sign setup!");
                            return;
                        } else {
                            player.sendMessage(ChatColor.RED + "That game already has a sign!");
                            event.getBlock().breakNaturally();
                            return;
                        }
                    } else {
                        player.sendMessage(ChatColor.RED + event.getLine(1) + " is not a valid game ID!");
                        event.getBlock().breakNaturally();
                        return;
                    }
                } catch (NumberFormatException e) {
                    player.sendMessage(ChatColor.RED + event.getLine(1) + " is not a number!");
                    event.getBlock().breakNaturally();
                    return;
                }
            } else {
                player.sendMessage(ChatColor.RED + "You do not have permission (ctt.create)");
                event.getBlock().breakNaturally();
                return;
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerGameListUpdate(PlayerGameListUpdateEvent event) {
        if (event.getCurrentBookMeta() == null)
            return;
        if (event.getCurrentBookMeta().equals(event.getNewBookMeta())) {
            event.setCancelled(true);
        }
    }
}
