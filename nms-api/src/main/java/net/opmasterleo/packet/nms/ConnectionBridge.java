package net.opmasterleo.packet.nms;

import org.bukkit.entity.Player;

/**
 * Direct NMS connection access ({@code ServerGamePacketListenerImpl} / {@code PlayerConnection}).
 * Does not expose Netty; packet I/O goes through {@link PlayerBridge#send}.
 */
public interface ConnectionBridge {

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
}
