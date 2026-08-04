package org.mastersmp.packet.packet;

import java.util.List;

import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import net.kyori.adventure.text.Component;
import org.mastersmp.packet.nms.NmsAdapter;
import org.mastersmp.packet.nms.PacketBridge;
import org.mastersmp.packet.nms.PlayerBridge;

public final class Packets {

    private final PlayerBridge players;
    private final PacketBridge packets;

    public Packets(NmsAdapter adapter) {
        this.players = adapter.players();
        this.packets = adapter.packets();
    }

    public void send(Player player, Object nmsPacket) {
        players.send(player, nmsPacket);
    }

    public void spawnTextDisplay(
            Player player,
            int entityId,
            double x,
            double y,
            double z,
            Component text,
            int lineWidth,
            int backgroundColor,
            byte textOpacity,
            boolean seeThrough
    ) {
        Object add = packets.createAddTextDisplay(
                entityId, x, y, z, text, lineWidth, backgroundColor, textOpacity, seeThrough
        );
        players.send(player, add);
    }

    public void destroyEntities(Player player, int... entityIds) {
        players.send(player, packets.createRemoveEntities(entityIds));
    }

    public void setEntityData(Player player, int entityId, List<?> metadataEntries) {
        players.send(player, packets.createSetEntityData(entityId, metadataEntries));
    }

    public void blockEvent(World world, int x, int y, int z, Object blockType, int type, int data) {
        Object packet = packets.createBlockEvent(world, x, y, z, blockType, type, data);
        for (Player player : world.getPlayers()) {
            players.send(player, packet);
        }
    }

    public void sendContainerSetSlot(Player player, int containerId, int stateId, int slot, ItemStack item) {
        players.send(player, packets.createContainerSetSlot(containerId, stateId, slot, item));
    }
}
