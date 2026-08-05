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
        Object value = invoke(packet, "b", "a", "getEntityId", "entityId");
        if (value instanceof Integer i) {
            return i;
        }
        Object field = get(field(packet.getClass(), "a", "b", "entityId", "id"), packet);
        return field instanceof Integer i ? i : -1;
    }

    @Override
    public List<Object> unwrapBundle(Object packet) {
        return List.of();
    }

    @Override
    public Object rebuildBundle(List<Object> packets) {
        throw new UnsupportedOperationException("Bundle packets require 1.19.4+");
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
            Class<?> type = Class.forName("net.minecraft.server.NMS.PacketPlayOutEntityDestroy");
            return type.getConstructor(int[].class).newInstance((Object) entityIds);
        } catch (ReflectiveOperationException error) {
            throw new UnsupportedOperationException("EntityDestroy unavailable", error);
        }
    }

    @Override
    public Object createSetEntityData(int entityId, List<?> metadataEntries) {
        throw new UnsupportedOperationException("SetEntityData construction is version-specific on legacy adapters");
    }

    @Override
    public Object createBlockEvent(World world, int x, int y, int z, Object blockType, int type, int data) {
        throw new UnsupportedOperationException("BlockEvent construction is version-specific on legacy adapters");
    }

    @Override
    public Object createContainerSetSlot(int containerId, int stateId, int slot, ItemStack item) {
        try {
            Class<?> type = Class.forName("net.minecraft.server.NMS.PacketPlayOutSetSlot");
            Object nms = CraftItemStack.asNMSCopy(item);
            return type.getConstructor(int.class, int.class, nms.getClass()).newInstance(containerId, slot, nms);
        } catch (ReflectiveOperationException error) {
            throw new UnsupportedOperationException("SetSlot unavailable", error);
        }
    }

    @Override
    public Object createSetHealth(float health, int food, float saturation) {
        try {
            Class<?> type = Class.forName("net.minecraft.server.NMS.PacketPlayOutUpdateHealth");
            return type.getConstructor(float.class, int.class, float.class).newInstance(health, food, saturation);
        } catch (ReflectiveOperationException error) {
            throw new UnsupportedOperationException("UpdateHealth unavailable", error);
        }
    }
}
