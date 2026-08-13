package net.opmasterleo.packet.channel;

import org.bukkit.entity.Player;

@FunctionalInterface
public interface PacketFilter {

    OutboundPacketResult filter(Player viewer, PacketView packet);
}
