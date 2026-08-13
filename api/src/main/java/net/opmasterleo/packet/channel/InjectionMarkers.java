package net.opmasterleo.packet.channel;

import io.netty.util.AttributeKey;

/**
 * Cross-instance constants for PacketAPI Netty injection.
 * <p>
 * These string literals must remain stable under shading/relocation (relocators rewrite
 * class references, not arbitrary string constants). Independently shaded copies therefore
 * still recognize each other's pipeline markers via these prefixes / attribute names.
 */
public final class InjectionMarkers {

    /**
     * Pipeline handler name prefix shared by every PacketAPI copy.
     * Full names look like {@code packetapi_<instanceToken>_filter_<playerUuid>}.
     */
    public static final String NAME_PREFIX = "packetapi_";

    /**
     * Segment that separates the instance token from the player id in handler names.
     */
    public static final String FILTER_SEGMENT = "_filter_";

    /**
     * Netty {@link AttributeKey} name. {@link AttributeKey#valueOf(String)} interns by name
     * globally, so independently loaded copies share the same key identity.
     */
    public static final String ATTR_KEY_NAME = "packetapi.injection.marker.v1";

    public static final AttributeKey<String> MARKER_ATTR = AttributeKey.valueOf(ATTR_KEY_NAME);

    private InjectionMarkers() {
    }

    public static boolean looksLikePacketApiHandler(String pipelineName) {
        if (pipelineName == null || !pipelineName.startsWith(NAME_PREFIX)) {
            return false;
        }
        // Current: packetapi_<token>_filter_<uuid>
        // Legacy (pre-hardening): packetapi_filter_<uuid> — still must be recognized as foreign.
        return pipelineName.contains(FILTER_SEGMENT);
    }

    public static String playerIdSuffix(String pipelineName, java.util.UUID playerId) {
        return FILTER_SEGMENT + playerId;
    }
}
