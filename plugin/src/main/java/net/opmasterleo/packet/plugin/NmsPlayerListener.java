package net.opmasterleo.packet.plugin;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import net.opmasterleo.packet.PacketAPI;
import net.opmasterleo.packet.event.NmsPlayerJoinEvent;
import net.opmasterleo.packet.event.NmsPlayerQuitEvent;
import net.opmasterleo.packet.event.NmsSwingEvent;
import net.opmasterleo.packet.event.NmsUnknownEntityInteractEvent;
import net.opmasterleo.packet.nms.packet.Hand;
import net.opmasterleo.packet.nms.packet.InteractAction;

/**
 * Bridges CraftBukkit/Paper player events into NMS-handle custom events.
 * Does not inject Netty handlers — inbound interact with unknown entities uses Paper's event.
 */
public final class NmsPlayerListener implements Listener {

    private final PacketAPI api;

    public NmsPlayerListener(PacketAPI api) {
        this.api = api;
    }

    public void register(JavaPlugin plugin) {
        PluginManager manager = plugin.getServer().getPluginManager();
        manager.registerEvents(this, plugin);
        registerUnknownEntity(plugin, manager);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onJoin(PlayerJoinEvent event) {
        Object handle = api.adapter().players().handle(event.getPlayer());
        event.getPlayer().getServer().getPluginManager().callEvent(new NmsPlayerJoinEvent(event.getPlayer(), handle));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event) {
        Object handle = api.adapter().players().handle(event.getPlayer());
        event.getPlayer().getServer().getPluginManager().callEvent(new NmsPlayerQuitEvent(event.getPlayer(), handle));
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onSwing(PlayerAnimationEvent event) {
        Object handle = api.adapter().players().handle(event.getPlayer());
        Hand hand = Hand.MAIN_HAND;
        try {
            Object nmsHand = event.getClass().getMethod("getAnimationType").invoke(event);
            if (nmsHand != null && nmsHand.toString().contains("OFF")) {
                hand = Hand.OFF_HAND;
            }
        } catch (ReflectiveOperationException ignored) {
        }
        NmsSwingEvent wrapped = new NmsSwingEvent(event.getPlayer(), handle, hand);
        event.getPlayer().getServer().getPluginManager().callEvent(wrapped);
        if (wrapped.isCancelled()) {
            event.setCancelled(true);
        }
    }

    @SuppressWarnings("unchecked")
    private void registerUnknownEntity(JavaPlugin plugin, PluginManager manager) {
        Class<?> type;
        try {
            type = Class.forName("com.destroystokyo.paper.event.player.PlayerUseUnknownEntityEvent");
        } catch (ClassNotFoundException ignored) {
            return;
        }
        manager.registerEvent(
                (Class<? extends org.bukkit.event.Event>) type,
                this,
                EventPriority.HIGH,
                (listener, event) -> handleUnknownEntity(event),
                plugin,
                true
        );
    }

    private void handleUnknownEntity(org.bukkit.event.Event event) {
        try {
            Object player = event.getClass().getMethod("getPlayer").invoke(event);
            if (!(player instanceof org.bukkit.entity.Player bukkitPlayer)) {
                return;
            }
            int entityId = (Integer) event.getClass().getMethod("getEntityId").invoke(event);
            boolean attack = false;
            try {
                Object clicked = event.getClass().getMethod("isAttack").invoke(event);
                attack = Boolean.TRUE.equals(clicked);
            } catch (NoSuchMethodException ignored) {
                try {
                    Object clicked = event.getClass().getMethod("clickedLeft").invoke(event);
                    attack = Boolean.TRUE.equals(clicked);
                } catch (NoSuchMethodException ignoredToo) {
                }
            }
            Hand hand = Hand.MAIN_HAND;
            try {
                Object nmsHand = event.getClass().getMethod("getHand").invoke(event);
                if (nmsHand != null && nmsHand.toString().contains("OFF")) {
                    hand = Hand.OFF_HAND;
                }
            } catch (NoSuchMethodException ignored) {
            }
            Object handle = api.adapter().players().handle(bukkitPlayer);
            NmsUnknownEntityInteractEvent wrapped = new NmsUnknownEntityInteractEvent(
                    bukkitPlayer,
                    handle,
                    entityId,
                    attack ? InteractAction.ATTACK : InteractAction.INTERACT,
                    hand
            );
            bukkitPlayer.getServer().getPluginManager().callEvent(wrapped);
            if (wrapped.isCancelled() && event instanceof org.bukkit.event.Cancellable cancellable) {
                cancellable.setCancelled(true);
            }
        } catch (ReflectiveOperationException ignored) {
        }
    }
}
