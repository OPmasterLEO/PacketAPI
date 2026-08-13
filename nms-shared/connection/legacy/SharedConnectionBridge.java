package net.opmasterleo.packet.nms.shared;

import org.bukkit.craftbukkit.NMS.entity.CraftPlayer;
import org.bukkit.entity.Player;

import io.netty.channel.Channel;
import net.minecraft.server.NMS.EntityPlayer;
import net.opmasterleo.packet.nms.ConnectionBridge;

import static net.opmasterleo.packet.nms.shared.Reflect.field;
import static net.opmasterleo.packet.nms.shared.Reflect.get;

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
        Object manager = connection(player);
        if (manager == null) {
            return null;
        }
        Object value = get(field(manager.getClass(), "channel"), manager);
        return value instanceof Channel channel ? channel : null;
    }

    @Override
    public Object connection(Player player) {
        EntityPlayer sp = nms(player);
        return sp == null || sp.playerConnection == null ? null : sp.playerConnection.networkManager;
    }

    @Override
    public Object listener(Player player) {
        EntityPlayer sp = nms(player);
        return sp == null ? null : sp.playerConnection;
    }

    @Override
    public int latency(Player player) {
        EntityPlayer sp = nms(player);
        return sp == null ? 0 : sp.ping;
    }

    @Override
    public boolean accepting(Player player) {
        EntityPlayer sp = nms(player);
        return sp != null && sp.playerConnection != null && player.isOnline();
    }

    @Override
    public String[] injectBeforeNames() {
        return ANCHORS.clone();
    }

    private static EntityPlayer nms(Player player) {
        return player instanceof CraftPlayer craft ? craft.getHandle() : null;
    }
}
