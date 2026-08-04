package org.mastersmp.packet.channel;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.entity.Player;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import org.mastersmp.packet.nms.ConnectionBridge;
import org.mastersmp.packet.nms.NmsAdapter;
import org.mastersmp.packet.schedule.Schedulers;

public final class PacketChannels {

    private static final String[] DEFAULT_OUTBOUND_BEFORE = {
            "packet_handler",
            "encoder",
            "outbound_config"
    };

    private static final String[] DEFAULT_INBOUND_BEFORE = {
            "packet_handler",
            "inbound_config"
    };

    private final ConnectionBridge connection;
    private final Schedulers schedulers;
    private final Map<UUID, Channel> knownChannels = new ConcurrentHashMap<>();

    public PacketChannels(NmsAdapter adapter, Schedulers schedulers) {
        this.connection = adapter.connection();
        this.schedulers = Objects.requireNonNull(schedulers, "schedulers");
    }

    public Channel channel(Player player) {
        if (player == null) {
            return null;
        }
        Channel channel = connection.channel(player);
        if (channel != null) {
            knownChannels.put(player.getUniqueId(), channel);
        }
        return channel;
    }

    public boolean isActive(Player player) {
        Channel channel = channel(player);
        return channel != null && channel.isActive();
    }

    public void injectOutbound(Player player, String name, ChannelHandler handler) {
        inject(player, name, handler, true);
    }

    public void injectInbound(Player player, String name, ChannelHandler handler) {
        inject(player, name, handler, false);
    }

    public void uninject(Player player, String name) {
        if (player == null || name == null) {
            return;
        }
        Channel channel = channel(player);
        if (channel == null) {
            return;
        }
        ChannelOps.runInEventLoop(channel, () -> ChannelOps.remove(channel, name));
    }

    public void uninject(Player player, Class<? extends ChannelHandler> handlerClass) {
        if (player == null || handlerClass == null) {
            return;
        }
        Channel channel = channel(player);
        if (channel == null) {
            return;
        }
        ChannelOps.runInEventLoop(channel, () -> ChannelOps.remove(channel, handlerClass));
    }

    public void runOnEventLoop(Channel channel, Runnable task) {
        ChannelOps.runInEventLoop(channel, task);
    }

    public void uninjectAll() {
        for (Channel channel : knownChannels.values()) {
            if (channel == null) {
                continue;
            }
            ChannelOps.runInEventLoop(channel, () -> {
                for (String name : ChannelOps.pipelineNames(channel)) {
                    if (name.startsWith("packetapi_")) {
                        ChannelOps.remove(channel, name);
                    }
                }
            });
        }
        knownChannels.clear();
    }

    private void inject(Player player, String name, ChannelHandler handler, boolean outbound) {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(handler, "handler");
        if (player == null) {
            return;
        }
        Runnable install = () -> {
            Channel channel = channel(player);
            if (channel == null) {
                return;
            }
            ChannelOps.runInEventLoop(channel, () -> installNow(channel, name, handler, outbound));
        };
        if (schedulers.isOwnedByCurrentRegion(player)) {
            install.run();
        } else {
            schedulers.advanced().runEntity(player, install);
        }
    }

    private void installNow(Channel channel, String name, ChannelHandler handler, boolean outbound) {
        ChannelOps.remove(channel, name);
        String[] anchors = outbound ? DEFAULT_OUTBOUND_BEFORE : DEFAULT_INBOUND_BEFORE;
        for (String anchor : anchors) {
            if (ChannelOps.get(channel, anchor) != null) {
                channel.pipeline().addBefore(anchor, name, handler);
                return;
            }
        }
        for (String anchor : connection.injectBeforeNames()) {
            if (ChannelOps.get(channel, anchor) != null) {
                channel.pipeline().addBefore(anchor, name, handler);
                return;
            }
        }
        if (ChannelOps.get(channel, "decoder") != null) {
            channel.pipeline().addAfter("decoder", name, handler);
        }
    }
}
