package org.mastersmp.packet.nms.shared;

import org.mastersmp.packet.nms.ComponentBridge;
import org.mastersmp.packet.nms.ConnectionBridge;
import org.mastersmp.packet.nms.ItemBridge;
import org.mastersmp.packet.nms.MenuBridge;
import org.mastersmp.packet.nms.MetadataBridge;
import org.mastersmp.packet.nms.NmsAdapter;
import org.mastersmp.packet.nms.PacketBridge;
import org.mastersmp.packet.nms.PlayerBridge;
import org.mastersmp.packet.nms.WorldBridge;

public abstract class AbstractNmsAdapter implements NmsAdapter {

    private final String bucketId;
    private final ConnectionBridge connection = new SharedConnectionBridge();
    private final PlayerBridge players = new SharedPlayerBridge();
    private final PacketBridge packets = new SharedPacketBridge();
    private final ItemBridge items = new SharedItemBridge();
    private final MenuBridge menus = new SharedMenuBridge();
    private final WorldBridge worlds = new SharedWorldBridge();
    private final ComponentBridge components = new SharedComponentBridge();
    private final MetadataBridge metadata = new SharedMetadataBridge(components);

    protected AbstractNmsAdapter(String bucketId) {
        this.bucketId = bucketId;
    }

    @Override
    public String bucketId() {
        return bucketId;
    }

    @Override
    public ConnectionBridge connection() {
        return connection;
    }

    @Override
    public PlayerBridge players() {
        return players;
    }

    @Override
    public PacketBridge packets() {
        return packets;
    }

    @Override
    public ItemBridge items() {
        return items;
    }

    @Override
    public MenuBridge menus() {
        return menus;
    }

    @Override
    public WorldBridge worlds() {
        return worlds;
    }

    @Override
    public ComponentBridge components() {
        return components;
    }

    @Override
    public MetadataBridge metadata() {
        return metadata;
    }
}
