package com.mike101102.ctt.gameapi.events;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.BookMeta;

import com.mike101102.ctt.gameapi.Game;
import com.mike101102.ctt.gameapi.GameStage;
import com.mike101102.ctt.gameapi.events.game.GameRunnerAddEvent;
import com.mike101102.ctt.gameapi.events.game.GameRunnerRemoveEvent;
import com.mike101102.ctt.gameapi.events.game.GameRunningChangeEvent;
import com.mike101102.ctt.gameapi.events.game.GameShutdownEvent;
import com.mike101102.ctt.gameapi.events.game.GameStageChangeEvent;
import com.mike101102.ctt.gameapi.events.player.PlayerGameListUpdateEvent;
import com.mike101102.ctt.gameapi.events.player.PlayerJoinGameEvent;
import com.mike101102.ctt.gameapi.events.player.PlayerLeaveGameEvent;
import com.mike101102.ctt.gameapi.events.sign.GameSignNotFoundEvent;
import com.mike101102.ctt.gameapi.events.sign.GameSignUpdateEvent;

public class EventHandle {

    public static GameSignUpdateEvent callGameSignUpdateEvent(Game game, Location signLoc) {
        GameSignUpdateEvent event = new GameSignUpdateEvent(game, signLoc);
        event.call();
        return event;
    }

    public static GameSignNotFoundEvent callGameSignNotFoundEvent(Game game, Location signLoc) {
        GameSignNotFoundEvent event = new GameSignNotFoundEvent(game, signLoc);
        event.call();
        return event;
    }

    public static GameRunningChangeEvent callGameRunningChangeEvent(Game game, boolean newRunState) {
        GameRunningChangeEvent event = new GameRunningChangeEvent(game, newRunState);
        event.call();
        return event;
    }

    public static GameRunnerAddEvent callGameRunnerAddEvent(Game game) {
        GameRunnerAddEvent event = new GameRunnerAddEvent(game);
        event.call();
        return event;
    }

    public static GameRunnerRemoveEvent callGameRunnerRemoveEvent(Game game) {
        GameRunnerRemoveEvent event = new GameRunnerRemoveEvent(game);
        event.call();
        return event;
    }

    public static GameShutdownEvent callGameShutdownEvent(Game game) {
        GameShutdownEvent event = new GameShutdownEvent(game);
        event.call();
        return event;
    }

    public static GameStageChangeEvent callGameStageChangeEvent(Game game, GameStage newGameStage) {
        GameStageChangeEvent event = new GameStageChangeEvent(game, newGameStage);
        event.call();
        return event;
    }

    public static PlayerJoinGameEvent callPlayerJoinGameEvent(Game game, Player player) {
        PlayerJoinGameEvent event = new PlayerJoinGameEvent(game, player);
        event.call();
        return event;
    }

    public static PlayerLeaveGameEvent callPlayerLeaveGameEvent(Game game, Player player) {
        PlayerLeaveGameEvent event = new PlayerLeaveGameEvent(game, player);
        event.call();
        return event;
    }

    public static PlayerGameListUpdateEvent callPlayerGameListUpdateEvent(Player player, BookMeta currentBookMeta, BookMeta newBookMeta) {
        PlayerGameListUpdateEvent event = new PlayerGameListUpdateEvent(player, currentBookMeta, newBookMeta);
        event.call();
        return event;
    }
}
