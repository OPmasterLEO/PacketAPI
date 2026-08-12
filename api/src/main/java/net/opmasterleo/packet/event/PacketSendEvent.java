package net.opmasterleo.packet.event;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.ApiStatus;

/**
 * Fired immediately before PacketAPI sends an NMS packet via {@code ServerPlayer.connection.send}.
 */
public final class PacketSendEvent extends PacketEvent {

    @ApiStatus.Internal
    public PacketSendEvent(Player player, Object packet, Object playerHandle) {
        super(player, packet, playerHandle);
    }

    /**
     * Dispatches only when listeners are registered — skips plugin-manager work on the hot path.
     *
     * @return the event after call, or {@code null} when nobody is listening
     */
    public static PacketSendEvent callIfListened(Player player, Object packet, Object playerHandle) {
        if (PacketEvent.getHandlerList().getRegisteredListeners().length == 0) {
            return null;
        }
        PacketSendEvent event = new PacketSendEvent(player, packet, playerHandle);
        Bukkit.getPluginManager().callEvent(event);
        return event;
    }
}
