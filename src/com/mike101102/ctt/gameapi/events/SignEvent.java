package com.mike101102.ctt.gameapi.events;

import org.bukkit.Location;

public interface SignEvent {

    /**
     * Gets the sign's location involved in the event
     * 
     * @return Location
     */
    public Location getSignLocation();
}
