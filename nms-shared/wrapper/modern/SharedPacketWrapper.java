package org.mastersmp.packet.nms.shared;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.ChannelDuplexHandler;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.bukkit.entity.Player;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.mastersmp.packet.nms.PacketWrapper;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public final class SharedPacketWrapper implements PacketWrapper {

    private static final List<ActiveHandler> ACTIVE_HANDLERS = new CopyOnWriteArrayList<>();

    @Override
    public void injectHandler(Player player, PacketHandler handler) {
        ServerPlayer sp = handle(player);
        if (sp == null || sp.connection == null) {
            return;
        }

        Connection connection = sp.connection instanceof ServerGamePacketListenerImpl listener 
            ? listener.connection 
            : null;
        
        if (connection == null || connection.channel == null) {
            return;
        }

        ChannelPipeline pipeline = connection.channel.pipeline();
        String handlerName = handler.getName();

        // Remove existing handler if present
        if (pipeline.names().contains(handlerName)) {
            pipeline.remove(handlerName);
        }

        // Add new handler
        pipeline.addAfter("decoder", handlerName, new ChannelDuplexHandler() {
            @Override
            public void channelRead(ChannelHandlerContext ctx, Object msg) {
                if (msg instanceof Packet<?> packet) {
                    if (handler.onPacketReceive(packet, sp)) {
                        return; // Cancel packet
                    }
                }
                ctx.fireChannelRead(msg);
            }

            @Override
            public void write(ChannelHandlerContext ctx, Object msg, io.netty.channel.ChannelPromise promise) {
                if (msg instanceof Packet<?> packet) {
                    if (handler.onPacketSend(packet, sp)) {
                        return; // Cancel packet
                    }
                }
                ctx.write(msg, promise);
            }
        });

        ACTIVE_HANDLERS.add(new ActiveHandler(player.getUniqueId(), handlerName));
    }

    @Override
    public void removeHandler(Player player, String handlerName) {
        ServerPlayer sp = handle(player);
        if (sp == null || sp.connection == null) {
            return;
        }

        Connection connection = sp.connection instanceof ServerGamePacketListenerImpl listener 
            ? listener.connection 
            : null;
        
        if (connection == null || connection.channel == null) {
            return;
        }

        ChannelPipeline pipeline = connection.channel.pipeline();
        if (pipeline.names().contains(handlerName)) {
            pipeline.remove(handlerName);
        }

        ACTIVE_HANDLERS.removeIf(h -> h.playerId().equals(player.getUniqueId()) && h.handlerName().equals(handlerName));
    }

    @Override
    public Object getConnection(Player player) {
        ServerPlayer sp = handle(player);
        if (sp == null || sp.connection == null) {
            return null;
        }
        return sp.connection instanceof ServerGamePacketListenerImpl listener 
            ? listener.connection 
            : sp.connection;
    }

    @Override
    public Object getChannel(Player player) {
        ServerPlayer sp = handle(player);
        if (sp == null || sp.connection == null) {
            return null;
        }
        Connection connection = sp.connection instanceof ServerGamePacketListenerImpl listener 
            ? listener.connection 
            : null;
        return connection != null ? connection.channel : null;
    }

    @Override
    public boolean hasHandler(Player player, String handlerName) {
        ServerPlayer sp = handle(player);
        if (sp == null || sp.connection == null) {
            return false;
        }

        Connection connection = sp.connection instanceof ServerGamePacketListenerImpl listener 
            ? listener.connection 
            : null;
        
        if (connection == null || connection.channel == null) {
            return false;
        }

        return connection.channel.pipeline().names().contains(handlerName);
    }

    private static ServerPlayer handle(Player player) {
        if (player instanceof CraftPlayer craft) {
            return craft.getHandle();
        }
        return null;
    }

    private record ActiveHandler(java.util.UUID playerId, String handlerName) {}
}
