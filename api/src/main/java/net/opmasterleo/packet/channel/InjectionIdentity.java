package net.opmasterleo.packet.channel;

import java.util.Objects;
import java.util.UUID;

/**
 * Per class-loader / per-load identity for one PacketAPI injection owner.
 * <p>
 * The token is random at construction so two independently shaded copies never compute the
 * same handler name by coincidence, while a single instance re-injecting for the same player
 * still recognizes (and may replace) its own prior handler.
 */
public final class InjectionIdentity {

    private final String instanceToken;
    private final String ownerLabel;

    public InjectionIdentity(String ownerLabel) {
        this(ownerLabel, UUID.randomUUID().toString().replace("-", ""));
    }

    /**
     * Visible for tests that need two distinct identities in one JVM.
     */
    public InjectionIdentity(String ownerLabel, String instanceToken) {
        this.ownerLabel = ownerLabel == null || ownerLabel.isBlank() ? "unknown" : ownerLabel;
        this.instanceToken = Objects.requireNonNull(instanceToken, "instanceToken");
        if (instanceToken.isBlank()) {
            throw new IllegalArgumentException("instanceToken must not be blank");
        }
    }

    public String instanceToken() {
        return instanceToken;
    }

    public String ownerLabel() {
        return ownerLabel;
    }

    public String handlerName(UUID playerId) {
        Objects.requireNonNull(playerId, "playerId");
        return InjectionMarkers.NAME_PREFIX + instanceToken + InjectionMarkers.FILTER_SEGMENT + playerId;
    }

    public boolean isOwnHandlerName(String pipelineName) {
        return pipelineName != null
                && pipelineName.startsWith(InjectionMarkers.NAME_PREFIX + instanceToken + InjectionMarkers.FILTER_SEGMENT);
    }

    public String encodeMarker(String handlerName, Class<?> handlerClass) {
        String className = handlerClass == null ? "?" : handlerClass.getName();
        String loader = "?";
        if (handlerClass != null && handlerClass.getClassLoader() != null) {
            loader = handlerClass.getClassLoader().getName() != null
                    ? handlerClass.getClassLoader().getName()
                    : handlerClass.getClassLoader().getClass().getName()
                    + "@" + Integer.toHexString(System.identityHashCode(handlerClass.getClassLoader()));
        }
        return "token=" + instanceToken
                + ";owner=" + sanitize(ownerLabel)
                + ";handler=" + sanitize(handlerName)
                + ";class=" + sanitize(className)
                + ";loader=" + sanitize(loader);
    }

    public static String describeForeign(String pipelineName, io.netty.channel.ChannelHandler handler, String marker) {
        StringBuilder sb = new StringBuilder();
        sb.append("name=").append(pipelineName);
        if (handler != null) {
            sb.append(", class=").append(handler.getClass().getName());
            ClassLoader cl = handler.getClass().getClassLoader();
            if (cl != null) {
                sb.append(", classloader=").append(cl.getName() != null ? cl.getName() : cl);
            }
        }
        if (marker != null && !marker.isBlank()) {
            sb.append(", marker=").append(marker);
        }
        return sb.toString();
    }

    private static String sanitize(String value) {
        return value.replace(';', '_').replace('\n', '_');
    }
}
