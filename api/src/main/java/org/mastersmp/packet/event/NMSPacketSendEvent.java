package org.mastersmp.packet.event;

/**
 * Fired when an NMS packet is about to be sent to a player.
 */
public class NMSPacketSendEvent extends NMSPacketEvent {

    public NMSPacketSendEvent(Object nmsPacket, Object playerHandle) {
        super(nmsPacket, playerHandle);
    }

    /**
     * Gets the packet type by stripping common prefixes.
     * @return Clean packet type name
     */
    public String getPacketType() {
        String name = getPacketName();
        if (name.startsWith("Clientbound")) {
            return name.substring("Clientbound".length());
        }
        if (name.startsWith("Serverbound")) {
            return name.substring("Serverbound".length());
        }
        if (name.startsWith("PacketPlayOut")) {
            return name.substring("PacketPlayOut".length());
        }
        if (name.startsWith("PacketPlayIn")) {
            return name.substring("PacketPlayIn".length());
        }
        return name;
    }
}
