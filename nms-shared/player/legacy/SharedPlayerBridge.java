package org.mastersmp.packet.nms.shared;

import org.bukkit.craftbukkit.NMS.entity.CraftPlayer;
import org.bukkit.entity.Player;

import net.minecraft.server.NMS.EntityPlayer;
import net.minecraft.server.NMS.Packet;
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
    public void send(Player player, Object nmsPacket) {
        if (!(nmsPacket instanceof Packet<?> packet)) {
            return;
        }
        Object handle = handle(player);
        if (handle instanceof EntityPlayer sp && sp.playerConnection != null) {
            sp.playerConnection.sendPacket(packet);
        }
    }

    @Override
    public boolean isConnected(Player player) {
        return player != null && player.isOnline();
    }
}
