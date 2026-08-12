package net.opmasterleo.packet.nms.shared;

import org.bukkit.craftbukkit.NMS.entity.CraftPlayer;
import org.bukkit.entity.Player;

import net.minecraft.server.NMS.EntityPlayer;
import net.minecraft.server.NMS.Packet;
import net.opmasterleo.packet.nms.PlayerBridge;

public final class SharedPlayerBridge implements PlayerBridge {

    @Override
    public Object handle(Player player) {
        if (player instanceof CraftPlayer craft) {
            return craft.getHandle();
        }
        return null;
    }

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void send(Player player, Object nmsPacket) {
        if (!(nmsPacket instanceof Packet packet)) {
            return;
        }
        EntityPlayer sp = nms(player);
        if (sp != null && sp.playerConnection != null) {
            sp.playerConnection.sendPacket(packet);
        }
    }

    @Override
    public boolean isConnected(Player player) {
        EntityPlayer sp = nms(player);
        return sp != null && sp.playerConnection != null && player.isOnline();
    }

    @Override
    public int entityId(Player player) {
        EntityPlayer sp = nms(player);
        return sp == null ? -1 : sp.getId();
    }

    @Override
    public Object level(Player player) {
        EntityPlayer sp = nms(player);
        return sp == null ? null : sp.world;
    }

    private static EntityPlayer nms(Player player) {
        return player instanceof CraftPlayer craft ? craft.getHandle() : null;
    }
}
