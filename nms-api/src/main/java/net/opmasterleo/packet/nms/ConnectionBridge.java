package net.opmasterleo.packet.nms;

import org.bukkit.entity.Player;

import io.netty.channel.Channel;

/**
 * Direct NMS connection access ({@code ServerGamePacketListenerImpl} / {@code PlayerConnection}).
 * Packet I/O for sending goes through {@link PlayerBridge#send}; {@link #channel(Player)} exists
 * only for optional Netty pipeline injection ownership checks.
 */
public interface ConnectionBridge {

    /**
     * Player Netty {@link Channel}, or {@code null} if unavailable.
     */
    Channel channel(Player player);

    /**
     * NMS {@code Connection} / {@code NetworkManager}.
     */
    Object connection(Player player);

    /**
     * NMS {@code ServerGamePacketListenerImpl} / {@code PlayerConnection}.
     */
    Object listener(Player player);

    int latency(Player player);

    boolean accepting(Player player);

    /**
     * Preferred pipeline handler names to inject before (first existing wins).
     */
    String[] injectBeforeNames();
}
