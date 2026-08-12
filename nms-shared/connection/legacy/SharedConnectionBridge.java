package net.opmasterleo.packet.nms.shared;

import org.bukkit.craftbukkit.NMS.entity.CraftPlayer;
import org.bukkit.entity.Player;

import net.minecraft.server.NMS.EntityPlayer;
import net.opmasterleo.packet.nms.ConnectionBridge;

public final class SharedConnectionBridge implements ConnectionBridge {

    @Override
    public Object connection(Player player) {
        EntityPlayer sp = nms(player);
        return sp == null || sp.playerConnection == null ? null : sp.playerConnection.networkManager;
    }

    @Override
    public Object listener(Player player) {
        EntityPlayer sp = nms(player);
        return sp == null ? null : sp.playerConnection;
    }

    @Override
    public int latency(Player player) {
        EntityPlayer sp = nms(player);
        return sp == null ? 0 : sp.ping;
    }

    @Override
    public boolean accepting(Player player) {
        EntityPlayer sp = nms(player);
        return sp != null && sp.playerConnection != null && player.isOnline();
    }

    private static EntityPlayer nms(Player player) {
        return player instanceof CraftPlayer craft ? craft.getHandle() : null;
    }
}
