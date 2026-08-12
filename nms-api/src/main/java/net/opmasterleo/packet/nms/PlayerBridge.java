package net.opmasterleo.packet.nms;

import org.bukkit.entity.Player;

/**
 * Direct {@code ServerPlayer} / {@code EntityPlayer} access via CraftBukkit {@code CraftPlayer}.
 */
public interface PlayerBridge {

    /**
     * NMS player handle ({@code ServerPlayer} / {@code EntityPlayer}).
     */
    Object handle(Player player);

    /**
     * Send an NMS packet through {@code ServerPlayer.connection.send} / {@code PlayerConnection.sendPacket}.
     */
    void send(Player player, Object nmsPacket);

    boolean isConnected(Player player);

    default int entityId(Player player) {
        return -1;
    }

    /**
     * NMS {@code ServerLevel} / {@code WorldServer} the player is in.
     */
    default Object level(Player player) {
        return null;
    }
}
