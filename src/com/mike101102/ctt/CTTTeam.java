package com.mike101102.ctt;

import com.mike101102.ctt.gameapi.GameTeam;

public class CTTTeam extends GameTeam {

    private int blocks = 0;

    public CTTTeam(String name) {
        super(name);
    }

    public int getBlocks() {
        return blocks;
    }

    public void setBlocks(int blocks) {
        this.blocks = blocks;
    }
}
