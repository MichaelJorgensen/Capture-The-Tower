package com.mike101102.ctt.gameapi.events;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import com.mike101102.ctt.CTT;
import com.mike101102.ctt.gameapi.Game;
import com.mike101102.ctt.gameapi.events.game.GameRunnerAddEvent;
import com.mike101102.ctt.gameapi.events.game.GameRunnerRemoveEvent;
import com.mike101102.ctt.gameapi.events.game.GameRunningChangeEvent;
import com.mike101102.ctt.gameapi.events.game.GameShutdownEvent;
import com.mike101102.ctt.gameapi.events.game.GameStageChangeEvent;
import com.mike101102.ctt.gameapi.events.player.PlayerGameListUpdateEvent;
import com.mike101102.ctt.gameapi.events.player.PlayerJoinGameEvent;
import com.mike101102.ctt.gameapi.events.player.PlayerLeaveGameEvent;
import com.mike101102.ctt.gameapi.events.sign.GameSignNotFoundEvent;

public class DebugListener implements Listener {

    public DebugListener() {
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onGameSignNotFound(GameSignNotFoundEvent event) {
        CTT.debug("Game sign not found for game " + event.getGame().getGameId());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onGameRunningChange(GameRunningChangeEvent event) {
        Game game = event.getGame();
        CTT.debug("Changed running state of " + game.getGameId() + " from " + game.isRunning() + " to " + event.getNewRunState());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onGameRunnerAdd(GameRunnerAddEvent event) {
        CTT.debug("Game " + event.getGame().getGameId() + " added to the list of runners");
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onGameRunnerRemove(GameRunnerRemoveEvent event) {
        CTT.debug("Game " + event.getGame().getGameId() + " removed from the list of runners");
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onGameShutdown(GameShutdownEvent event) {
        CTT.debug("Shutting down game " + event.getGame().getGameId());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onGameStageChange(GameStageChangeEvent event) {
        Game game = event.getGame();
        CTT.debug("Game stage updated for " + game.getGameId() + ", changed from " + game.getGameStage() + " to " + event.getNewGameStage());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerJoinGame(PlayerJoinGameEvent event) {
        CTT.debug("Player " + event.getPlayer().getName() + " has joined game " + event.getGame().getGameId());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerLeaveGame(PlayerLeaveGameEvent event) {
        CTT.debug("Player " + event.getPlayer().getName() + " has left game " + event.getGame().getGameId());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerGameListUpdate(PlayerGameListUpdateEvent event) {
        CTT.debug("Player " + event.getPlayer().getName() + " is receiving an update for their game list book");
    }
}
