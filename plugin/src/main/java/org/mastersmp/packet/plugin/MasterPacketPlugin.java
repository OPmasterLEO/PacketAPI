package org.mastersmp.packet.plugin;

import org.bukkit.plugin.java.JavaPlugin;

import org.mastersmp.packet.PacketAPI;

public final class MasterPacketPlugin extends JavaPlugin {

    private PacketAPI api;

    @Override
    public void onEnable() {
        api = PacketAPI.bootstrap(this);
        getLogger().info("Loaded NMS adapter " + api.adapter().bucketId()
                + " on " + api.schedulers().platform().kind());
    }

    @Override
    public void onDisable() {
        if (api != null) {
            api.shutdown();
            api = null;
        }
    }

    public PacketAPI api() {
        return api;
    }
}
