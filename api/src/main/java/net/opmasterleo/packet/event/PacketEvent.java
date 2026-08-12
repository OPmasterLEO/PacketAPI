package net.opmasterleo.packet.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.ApiStatus;
import net.opmasterleo.packet.nms.packet.PacketWrap;

/**
 * Parent event for NMS packet traffic sent through PacketAPI.
 * Listen to this to capture every packet event, or to {@link PacketSendEvent} only.
 */
public abstract class PacketEvent extends Event implements Cancellable {

    private static final HandlerList HANDLER_LIST = new HandlerList();

    private final Player player;
    private final Object playerHandle;
    private Object packet;
    private boolean cancelled;

    @ApiStatus.Internal
    protected PacketEvent(Player player, Object packet, Object playerHandle) {
        super(false);
        this.player = player;
        this.packet = packet;
        this.playerHandle = playerHandle;
    }

    public Player getPlayer() {
        return player;
    }

    /**
     * NMS {@code ServerPlayer} / {@code EntityPlayer}.
     */
    public Object getPlayerHandle() {
        return playerHandle;
    }

    /**
     * Raw NMS packet ({@code net.minecraft.network.protocol.Packet}).
     */
    public Object getPacket() {
        return packet;
    }

    public void setPacket(Object packet) {
        this.packet = packet;
    }

    public PacketWrap wrap() {
        return packet == null ? null : PacketWrap.of(packet);
    }

    public String getPacketName() {
        return packet == null ? "null" : packet.getClass().getSimpleName();
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }
}
