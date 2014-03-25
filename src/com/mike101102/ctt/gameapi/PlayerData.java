package com.mike101102.ctt.gameapi;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class PlayerData {

    private ItemStack[] player_inventory;
    private ItemStack[] player_armor;
    private GameMode player_gamemode;

    /**
     * Stores the player's inventory, armor, and gamemode for quick access later
     * (after the game is finished) so you can restore it without saving all of
     * the player's information.
     * 
     * A good way to store this variable is in a HashMap<String, PlayerData>
     * where the string is the player's name and the PlayerData is the player's
     * info needed to restore them to pre-game status.
     * 
     * @param player who's data is being stored
     */
    public PlayerData(Player player) {
        this.player_inventory = player.getInventory().getContents();
        this.player_armor = player.getInventory().getArmorContents();
        this.player_gamemode = player.getGameMode();
    }

    /**
     * Gets the contents of the player's inventory before the game
     * 
     * @return ItemStack[]
     */
    public ItemStack[] getPlayerInventory() {
        return player_inventory;
    }

    /**
     * Gets the contents of the player's armor before the game
     * 
     * @return ItemStack[]
     */
    public ItemStack[] getPlayerArmor() {
        return player_armor;
    }

    /**
     * Gets the gamemode of the player before the game
     * 
     * @return GameMode
     */
    public GameMode getPlayerGameMode() {
        return player_gamemode;
    }
}
