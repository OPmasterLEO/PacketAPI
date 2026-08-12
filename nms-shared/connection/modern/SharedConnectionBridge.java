package org.mastersmp.packet.nms.shared;

import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;

import net.minecraft.network.Connection;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.mastersmp.packet.nms.ConnectionBridge;

public final class SharedConnectionBridge implements ConnectionBridge {

    @Override
    public Object connection(Player player) {
        ServerPlayer sp = nms(player);
        if (sp == null || sp.connection == null) {
            return null;
        }
        if (sp.connection instanceof ServerGamePacketListenerImpl listener) {
            return listener.connection;
        }
        return sp.connection;
    }

    @Override
    public Object listener(Player player) {
        ServerPlayer sp = nms(player);
        return sp == null ? null : sp.connection;
    }

    @Override
    public int latency(Player player) {
        ServerPlayer sp = nms(player);
        if (sp == null || sp.connection == null) {
            return 0;
        }
        return sp.connection.latency();
    }

    @Override
    public boolean accepting(Player player) {
        ServerPlayer sp = nms(player);
        return sp != null && sp.connection != null && sp.connection.isAcceptingMessages();
    }

    private static ServerPlayer nms(Player player) {
        return player instanceof CraftPlayer craft ? craft.getHandle() : null;
    }
}
