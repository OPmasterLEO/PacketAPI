package net.opmasterleo.packet.channel;

import java.util.Objects;

public final class PacketView {

    private final Object handle;
    private final String name;
    private final PacketDirection direction;

    public PacketView(Object handle, String name, PacketDirection direction) {
        this.handle = Objects.requireNonNull(handle, "handle");
        this.name = Objects.requireNonNull(name, "name");
        this.direction = Objects.requireNonNull(direction, "direction");
    }

    public Object handle() {
        return handle;
    }

    public String name() {
        return name;
    }

    public PacketDirection direction() {
        return direction;
    }

    public PacketView withHandle(Object replacement) {
        return new PacketView(replacement, name, direction);
    }
}
