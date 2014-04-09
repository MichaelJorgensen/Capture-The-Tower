package com.mike101102.ctt;

import java.util.List;

import org.bukkit.inventory.ItemStack;

public class Kit {

    private String name;
    private String perm;
    private List<ItemStack> contents;

    public Kit(String name, String perm, List<ItemStack> contents) {
        this.name = name;
        this.perm = perm;
        this.contents = contents;
        CTT.debug("Loaded kit: " + name);
    }

    public String getName() {
        return name;
    }

    public String getPermission() {
        return perm;
    }

    public List<ItemStack> getContents() {
        return contents;
    }
}
