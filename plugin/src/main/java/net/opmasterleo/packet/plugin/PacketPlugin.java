package net.opmasterleo.packet.plugin;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import net.opmasterleo.packet.PacketAPI;
import net.opmasterleo.packet.channel.InjectionProbe;

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
                + " (injection id=" + api.listeners().identity().instanceToken() + ")");
        reportInjectionConflicts();
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

    /**
     * Status/debug path: surface foreign PacketAPI injections without reading logs.
     */
    public void reportInjectionConflicts() {
        if (api == null) {
            return;
        }
        int foreign = 0;
        for (Player player : Bukkit.getOnlinePlayers()) {
            InjectionProbe.Result result = api.detectInjectionConflicts(player);
            if (result.state() == InjectionProbe.State.FOREIGN
                    || result.state() == InjectionProbe.State.OWN_AND_FOREIGN) {
                foreign++;
                getLogger().warning("Injection conflict: " + api.injectionStatus(player));
            }
        }
        if (foreign == 0) {
            getLogger().info("PacketAPI injection probe: no foreign handlers on online players");
        } else {
            getLogger().warning("PacketAPI injection probe: " + foreign + " player(s) have foreign PacketAPI handlers");
        }
    }
}
