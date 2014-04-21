package com.mike101102.ctt;

import java.io.File;

import net.gravitydevelopment.updater.Updater;
import net.gravitydevelopment.updater.Updater.ReleaseType;
import net.gravitydevelopment.updater.Updater.UpdateResult;
import net.gravitydevelopment.updater.Updater.UpdateType;

import org.bukkit.scheduler.BukkitRunnable;

public class UpdateCheck extends BukkitRunnable {

    private CTT plugin;
    private File file;

    public UpdateCheck(CTT plugin, File file) {
        this.plugin = plugin;
        this.file = file;
    }

    @Override
    public void run() {
        Updater u;
        if (plugin.debug()) {
            CTT.debug("Checking for update...");
            u = new Updater(plugin, 76888, file, UpdateType.NO_DOWNLOAD, true);
        } else {
            u = new Updater(plugin, 76888, file, UpdateType.NO_DOWNLOAD, true);
        }
        if (u.getResult() == UpdateResult.UPDATE_AVAILABLE && u.getLatestType() == ReleaseType.RELEASE) {
            CTT.debug("Update Available");
            plugin.setNewUpdate(true);
            plugin.setUpdateInfo(new UpdateInfo(plugin.getDescription().getVersion(), u.getLatestName(), u.getLatestType()));
        } else {
            CTT.debug("No update: " + u.getResult().toString());
            plugin.setNewUpdate(false);
        }
    }
}
