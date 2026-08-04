package org.mastersmp.packet.nms;

import org.bukkit.entity.Player;

public interface PlayerBridge {

    Object handle(Player player);

    void send(Player player, Object nmsPacket);

    boolean isConnected(Player player);
}
