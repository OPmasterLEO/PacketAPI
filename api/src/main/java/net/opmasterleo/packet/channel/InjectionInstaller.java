package net.opmasterleo.packet.channel;

import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;

/**
 * Bukkit-free install core so ownership behaviour can be unit-tested with EmbeddedChannel.
 */
public final class InjectionInstaller {

    private static final String[] DEFAULT_OUTBOUND_BEFORE = {
            "packet_handler",
            "encoder",
            "outbound_config"
    };

    private static final String[] DEFAULT_INBOUND_BEFORE = {
            "packet_handler",
            "inbound_config"
    };

    private InjectionInstaller() {
    }

    public static InjectionInstallResult installNow(
            Channel channel,
            InjectionIdentity identity,
            UUID playerId,
            ChannelHandler handler,
            String[] extraAnchors,
            Consumer<String> warn
    ) {
        Objects.requireNonNull(channel, "channel");
        Objects.requireNonNull(identity, "identity");
        Objects.requireNonNull(playerId, "playerId");
        Objects.requireNonNull(handler, "handler");
        String name = identity.handlerName(playerId);
        InjectionProbe.Result probe = InjectionProbe.probe(channel, identity, playerId);
        if (probe.hasForeign()) {
            StringBuilder detail = new StringBuilder();
            for (InjectionProbe.ForeignHandler foreign : probe.foreign()) {
                if (detail.length() > 0) {
                    detail.append(" | ");
                }
                detail.append(InjectionIdentity.describeForeign(foreign.pipelineName(), foreign.handler(), foreign.marker()));
            }
            String message = "Another PacketAPI instance is already injected for player " + playerId
                    + " (owner=" + identity.ownerLabel() + ", token=" + identity.instanceToken()
                    + "). Refusing to overwrite. Foreign: " + detail;
            if (warn != null) {
                warn.accept(message);
            }
            return InjectionInstallResult.CONFLICT_FOREIGN;
        }
        ChannelHandler existingOwn = ChannelOps.get(channel, name);
        if (existingOwn != null) {
            ChannelOps.remove(channel, name);
            addBeforeAnchors(channel, name, handler, true, extraAnchors);
            channel.attr(InjectionMarkers.MARKER_ATTR).set(identity.encodeMarker(name, handler.getClass()));
            return InjectionInstallResult.REPLACED_OWN;
        }
        if (probe.ownPresent()) {
            channel.attr(InjectionMarkers.MARKER_ATTR).set(identity.encodeMarker(name, handler.getClass()));
            return InjectionInstallResult.ALREADY_OWN;
        }
        addBeforeAnchors(channel, name, handler, true, extraAnchors);
        channel.attr(InjectionMarkers.MARKER_ATTR).set(identity.encodeMarker(name, handler.getClass()));
        return InjectionInstallResult.INSTALLED;
    }

    static void addBeforeAnchors(
            Channel channel,
            String name,
            ChannelHandler handler,
            boolean outbound,
            String[] extraAnchors
    ) {
        String[] anchors = outbound ? DEFAULT_OUTBOUND_BEFORE : DEFAULT_INBOUND_BEFORE;
        for (String anchor : anchors) {
            if (ChannelOps.get(channel, anchor) != null) {
                channel.pipeline().addBefore(anchor, name, handler);
                return;
            }
        }
        if (extraAnchors != null) {
            for (String anchor : extraAnchors) {
                if (ChannelOps.get(channel, anchor) != null) {
                    channel.pipeline().addBefore(anchor, name, handler);
                    return;
                }
            }
        }
        if (ChannelOps.get(channel, "decoder") != null) {
            channel.pipeline().addAfter("decoder", name, handler);
            return;
        }
        channel.pipeline().addFirst(name, handler);
    }
}
