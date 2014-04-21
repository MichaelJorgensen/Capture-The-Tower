package com.mike101102.ctt;

import net.gravitydevelopment.updater.Updater.ReleaseType;

public class UpdateInfo {

    private String current;
    private String newest;
    private ReleaseType type;

    public UpdateInfo(String current, String newest, ReleaseType type) {
        this.current = current;
        this.newest = newest.split("CaptureTheTower v")[1];
        this.type = type;
    }

    public String getCurrentVersion() {
        return current;
    }

    public String getNewestVersion() {
        return newest;
    }

    public ReleaseType getReleaseType() {
        return type;
    }
}
