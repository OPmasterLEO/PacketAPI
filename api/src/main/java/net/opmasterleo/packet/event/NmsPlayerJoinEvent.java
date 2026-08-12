package net.opmasterleo.packet.event;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.ApiStatus;

/**
 * Fired after a player joins, with the NMS {@code ServerPlayer} handle already resolved.
 */
public final class NmsPlayerJoinEvent extends NmsPlayerEvent {

    @ApiStatus.Internal
    public NmsPlayerJoinEvent(Player player, Object playerHandle) {
        super(player, playerHandle);
    }
}
