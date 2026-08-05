package org.mastersmp.packet.event;

/**
 * Base class for NMS-level packet events.
 */
public abstract class NMSPacketEvent {

    private final Object nmsPacket;
    private final Object playerHandle;
    private boolean cancelled;

    protected NMSPacketEvent(Object nmsPacket, Object playerHandle) {
        this.nmsPacket = nmsPacket;
        this.playerHandle = playerHandle;
        this.cancelled = false;
    }

    /**
     * Gets the raw NMS packet object.
     * @return The NMS packet (net.minecraft.network.protocol.Packet)
     */
    public Object getNmsPacket() {
        return nmsPacket;
    }

    /**
     * Gets the player's NMS handle (ServerPlayer).
     * @return The ServerPlayer instance
     */
    public Object getPlayerHandle() {
        return playerHandle;
    }

    /**
     * Gets the packet class name for identification.
     * @return The simple class name of the packet
     */
    public String getPacketName() {
        return nmsPacket != null ? nmsPacket.getClass().getSimpleName() : "null";
    }

    /**
     * Checks if the packet send should be cancelled.
     * @return true if cancelled
     */
    public boolean isCancelled() {
        return cancelled;
    }

    /**
     * Sets whether to cancel the packet send.
     * @param cancelled true to cancel
     */
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
}
