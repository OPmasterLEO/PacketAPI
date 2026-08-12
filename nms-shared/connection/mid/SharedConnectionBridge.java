package net.opmasterleo.packet.nms.shared;

import org.bukkit.craftbukkit.NMS.entity.CraftPlayer;
import org.bukkit.entity.Player;

import net.minecraft.network.Connection;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.opmasterleo.packet.nms.ConnectionBridge;

import static net.opmasterleo.packet.nms.shared.Reflect.field;
import static net.opmasterleo.packet.nms.shared.Reflect.get;
import static net.opmasterleo.packet.nms.shared.Reflect.invoke;

public final class SharedConnectionBridge implements ConnectionBridge {

    @Override
    public Object connection(Player player) {
        ServerPlayer sp = nms(player);
        if (sp == null || sp.connection == null) {
            return null;
        }
        if (sp.connection instanceof ServerGamePacketListenerImpl listener) {
            Object value = get(field(listener.getClass(), "connection"), listener);
            return value instanceof Connection connection ? connection : listener;
        }
        Object value = get(field(sp.connection.getClass(), "connection", "networkManager"), sp.connection);
        return value instanceof Connection connection ? connection : sp.connection;
    }

    @Override
    public Object listener(Player player) {
        ServerPlayer sp = nms(player);
        return sp == null ? null : sp.connection;
    }

    @Override
    public int latency(Player player) {
        ServerPlayer sp = nms(player);
        if (sp == null) {
            return 0;
        }
        Object ping = invoke(sp, "latency", "getPing");
        if (ping instanceof Integer i) {
            return i;
        }
        Object listener = sp.connection;
        ping = invoke(listener, "latency", "getLatency");
        if (ping instanceof Integer i) {
            return i;
        }
        return player.getPing();
    }

    @Override
    public boolean accepting(Player player) {
        ServerPlayer sp = nms(player);
        if (sp == null || sp.connection == null) {
            return false;
        }
        Object accepting = invoke(sp.connection, "isAcceptingMessages");
        if (accepting instanceof Boolean b) {
            return b;
        }
        return player.isOnline();
    }

    private static ServerPlayer nms(Player player) {
        return player instanceof CraftPlayer craft ? craft.getHandle() : null;
    }
}
