package org.mastersmp.packet.nms;

public interface NmsAdapter {

    String bucketId();

    ConnectionBridge connection();

    PlayerBridge players();

    PacketBridge packets();

    ItemBridge items();

    MenuBridge menus();

    WorldBridge worlds();

    ComponentBridge components();
}
