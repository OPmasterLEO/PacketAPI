package org.mastersmp.packet.nms.shared;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.World;
import org.bukkit.craftbukkit.NMS.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;

import net.kyori.adventure.text.Component;
import org.mastersmp.packet.nms.PacketBridge;

import static org.mastersmp.packet.nms.shared.Reflect.field;
import static org.mastersmp.packet.nms.shared.Reflect.get;
import static org.mastersmp.packet.nms.shared.Reflect.invoke;

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
        return simple;
    }

    @Override
    public int entityId(Object packet) {
        Object value = invoke(packet, "getEntityId", "id", "entityId", "getId");
        if (value instanceof Integer i) {
            return i;
        }
        Object field = get(field(packet.getClass(), "id", "entityId"), packet);
        return field instanceof Integer i ? i : -1;
    }

    @Override
    public List<Object> unwrapBundle(Object packet) {
        Object iterable = invoke(packet, "subPackets", "getPackets");
        if (!(iterable instanceof Iterable<?> it)) {
            return List.of();
        }
        List<Object> out = new ArrayList<>();
        for (Object part : it) {
            out.add(part);
        }
        return out;
    }

    @Override
    public Object rebuildBundle(List<Object> packets) {
        throw new UnsupportedOperationException("Bundle rebuild is not available on this mid-version adapter");
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
        throw new UnsupportedOperationException("Text display packets require 1.19.4+");
    }

    @Override
    public Object createRemoveEntities(int... entityIds) {
        try {
            Class<?> type = Class.forName("net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket");
            return type.getConstructor(int[].class).newInstance((Object) entityIds);
        } catch (ReflectiveOperationException error) {
            throw new UnsupportedOperationException("RemoveEntities packet unavailable", error);
        }
    }

    @Override
    public Object createSetEntityData(int entityId, List<?> metadataEntries) {
        throw new UnsupportedOperationException("SetEntityData construction is version-specific on mid adapters");
    }

    @Override
    public Object createBlockEvent(World world, int x, int y, int z, Object blockType, int type, int data) {
        throw new UnsupportedOperationException("BlockEvent construction is version-specific on mid adapters");
    }

    @Override
    public Object createContainerSetSlot(int containerId, int stateId, int slot, ItemStack item) {
        try {
            Class<?> type = Class.forName("net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket");
            Object nms = CraftItemStack.asNMSCopy(item);
            try {
                return type.getConstructor(int.class, int.class, int.class, nms.getClass()).newInstance(containerId, stateId, slot, nms);
            } catch (NoSuchMethodException ignored) {
                return type.getConstructor(int.class, int.class, nms.getClass()).newInstance(containerId, slot, nms);
            }
        } catch (ReflectiveOperationException error) {
            throw new UnsupportedOperationException("ContainerSetSlot unavailable", error);
        }
    }

    @Override
    public Object createSetHealth(float health, int food, float saturation) {
        try {
            Class<?> type = Class.forName("net.minecraft.network.protocol.game.ClientboundSetHealthPacket");
            return type.getConstructor(float.class, int.class, float.class).newInstance(health, food, saturation);
        } catch (ReflectiveOperationException error) {
            throw new UnsupportedOperationException("SetHealth unavailable", error);
        }
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
