package org.mastersmp.packet.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.ApiStatus;

/**
 * Parent for NMS player lifecycle events. Handle is {@code ServerPlayer} / {@code EntityPlayer}.
 */
public abstract class NmsPlayerEvent extends Event {

    private static final HandlerList HANDLER_LIST = new HandlerList();

    private final Player player;
    private final Object playerHandle;

    @ApiStatus.Internal
    protected NmsPlayerEvent(Player player, Object playerHandle) {
        super(false);
        this.player = player;
        this.playerHandle = playerHandle;
    }

    public Player getPlayer() {
        return player;
    }

    /**
     * NMS {@code ServerPlayer} / {@code EntityPlayer} from {@code CraftPlayer.getHandle()}.
     */
    public Object getPlayerHandle() {
        return playerHandle;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }
}
