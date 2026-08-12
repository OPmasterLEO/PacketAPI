package net.opmasterleo.packet.nms.packet;

import java.util.Objects;

/**
 * Zero-copy handle around a raw NMS packet. Stable packets are not given per-version wrapper
 * classes — use this plus {@link net.opmasterleo.packet.nms.PacketBridge#classify}.
 */
public final class PacketWrap {

    public enum Direction {
        CLIENTBOUND,
        SERVERBOUND,
        UNKNOWN
    }

    private final Object handle;
    private final String type;
    private final Direction direction;

    public PacketWrap(Object handle, String type, Direction direction) {
        this.handle = Objects.requireNonNull(handle, "handle");
        this.type = type == null ? handle.getClass().getSimpleName() : type;
        this.direction = direction == null ? Direction.UNKNOWN : direction;
    }

    public static PacketWrap of(Object handle) {
        Objects.requireNonNull(handle, "handle");
        String simple = handle.getClass().getSimpleName();
        Direction direction;
        if (simple.startsWith("Clientbound") || simple.startsWith("PacketPlayOut")) {
            direction = Direction.CLIENTBOUND;
        } else if (simple.startsWith("Serverbound") || simple.startsWith("PacketPlayIn")) {
            direction = Direction.SERVERBOUND;
        } else {
            direction = Direction.UNKNOWN;
        }
        return new PacketWrap(handle, simple, direction);
    }

    public Object handle() {
        return handle;
    }

    public String type() {
        return type;
    }

    public Direction direction() {
        return direction;
    }

    public boolean clientbound() {
        return direction == Direction.CLIENTBOUND;
    }

    public PacketWrap withHandle(Object replacement) {
        return new PacketWrap(replacement, type, direction);
    }
}
