package net.opmasterleo.packet.plugin;

import org.bukkit.plugin.java.JavaPlugin;

import net.opmasterleo.packet.PacketAPI;

public final class PacketPlugin extends JavaPlugin {

    private PacketAPI api;
    private NmsPlayerListener listener;

    @Override
    public void onEnable() {
        api = PacketAPI.bootstrap(this);
        listener = new NmsPlayerListener(api);
        listener.register(this);
        getLogger().info("Loaded NMS adapter " + api.adapter().bucketId()
                + " on " + api.schedulers().platform().kind()
                + " (direct NMS, no Netty intercept)");
    }

    @Override
    public void onDisable() {
        if (api != null) {
            api.shutdown();
            api = null;
        }
        listener = null;
    }

    public PacketAPI api() {
        return api;
    }
}
