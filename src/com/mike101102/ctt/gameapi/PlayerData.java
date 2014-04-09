package com.mike101102.ctt.gameapi;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.mike101102.ctt.CTT;
import com.mike101102.ctt.Kit;

public class PlayerData {

    private ItemStack[] player_inventory;
    private ItemStack[] player_armor;
    private GameMode player_gamemode;
    private Kit k;

    public PlayerData(Player player, Kit k) {
        if (k == null)
            CTT.debug("Player " + player.getName() + "'s kit it null");
        this.player_inventory = player.getInventory().getContents();
        this.player_armor = player.getInventory().getArmorContents();
        this.player_gamemode = player.getGameMode();
        this.k = k;
        CTT.debug("New PlayerData for " + player.getName() + ", kit: " + k.getName());
    }

    public ItemStack[] getPlayerInventory() {
        return player_inventory;
    }

    public ItemStack[] getPlayerArmor() {
        return player_armor;
    }

    public GameMode getPlayerGameMode() {
        return player_gamemode;
    }

    public Kit getKit() {
        return k;
    }

    public void setKit(Kit k) {
        this.k = k;
    }
}
