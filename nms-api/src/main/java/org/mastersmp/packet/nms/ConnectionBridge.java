package org.mastersmp.packet.nms;

import org.bukkit.entity.Player;

import io.netty.channel.Channel;

public interface ConnectionBridge {

    Channel channel(Player player);

    Object connection(Player player);

    Object gamePacketListener(Player player);

    String[] injectBeforeNames();
}
