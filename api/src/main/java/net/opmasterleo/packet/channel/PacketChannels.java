package net.opmasterleo.packet.channel;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import org.bukkit.entity.Player;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import net.opmasterleo.packet.nms.ConnectionBridge;
import net.opmasterleo.packet.nms.NmsAdapter;
import net.opmasterleo.packet.schedule.Schedulers;

/**
 * Low-level Netty channel helpers. Pipeline installation for PacketAPI filters must go through
 * {@link PacketListenerBus#ensureInjected(Player)} — this class does not expose a public inject API.
 */
public final class PacketChannels {

    private final ConnectionBridge connection;
    private final Schedulers schedulers;
    private final InjectionIdentity identity;
    private final Logger logger;
    private final Map<UUID, Channel> knownChannels = new ConcurrentHashMap<>();
    private final Map<UUID, String> installedNames = new ConcurrentHashMap<>();

    public PacketChannels(NmsAdapter adapter, Schedulers schedulers, InjectionIdentity identity, Logger logger) {
        this.connection = adapter.connection();
        this.schedulers = Objects.requireNonNull(schedulers, "schedulers");
        this.identity = Objects.requireNonNull(identity, "identity");
        this.logger = logger;
    }

    public InjectionIdentity identity() {
        return identity;
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

    public InjectionProbe.Result probe(Player player) {
        if (player == null) {
            return new InjectionProbe.Result(InjectionProbe.State.NONE, null, false, java.util.List.of());
        }
        return InjectionProbe.probe(channel(player), identity, player.getUniqueId());
    }

    /**
     * Sole pipeline install path used by {@link PacketListenerBus}. Never silently evicts a
     * foreign PacketAPI handler.
     */
    InjectionInstallResult installOwned(Player player, ChannelHandler handler) {
        Objects.requireNonNull(handler, "handler");
        if (player == null) {
            return InjectionInstallResult.NO_CHANNEL;
        }
        UUID playerId = player.getUniqueId();
        String name = identity.handlerName(playerId);
        InjectionInstallResult[] outcome = {InjectionInstallResult.NO_CHANNEL};
        Runnable install = () -> {
            Channel channel = channel(player);
            if (channel == null) {
                outcome[0] = InjectionInstallResult.NO_CHANNEL;
                return;
            }
            ChannelOps.runInEventLoop(channel, () -> outcome[0] = InjectionInstaller.installNow(
                    channel,
                    identity,
                    playerId,
                    handler,
                    connection.injectBeforeNames(),
                    message -> {
                        if (logger != null) {
                            logger.warning(message);
                        }
                    }));
        };
        if (schedulers.isOwnedByCurrentRegion(player)) {
            install.run();
        } else {
            schedulers.advanced().runEntity(player, install);
        }
        if (outcome[0] == InjectionInstallResult.INSTALLED
                || outcome[0] == InjectionInstallResult.REPLACED_OWN
                || outcome[0] == InjectionInstallResult.ALREADY_OWN) {
            installedNames.put(playerId, name);
        }
        return outcome[0];
    }

    void uninjectOwned(Player player) {
        if (player == null) {
            return;
        }
        UUID id = player.getUniqueId();
        String name = installedNames.remove(id);
        if (name == null) {
            name = identity.handlerName(id);
        }
        Channel channel = channel(player);
        if (channel == null) {
            return;
        }
        String toRemove = name;
        ChannelOps.runInEventLoop(channel, () -> {
            ChannelHandler handler = ChannelOps.get(channel, toRemove);
            if (handler != null && identity.isOwnHandlerName(toRemove)) {
                ChannelOps.remove(channel, toRemove);
            }
            clearMarkerIfOurs(channel);
        });
    }

    /**
     * Removes only handlers installed by this identity. Never strips another PacketAPI copy's
     * pipeline entries (the old {@code startsWith("packetapi_")} behaviour).
     */
    public void uninjectAll() {
        for (Map.Entry<UUID, Channel> entry : knownChannels.entrySet()) {
            Channel channel = entry.getValue();
            if (channel == null) {
                continue;
            }
            UUID playerId = entry.getKey();
            String ownName = installedNames.getOrDefault(playerId, identity.handlerName(playerId));
            ChannelOps.runInEventLoop(channel, () -> {
                for (String name : ChannelOps.pipelineNames(channel)) {
                    if (identity.isOwnHandlerName(name)) {
                        ChannelOps.remove(channel, name);
                    }
                }
                if (ownName != null && identity.isOwnHandlerName(ownName)) {
                    ChannelOps.remove(channel, ownName);
                }
                clearMarkerIfOurs(channel);
            });
        }
        installedNames.clear();
        knownChannels.clear();
    }

    public void runOnEventLoop(Channel channel, Runnable task) {
        ChannelOps.runInEventLoop(channel, task);
    }

    private void clearMarkerIfOurs(Channel channel) {
        try {
            String marker = channel.attr(InjectionMarkers.MARKER_ATTR).get();
            if (marker != null && marker.contains("token=" + identity.instanceToken())) {
                channel.attr(InjectionMarkers.MARKER_ATTR).set(null);
            }
        } catch (Throwable ignored) {
        }
    }
}
