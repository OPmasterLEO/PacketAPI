package org.mastersmp.packet.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.ApiStatus;
import org.mastersmp.packet.nms.packet.Hand;
import org.mastersmp.packet.nms.packet.InteractAction;

/**
 * NMS-oriented wrap of Paper {@code PlayerUseUnknownEntityEvent} (fake-entity interact)
 * without injecting into the Netty pipeline.
 */
public final class NmsUnknownEntityInteractEvent extends Event implements Cancellable {

    private static final HandlerList HANDLER_LIST = new HandlerList();

    private final Player player;
    private final Object playerHandle;
    private final int entityId;
    private final InteractAction action;
    private final Hand hand;
    private boolean cancelled;

    @ApiStatus.Internal
    public NmsUnknownEntityInteractEvent(
            Player player,
            Object playerHandle,
            int entityId,
            InteractAction action,
            Hand hand
    ) {
        super(false);
        this.player = player;
        this.playerHandle = playerHandle;
        this.entityId = entityId;
        this.action = action;
        this.hand = hand;
    }

    public Player getPlayer() {
        return player;
    }

    public Object getPlayerHandle() {
        return playerHandle;
    }

    public int getEntityId() {
        return entityId;
    }

    public InteractAction getAction() {
        return action;
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
