package org.mastersmp.packet.nms;

import java.util.List;

import org.bukkit.World;
import org.bukkit.inventory.ItemStack;

import net.kyori.adventure.text.Component;

public interface PacketBridge {

    String classify(Object packet);

    int entityId(Object packet);

    List<Object> unwrapBundle(Object packet);

    Object rebuildBundle(List<Object> packets);

    Object createAddTextDisplay(
            int entityId,
            double x,
            double y,
            double z,
            Component text,
            int lineWidth,
            int backgroundColor,
            byte textOpacity,
            boolean seeThrough
    );

    Object createRemoveEntities(int... entityIds);

    Object createSetEntityData(int entityId, List<?> metadataEntries);

    Object createBlockEvent(World world, int x, int y, int z, Object blockType, int type, int data);

    Object createContainerSetSlot(int containerId, int stateId, int slot, ItemStack item);

    Object createSetHealth(float health, int food, float saturation);
}
