package org.mastersmp.packet.nms.packet;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PacketWrapTest {

    @Test
    void classifiesClientboundName() {
        PacketWrap wrap = PacketWrap.of(new ClientboundSetHealthPacket());
        assertEquals("ClientboundSetHealthPacket", wrap.type());
        assertTrue(wrap.clientbound());
        assertEquals(PacketWrap.Direction.CLIENTBOUND, wrap.direction());
    }

    @Test
    void classifiesServerboundName() {
        PacketWrap wrap = PacketWrap.of(new ServerboundInteractPacket());
        assertEquals(PacketWrap.Direction.SERVERBOUND, wrap.direction());
    }

    @Test
    void withHandleKeepsType() {
        PacketWrap original = PacketWrap.of(new ClientboundSetHealthPacket());
        Object replacement = new ClientboundSetHealthPacket();
        PacketWrap next = original.withHandle(replacement);
        assertEquals(replacement, next.handle());
        assertEquals(original.type(), next.type());
    }

    static final class ClientboundSetHealthPacket {
    }

    static final class ServerboundInteractPacket {
    }
}
