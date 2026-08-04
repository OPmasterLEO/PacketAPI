package org.mastersmp.packet.nms.shared;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.World;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;

import net.kyori.adventure.text.Component;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundBlockEventPacket;
import net.minecraft.network.protocol.game.ClientboundBundlePacket;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundSetHealthPacket;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;
import org.mastersmp.packet.nms.PacketBridge;

public final class SharedPacketBridge implements PacketBridge {

    @Override
    public String classify(Object packet) {
        if (packet == null) {
            return "Unknown";
        }
        String simple = packet.getClass().getSimpleName();
        if (simple.startsWith("Clientbound")) {
            return strip(simple, "Clientbound", "Packet");
        }
        if (simple.startsWith("Serverbound")) {
            return strip(simple, "Serverbound", "Packet");
        }
        if (simple.startsWith("PacketPlayOut")) {
            return simple.substring("PacketPlayOut".length());
        }
        if (simple.startsWith("PacketPlayIn")) {
            return simple.substring("PacketPlayIn".length());
        }
        return simple;
    }

    @Override
    public int entityId(Object packet) {
        Object value = Reflect.invoke(packet, "getEntityId", "id", "entityId", "getId");
        if (value instanceof Integer i) {
            return i;
        }
        Object field = Reflect.get(Reflect.field(packet.getClass(), "id", "entityId"), packet);
        return field instanceof Integer i ? i : -1;
    }

    @Override
    public List<Object> unwrapBundle(Object packet) {
        if (!(packet instanceof ClientboundBundlePacket bundle)) {
            return List.of();
        }
        List<Object> out = new ArrayList<>();
        bundle.subPackets().forEach(out::add);
        return out;
    }

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public Object rebuildBundle(List<Object> packets) {
        List raw = new ArrayList(packets);
        return new ClientboundBundlePacket(raw);
    }

    @Override
    public Object createAddTextDisplay(
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
        return new ClientboundAddEntityPacket(
                entityId,
                java.util.UUID.randomUUID(),
                x,
                y,
                z,
                0f,
                0f,
                EntityType.TEXT_DISPLAY,
                0,
                Vec3.ZERO,
                0.0
        );
    }

    @Override
    public Object createRemoveEntities(int... entityIds) {
        return new ClientboundRemoveEntitiesPacket(entityIds);
    }

    @Override
    public Object createSetEntityData(int entityId, List<?> metadataEntries) {
        @SuppressWarnings("unchecked")
        List<SynchedEntityData.DataValue<?>> values = (List<SynchedEntityData.DataValue<?>>) metadataEntries;
        return new ClientboundSetEntityDataPacket(entityId, values);
    }

    @Override
    public Object createBlockEvent(World world, int x, int y, int z, Object blockType, int type, int data) {
        Block block = blockType instanceof Block b ? b : net.minecraft.world.level.block.Blocks.ENDER_CHEST;
        return new ClientboundBlockEventPacket(new BlockPos(x, y, z), block, type, data);
    }

    @Override
    public Object createContainerSetSlot(int containerId, int stateId, int slot, ItemStack item) {
        return new ClientboundContainerSetSlotPacket(
                containerId,
                stateId,
                slot,
                CraftItemStack.asNMSCopy(item)
        );
    }

    @Override
    public Object createSetHealth(float health, int food, float saturation) {
        return new ClientboundSetHealthPacket(health, food, saturation);
    }

    private static String strip(String value, String prefix, String suffix) {
        String out = value;
        if (out.startsWith(prefix)) {
            out = out.substring(prefix.length());
        }
        if (out.endsWith(suffix)) {
            out = out.substring(0, out.length() - suffix.length());
        }
        return out;
    }
}
