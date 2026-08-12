package org.mastersmp.packet.event;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.ApiStatus;

/**
 * Fired when a player quits, with the NMS {@code ServerPlayer} handle when still available.
 */
public final class NmsPlayerQuitEvent extends NmsPlayerEvent {

    @ApiStatus.Internal
    public NmsPlayerQuitEvent(Player player, Object playerHandle) {
        super(player, playerHandle);
    }
}
