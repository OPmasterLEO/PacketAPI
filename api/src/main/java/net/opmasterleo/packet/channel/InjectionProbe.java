package net.opmasterleo.packet.channel;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;

/**
 * Probes a Netty pipeline for PacketAPI injection ownership relative to one identity.
 */
public final class InjectionProbe {

    public enum State {
        /** No PacketAPI handler for this player. */
        NONE,
        /** Only this identity's handler is present. */
        OWN,
        /** A foreign PacketAPI instance is injected; this identity is not. */
        FOREIGN,
        /** Both this identity and a foreign PacketAPI handler are present. */
        OWN_AND_FOREIGN
    }

    public record ForeignHandler(String pipelineName, ChannelHandler handler, String marker) {
    }

    public record Result(
            State state,
            String ownHandlerName,
            boolean ownPresent,
            List<ForeignHandler> foreign
    ) {
        public boolean hasForeign() {
            return foreign != null && !foreign.isEmpty();
        }
    }

    private InjectionProbe() {
    }

    public static Result probe(Channel channel, InjectionIdentity identity, UUID playerId) {
        Objects.requireNonNull(identity, "identity");
        Objects.requireNonNull(playerId, "playerId");
        String ownName = identity.handlerName(playerId);
        if (channel == null) {
            return new Result(State.NONE, ownName, false, List.of());
        }

        String marker = null;
        try {
            marker = channel.attr(InjectionMarkers.MARKER_ATTR).get();
        } catch (Throwable ignored) {
        }

        boolean ownPresent = false;
        List<ForeignHandler> foreign = new ArrayList<>();
        String suffix = InjectionMarkers.playerIdSuffix(null, playerId);

        for (String name : ChannelOps.pipelineNames(channel)) {
            if (!InjectionMarkers.looksLikePacketApiHandler(name) || !name.endsWith(suffix)) {
                continue;
            }
            ChannelHandler handler = ChannelOps.get(channel, name);
            if (identity.isOwnHandlerName(name)) {
                ownPresent = true;
            } else {
                foreign.add(new ForeignHandler(name, handler, marker));
            }
        }

        // Marker may outlive a removed handler or identify a foreign owner when names differ.
        if (!ownPresent && marker != null && marker.contains("token=") && !marker.contains("token=" + identity.instanceToken())) {
            if (foreign.isEmpty()) {
                foreign.add(new ForeignHandler("(marker-only)", null, marker));
            }
        }

        State state;
        if (ownPresent && !foreign.isEmpty()) {
            state = State.OWN_AND_FOREIGN;
        } else if (ownPresent) {
            state = State.OWN;
        } else if (!foreign.isEmpty()) {
            state = State.FOREIGN;
        } else {
            state = State.NONE;
        }
        return new Result(state, ownName, ownPresent, List.copyOf(foreign));
    }
}
