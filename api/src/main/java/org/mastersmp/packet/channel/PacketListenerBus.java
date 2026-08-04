package org.mastersmp.packet.channel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.bukkit.entity.Player;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import org.mastersmp.packet.nms.NmsAdapter;
import org.mastersmp.packet.packet.PacketTypeCache;

public final class PacketListenerBus {

    private final PacketChannels channels;
    private final NmsAdapter adapter;
    private final PacketTypeCache typeCache = new PacketTypeCache();
    private final List<PacketFilter> globalOutbound = new CopyOnWriteArrayList<>();
    private final List<InboundListener> globalInbound = new CopyOnWriteArrayList<>();
    private final Map<UUID, List<PacketFilter>> playerOutbound = new ConcurrentHashMap<>();
    private final Map<UUID, List<InboundListener>> playerInbound = new ConcurrentHashMap<>();
    private final Map<UUID, String> injected = new ConcurrentHashMap<>();

    public PacketListenerBus(PacketChannels channels, NmsAdapter adapter) {
        this.channels = channels;
        this.adapter = adapter;
    }

    public void addOutbound(PacketFilter filter) {
        globalOutbound.add(Objects.requireNonNull(filter, "filter"));
    }

    public void addOutbound(Player player, PacketFilter filter) {
        playerOutbound
                .computeIfAbsent(player.getUniqueId(), id -> new CopyOnWriteArrayList<>())
                .add(Objects.requireNonNull(filter, "filter"));
        ensureInjected(player);
    }

    public void addInbound(InboundListener listener) {
        globalInbound.add(Objects.requireNonNull(listener, "listener"));
    }

    public void addInbound(Player player, InboundListener listener) {
        playerInbound
                .computeIfAbsent(player.getUniqueId(), id -> new CopyOnWriteArrayList<>())
                .add(Objects.requireNonNull(listener, "listener"));
        ensureInjected(player);
    }

    public void removeOutbound(PacketFilter filter) {
        globalOutbound.remove(filter);
        for (List<PacketFilter> list : playerOutbound.values()) {
            list.remove(filter);
        }
    }

    public void removeInbound(InboundListener listener) {
        globalInbound.remove(listener);
        for (List<InboundListener> list : playerInbound.values()) {
            list.remove(listener);
        }
    }

    public void clear(Player player) {
        if (player == null) {
            return;
        }
        UUID id = player.getUniqueId();
        playerOutbound.remove(id);
        playerInbound.remove(id);
        String name = injected.remove(id);
        if (name != null) {
            channels.uninject(player, name);
        }
    }

    public void clear() {
        globalOutbound.clear();
        globalInbound.clear();
        playerOutbound.clear();
        playerInbound.clear();
        injected.clear();
        typeCache.clear();
    }

    public void ensureInjected(Player player) {
        if (player == null) {
            return;
        }
        String name = "packetapi_filter_" + player.getUniqueId();
        channels.injectOutbound(player, name, new FilterHandler(player));
        injected.put(player.getUniqueId(), name);
    }

    private OutboundPacketResult applyOutbound(Player player, PacketView view) {
        OutboundPacketResult result = OutboundPacketResult.keep();
        PacketView current = view;
        for (PacketFilter filter : globalOutbound) {
            result = filter.filter(player, current);
            if (result.kind() == OutboundPacketResult.Kind.DROP) {
                return result;
            }
            if (result.kind() == OutboundPacketResult.Kind.REPLACE) {
                current = current.withHandle(result.replacement());
                result = OutboundPacketResult.keep();
            }
        }
        List<PacketFilter> personal = playerOutbound.get(player.getUniqueId());
        if (personal != null) {
            for (PacketFilter filter : personal) {
                result = filter.filter(player, current);
                if (result.kind() == OutboundPacketResult.Kind.DROP) {
                    return result;
                }
                if (result.kind() == OutboundPacketResult.Kind.REPLACE) {
                    current = current.withHandle(result.replacement());
                    result = OutboundPacketResult.keep();
                }
            }
        }
        if (current.handle() != view.handle()) {
            return OutboundPacketResult.replace(current.handle());
        }
        return OutboundPacketResult.keep();
    }

    private boolean applyInbound(Player player, PacketView view) {
        for (InboundListener listener : globalInbound) {
            if (!listener.onInbound(player, view)) {
                return false;
            }
        }
        List<InboundListener> personal = playerInbound.get(player.getUniqueId());
        if (personal != null) {
            for (InboundListener listener : personal) {
                if (!listener.onInbound(player, view)) {
                    return false;
                }
            }
        }
        return true;
    }

    private String classify(Object packet) {
        return typeCache.getOrCompute(packet.getClass(), type -> adapter.packets().classify(packet));
    }

    @FunctionalInterface
    public interface InboundListener {
        boolean onInbound(Player player, PacketView packet);
    }

    private final class FilterHandler extends ChannelDuplexHandler {

        private final Player player;

        private FilterHandler(Player player) {
            this.player = player;
        }

        @Override
        public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
            if (msg == null) {
                super.write(ctx, msg, promise);
                return;
            }
            String name = classify(msg);
            if ("Bundle".equals(name) || name.endsWith("BundlePacket")) {
                List<Object> parts = adapter.packets().unwrapBundle(msg);
                if (parts != null && !parts.isEmpty()) {
                    List<Object> rebuilt = new ArrayList<>(parts.size());
                    for (Object part : parts) {
                        PacketView view = new PacketView(part, classify(part), PacketDirection.CLIENTBOUND);
                        OutboundPacketResult result = applyOutbound(player, view);
                        switch (result.kind()) {
                            case DROP -> {
                            }
                            case REPLACE -> rebuilt.add(result.replacement());
                            case KEEP -> rebuilt.add(part);
                        }
                    }
                    if (rebuilt.isEmpty()) {
                        promise.setSuccess();
                        return;
                    }
                    msg = adapter.packets().rebuildBundle(rebuilt);
                }
            } else {
                PacketView view = new PacketView(msg, name, PacketDirection.CLIENTBOUND);
                OutboundPacketResult result = applyOutbound(player, view);
                switch (result.kind()) {
                    case DROP -> {
                        promise.setSuccess();
                        return;
                    }
                    case REPLACE -> msg = result.replacement();
                    case KEEP -> {
                    }
                }
            }
            super.write(ctx, msg, promise);
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            if (msg != null) {
                PacketView view = new PacketView(msg, classify(msg), PacketDirection.SERVERBOUND);
                if (!applyInbound(player, view)) {
                    return;
                }
            }
            super.channelRead(ctx, msg);
        }
    }
}
