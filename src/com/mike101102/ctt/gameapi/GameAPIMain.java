package com.mike101102.ctt.gameapi;

import java.util.HashMap;
import java.util.Map.Entry;

import org.bukkit.inventory.ItemStack;

import com.mike101102.ctt.gameapi.events.EventHandle;

public class GameAPIMain {

    public static ItemStack gameBook;
    private static HashMap<Integer, Game> runningGames = new HashMap<Integer, Game>();

    public static void onDisable() {
        send("Shutting down games");
        for (Entry<Integer, Game> en : getRunners().entrySet()) {
            en.getValue().shutdown();
        }
        send("Games shutdown");
    }

    /**
     * Adds the game to the running list <b>This is required for the game to
     * function properly</b>
     * 
     * @param game to be added
     * @return boolean true if the game was added, otherwise false
     */
    public static boolean addRunner(Game game) {
        if (!EventHandle.callGameRunnerAddEvent(game).isCancelled()) {
            runningGames.put(game.getGameId(), game);
            return true;
        }
        return false;
    }

    /**
     * Removes the game from the running list
     * 
     * @param gameid (key) to remove
     * @return boolean true if the game was removed, otherwise false
     */
    public static boolean removeRunner(int gameid) {
        if (!EventHandle.callGameRunnerRemoveEvent(getRunners().get(gameid)).isCancelled()) {
            getRunners().get(gameid).shutdown();
            runningGames.remove(gameid);
            return true;
        }
        return false;
    }

    /**
     * Gets the mapping of the current running games
     * 
     * @return HashMap<Integer, Game>
     */
    public static HashMap<Integer, Game> getRunners() {
        return runningGames;
    }

    public static void send(String message) {
        System.out.println("[GameAPI] " + message);
    }
}
