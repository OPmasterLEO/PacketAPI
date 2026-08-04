package org.mastersmp.packet.channel;

import org.bukkit.entity.Player;

@FunctionalInterface
public interface PacketFilter {

    OutboundPacketResult filter(Player viewer, PacketView packet);
}
