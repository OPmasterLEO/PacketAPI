package org.mastersmp.packet.nms.shared;

import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;

import io.netty.channel.Channel;
import net.minecraft.network.Connection;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.mastersmp.packet.nms.ConnectionBridge;

import static org.mastersmp.packet.nms.shared.Reflect.field;
import static org.mastersmp.packet.nms.shared.Reflect.get;

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
        ServerPlayer sp = handle(player);
        if (sp == null) {
            return null;
        }
        Connection connection = resolveConnection(sp);
        if (connection != null && connection.channel != null) {
            return connection.channel;
        }
        return channelByAddress(player);
    }

    @Override
    public Object connection(Player player) {
        ServerPlayer sp = handle(player);
        return sp == null ? null : resolveConnection(sp);
    }

    @Override
    public Object gamePacketListener(Player player) {
        ServerPlayer sp = handle(player);
        return sp == null ? null : sp.connection;
    }

    @Override
    public String[] injectBeforeNames() {
        return ANCHORS.clone();
    }

    private static ServerPlayer handle(Player player) {
        if (player instanceof CraftPlayer craft) {
            return craft.getHandle();
        }
        return null;
    }

    private static Connection resolveConnection(ServerPlayer sp) {
        if (sp.connection instanceof ServerGamePacketListenerImpl listener) {
            return listener.connection;
        }
        Object value = get(field(sp.connection.getClass(), "connection"), sp.connection);
        return value instanceof Connection connection ? connection : null;
    }

    private static Channel channelByAddress(Player player) {
        if (player == null || player.getAddress() == null) {
            return null;
        }
        try {
            var address = player.getAddress().getAddress();
            for (Connection connection : MinecraftServer.getServer().getConnection().getConnections()) {
                if (connection == null || connection.channel == null) {
                    continue;
                }
                if (connection.getRemoteAddress() instanceof java.net.InetSocketAddress remote
                        && address.equals(remote.getAddress())) {
                    return connection.channel;
                }
            }
        } catch (Throwable ignored) {
        }
        return null;
    }
}
