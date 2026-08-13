package net.opmasterleo.packet.nms.shared;

import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;

import io.netty.channel.Channel;
import net.minecraft.network.Connection;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.opmasterleo.packet.nms.ConnectionBridge;

public final class SharedConnectionBridge implements ConnectionBridge {

    private static final String[] ANCHORS = {
            "packet_handler",
            "inbound_config",
            "outbound_config",
            "encoder",
            "decoder",
            "prepender",
            "compress"
    };

    @Override
    public Channel channel(Player player) {
        Object raw = connection(player);
        if (raw instanceof Connection connection && connection.channel != null) {
            return connection.channel;
        }
        return null;
    }

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

    @Override
    public String[] injectBeforeNames() {
        return ANCHORS.clone();
    }

    private static ServerPlayer nms(Player player) {
        return player instanceof CraftPlayer craft ? craft.getHandle() : null;
    }
}
