package net.opmasterleo.packet.channel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Logger;

import org.bukkit.entity.Player;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import net.opmasterleo.packet.nms.NmsAdapter;
import net.opmasterleo.packet.packet.PacketTypeCache;
import net.opmasterleo.packet.schedule.Schedulers;

/**
 * Packet filter bus with a single idempotent injection entry point ({@link #ensureInjected(Player)}).
 * <p>
 * Handler names are instance-scoped so independently shaded PacketAPI copies cannot silently
 * evict each other. Foreign injections are detected, logged, and refused.
 */
public final class PacketListenerBus {

    private final PacketChannels channels;
    private final NmsAdapter adapter;
    private final Logger logger;
    private final PacketTypeCache typeCache = new PacketTypeCache();
    private final List<PacketFilter> globalOutbound = new CopyOnWriteArrayList<>();
    private final List<InboundListener> globalInbound = new CopyOnWriteArrayList<>();
    private final Map<UUID, List<PacketFilter>> playerOutbound = new ConcurrentHashMap<>();
    private final Map<UUID, List<InboundListener>> playerInbound = new ConcurrentHashMap<>();
    private final Map<UUID, String> injected = new ConcurrentHashMap<>();
    private final Map<UUID, InjectionProbe.State> lastKnownState = new ConcurrentHashMap<>();

    public PacketListenerBus(PacketChannels channels, NmsAdapter adapter, Logger logger) {
        this.channels = Objects.requireNonNull(channels, "channels");
        this.adapter = Objects.requireNonNull(adapter, "adapter");
        this.logger = logger;
    }

    public static PacketListenerBus create(NmsAdapter adapter, Schedulers schedulers, String ownerLabel, Logger logger) {
        InjectionIdentity identity = new InjectionIdentity(ownerLabel);
        PacketChannels channels = new PacketChannels(adapter, schedulers, identity, logger);
        return new PacketListenerBus(channels, adapter, logger);
    }

    public PacketChannels channels() {
        return channels;
    }

    public InjectionIdentity identity() {
        return channels.identity();
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
        injected.remove(id);
        lastKnownState.remove(id);
        channels.uninjectOwned(player);
    }

    public void clear() {
        globalOutbound.clear();
        globalInbound.clear();
        playerOutbound.clear();
        playerInbound.clear();
        injected.clear();
        lastKnownState.clear();
        typeCache.clear();
        channels.uninjectAll();
    }

    /**
     * Canonical, idempotent injection entry point. All internal callers must go through this
     * method — there is no parallel public inject path on {@link PacketChannels}.
     *
     * @return install outcome (including {@link InjectionInstallResult#CONFLICT_FOREIGN}
     * when another PacketAPI copy already owns the pipeline)
     */
    public InjectionInstallResult ensureInjected(Player player) {
        if (player == null) {
            return InjectionInstallResult.NO_CHANNEL;
        }
        InjectionProbe.Result before = detectConflicts(player);
        if (before.state() == InjectionProbe.State.FOREIGN
                || before.state() == InjectionProbe.State.OWN_AND_FOREIGN) {
            if (!tryAttachToForeignDispatch(player, before)) {
                logForeignConflict(player, before);
                lastKnownState.put(player.getUniqueId(), before.state());
                return InjectionInstallResult.CONFLICT_FOREIGN;
            }
        }
        InjectionInstallResult result = channels.installOwned(player, new FilterHandler(player));
        if (result == InjectionInstallResult.INSTALLED
                || result == InjectionInstallResult.REPLACED_OWN
                || result == InjectionInstallResult.ALREADY_OWN) {
            injected.put(player.getUniqueId(), channels.identity().handlerName(player.getUniqueId()));
            lastKnownState.put(player.getUniqueId(), InjectionProbe.State.OWN);
        } else if (result == InjectionInstallResult.CONFLICT_FOREIGN) {
            lastKnownState.put(player.getUniqueId(), InjectionProbe.State.FOREIGN);
        }
        return result;
    }

    /**
     * Status/debug probe: is this player injected by this instance, a foreign instance, or neither?
     */
    public InjectionProbe.Result detectConflicts(Player player) {
        if (player == null) {
            return new InjectionProbe.Result(InjectionProbe.State.NONE, null, false, List.of());
        }
        InjectionProbe.Result result = channels.probe(player);
        lastKnownState.put(player.getUniqueId(), result.state());
        return result;
    }

    /**
     * Human-readable diagnostics for status/debug commands.
     */
    public String describeInjection(Player player) {
        InjectionProbe.Result result = detectConflicts(player);
        StringBuilder sb = new StringBuilder();
        sb.append("player=").append(player == null ? "null" : player.getUniqueId());
        sb.append(" state=").append(result.state());
        sb.append(" ownHandler=").append(result.ownHandlerName());
        sb.append(" identity=").append(channels.identity().ownerLabel());
        sb.append('/').append(channels.identity().instanceToken());
        if (result.hasForeign()) {
            sb.append(" foreign=[");
            boolean first = true;
            for (InjectionProbe.ForeignHandler foreign : result.foreign()) {
                if (!first) {
                    sb.append("; ");
                }
                first = false;
                sb.append(InjectionIdentity.describeForeign(foreign.pipelineName(), foreign.handler(), foreign.marker()));
            }
            sb.append(']');
        }
        return sb.toString();
    }

    public Map<UUID, InjectionProbe.State> lastKnownStates() {
        return Map.copyOf(lastKnownState);
    }

    /**
     * Attempt to register this bus's listeners against an existing same-classloader handler.
     * Independently shaded copies cannot share dispatch across classloaders — returns false.
     */
    private boolean tryAttachToForeignDispatch(Player player, InjectionProbe.Result probe) {
        for (InjectionProbe.ForeignHandler foreign : probe.foreign()) {
            ChannelHandler handler = foreign.handler();
            if (handler == null) {
                continue;
            }
            if (handler.getClass().getClassLoader() != FilterHandler.class.getClassLoader()) {
                continue;
            }
            if (!(handler instanceof FilterHandler existing)) {
                continue;
            }
            // Same classloader + our handler type: the "foreign" handler is actually a prior
            // inject from this bus class. Adopt it instead of installing a second handler.
            injected.put(player.getUniqueId(), foreign.pipelineName());
            lastKnownState.put(player.getUniqueId(), InjectionProbe.State.OWN);
            if (logger != null) {
                logger.info("Reusing existing PacketAPI FilterHandler for " + player.getUniqueId()
                        + " (" + foreign.pipelineName() + ")");
            }
            // existing closes over its constructing bus; if it is this bus, listeners already apply.
            // If it is another PacketListenerBus in the same loader (rare), we cannot safely merge
            // without shared static state — still refuse silent overwrite of a different bus.
            if (existing.owner() == this) {
                return true;
            }
        }
        return false;
    }

    private void logForeignConflict(Player player, InjectionProbe.Result probe) {
        String message = "PacketAPI injection conflict for player " + player.getUniqueId()
                + ": another PacketAPI instance already owns the Netty pipeline. "
                + "This instance (" + channels.identity().ownerLabel()
                + " / " + channels.identity().instanceToken()
                + ") will NOT overwrite it. " + describeInjection(player);
        if (logger != null) {
            logger.warning(message);
        }
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

        private PacketListenerBus owner() {
            return PacketListenerBus.this;
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
