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
        ServerPlayer sp = nms(player);
        if (sp != null && sp.connection != null) {
            sp.connection.send(packet);
        }
    }

    @Override
    public boolean isConnected(Player player) {
        ServerPlayer sp = nms(player);
        return sp != null && sp.connection != null && sp.connection.isAcceptingMessages();
    }

    @Override
    public int entityId(Player player) {
        ServerPlayer sp = nms(player);
        return sp == null ? -1 : sp.getId();
    }

    @Override
    public Object level(Player player) {
        ServerPlayer sp = nms(player);
        return sp == null ? null : sp.level();
    }

    private static ServerPlayer nms(Player player) {
        return player instanceof CraftPlayer craft ? craft.getHandle() : null;
    }
}
