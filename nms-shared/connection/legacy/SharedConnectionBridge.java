package org.mastersmp.packet.nms.shared;

import org.bukkit.craftbukkit.NMS.entity.CraftPlayer;
import org.bukkit.entity.Player;

import io.netty.channel.Channel;
import net.minecraft.server.NMS.EntityPlayer;
import net.minecraft.server.NMS.NetworkManager;
import net.minecraft.server.NMS.PlayerConnection;
import org.mastersmp.packet.nms.ConnectionBridge;

public final class SharedConnectionBridge implements ConnectionBridge {

    private static final String[] ANCHORS = {
            "packet_handler",
            "encoder",
            "decoder",
            "prepender",
            "compress"
    };

    @Override
    public Channel channel(Player player) {
        EntityPlayer sp = handle(player);
        if (sp == null || sp.playerConnection == null) {
            return null;
        }
        NetworkManager manager = sp.playerConnection.networkManager;
        Object channel = Reflect.get(Reflect.field(manager.getClass(), "channel"), manager);
        return channel instanceof Channel c ? c : null;
    }

    @Override
    public Object connection(Player player) {
        EntityPlayer sp = handle(player);
        return sp == null || sp.playerConnection == null ? null : sp.playerConnection.networkManager;
    }

    @Override
    public Object gamePacketListener(Player player) {
        EntityPlayer sp = handle(player);
        return sp == null ? null : sp.playerConnection;
    }

    @Override
    public String[] injectBeforeNames() {
        return ANCHORS.clone();
    }

    private static EntityPlayer handle(Player player) {
        if (player instanceof CraftPlayer craft) {
            return craft.getHandle();
        }
        return null;
    }
}
