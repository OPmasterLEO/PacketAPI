package org.mastersmp.packet.nms;

import org.bukkit.entity.Player;

/**
 * Interface for wrapping and handling NMS packets across different Minecraft versions.
 */
public interface PacketWrapper {

    /**
     * Injects a packet handler into the player's channel pipeline.
     * @param player The player to inject for
     * @param handler The packet handler to inject
     */
    void injectHandler(Player player, PacketHandler handler);

    /**
     * Removes a packet handler from the player's channel pipeline.
     * @param player The player to remove from
     * @param handlerName The name of the handler to remove
     */
    void removeHandler(Player player, String handlerName);

    /**
     * Gets the player's NMS connection object.
     * @param player The player
     * @return The NMS connection object
     */
    Object getConnection(Player player);

    /**
     * Gets the player's NMS channel.
     * @param player The player
     * @return The Netty channel
     */
    Object getChannel(Player player);

    /**
     * Checks if a handler is already injected.
     * @param player The player to check
     * @param handlerName The handler name
     * @return true if the handler exists
     */
    boolean hasHandler(Player player, String handlerName);

    /**
     * Interface for handling packets at the NMS level.
     */
    interface PacketHandler {
        /**
         * Called when a packet is received from the player.
         * @param packet The NMS packet
         * @param playerHandle The player's NMS handle (ServerPlayer)
         * @return true to cancel the packet, false to continue
         */
        boolean onPacketReceive(Object packet, Object playerHandle);

        /**
         * Called when a packet is sent to the player.
         * @param packet The NMS packet
         * @param playerHandle The player's NMS handle (ServerPlayer)
         * @return true to cancel the packet, false to continue
         */
        boolean onPacketSend(Object packet, Object playerHandle);

        /**
         * Gets the name of this handler.
         * @return The handler name
         */
        String getName();
    }
}
