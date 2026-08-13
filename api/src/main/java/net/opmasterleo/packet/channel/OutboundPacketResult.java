package net.opmasterleo.packet.channel;

import java.util.Objects;

public final class OutboundPacketResult {

    private static final OutboundPacketResult KEEP = new OutboundPacketResult(Kind.KEEP, null);
    private static final OutboundPacketResult DROP = new OutboundPacketResult(Kind.DROP, null);

    private final Kind kind;
    private final Object replacement;

    private OutboundPacketResult(Kind kind, Object replacement) {
        this.kind = kind;
        this.replacement = replacement;
    }

    public static OutboundPacketResult keep() {
        return KEEP;
    }

    public static OutboundPacketResult drop() {
        return DROP;
    }

    public static OutboundPacketResult replace(Object packet) {
        return new OutboundPacketResult(Kind.REPLACE, Objects.requireNonNull(packet, "packet"));
    }

    public Kind kind() {
        return kind;
    }

    public Object replacement() {
        return replacement;
    }

    public enum Kind {
        KEEP,
        DROP,
        REPLACE
    }
}
