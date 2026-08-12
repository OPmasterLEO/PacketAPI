package org.mastersmp.packet.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.ApiStatus;
import org.mastersmp.packet.nms.packet.Hand;

/**
 * NMS-oriented wrap of CraftBukkit/Paper swing animation. Avoids Netty intercept of
 * {@code ServerboundSwingPacket}.
 */
public final class NmsSwingEvent extends Event implements Cancellable {

    private static final HandlerList HANDLER_LIST = new HandlerList();

    private final Player player;
    private final Object playerHandle;
    private final Hand hand;
    private boolean cancelled;

    @ApiStatus.Internal
    public NmsSwingEvent(Player player, Object playerHandle, Hand hand) {
        super(false);
        this.player = player;
        this.playerHandle = playerHandle;
        this.hand = hand;
    }

    public Player getPlayer() {
        return player;
    }

    public Object getPlayerHandle() {
        return playerHandle;
    }

    public Hand getHand() {
        return hand;
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
