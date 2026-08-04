package org.mastersmp.packet.nms.shared;

import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;

import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerPlayer;
import org.mastersmp.packet.nms.PlayerBridge;

public final class SharedPlayerBridge implements PlayerBridge {

    @Override
    public Object handle(Player player) {
        if (player instanceof CraftPlayer craft) {
            return craft.getHandle();
        }
        return null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void send(Player player, Object nmsPacket) {
        if (!(nmsPacket instanceof Packet<?> packet)) {
            return;
        }
        Object handle = handle(player);
        if (handle instanceof ServerPlayer sp && sp.connection != null) {
            sp.connection.send(packet);
        }
    }

    @Override
    public boolean isConnected(Player player) {
        Object handle = handle(player);
        if (!(handle instanceof ServerPlayer sp) || sp.connection == null) {
            return false;
        }
        return sp.connection.isAcceptingMessages();
    }
}
