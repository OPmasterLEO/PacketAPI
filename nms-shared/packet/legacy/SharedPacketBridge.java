package org.mastersmp.packet.nms.shared;

import java.util.List;

import net.minecraft.server.NMS.Block;
import net.minecraft.server.NMS.BlockPosition;
import net.minecraft.server.NMS.Blocks;
import net.minecraft.server.NMS.Entity;
import net.minecraft.server.NMS.IBlockData;
import net.minecraft.server.NMS.PacketPlayOutAnimation;
import net.minecraft.server.NMS.PacketPlayOutBlockAction;
import net.minecraft.server.NMS.PacketPlayOutBlockBreakAnimation;
import net.minecraft.server.NMS.PacketPlayOutBlockChange;
import net.minecraft.server.NMS.PacketPlayOutCamera;
import net.minecraft.server.NMS.PacketPlayOutCollect;
import net.minecraft.server.NMS.PacketPlayOutEntityDestroy;
import net.minecraft.server.NMS.PacketPlayOutEntityHeadRotation;
import net.minecraft.server.NMS.PacketPlayOutEntityStatus;
import net.minecraft.server.NMS.PacketPlayOutExperience;
import net.minecraft.server.NMS.PacketPlayOutMount;
import net.minecraft.server.NMS.PacketPlayOutUpdateHealth;
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
    public Object removeEntities(int... entityIds) {
        return new PacketPlayOutEntityDestroy(entityIds);
    }

    @Override
    public Object entityEvent(Object nmsEntity, byte event) {
        if (!(nmsEntity instanceof Entity entity)) {
            throw new IllegalArgumentException("entityEvent requires Entity");
        }
        return new PacketPlayOutEntityStatus(entity, event);
    }

    @Override
    public Object animate(Object nmsEntity, int action) {
        if (!(nmsEntity instanceof Entity entity)) {
            throw new IllegalArgumentException("animate requires Entity");
        }
        return new PacketPlayOutAnimation(entity, action);
    }

    @Override
    public Object rotateHead(Object nmsEntity, float yaw) {
        if (!(nmsEntity instanceof Entity entity)) {
            throw new IllegalArgumentException("rotateHead requires Entity");
        }
        return new PacketPlayOutEntityHeadRotation(entity, (byte) Math.floor(yaw * 256.0f / 360.0f));
    }

    @Override
    public Object setCamera(Object nmsEntity) {
        if (!(nmsEntity instanceof Entity entity)) {
            throw new IllegalArgumentException("setCamera requires Entity");
        }
        return new PacketPlayOutCamera(entity);
    }

    @Override
    public Object setPassengers(Object nmsVehicle) {
        if (!(nmsVehicle instanceof Entity entity)) {
            throw new IllegalArgumentException("setPassengers requires Entity");
        }
        return new PacketPlayOutMount(entity);
    }

    @Override
    public Object collectItem(int collectedId, int collectorId, int amount) {
        return new PacketPlayOutCollect(collectedId, collectorId, amount);
    }

    @Override
    public Object blockUpdate(int x, int y, int z, Object nmsBlockState) {
        IBlockData state = nmsBlockState instanceof IBlockData data ? data : Blocks.AIR.getBlockData();
        return new PacketPlayOutBlockChange(new BlockPosition(x, y, z), state);
    }

    @Override
    public Object blockEvent(int x, int y, int z, Object nmsBlock, int type, int data) {
        Block block = nmsBlock instanceof Block b ? b : Blocks.ENDER_CHEST;
        return new PacketPlayOutBlockAction(new BlockPosition(x, y, z), block, type, data);
    }

    @Override
    public Object blockDestruction(int entityId, int x, int y, int z, int progress) {
        return new PacketPlayOutBlockBreakAnimation(entityId, new BlockPosition(x, y, z), progress);
    }

    @Override
    public Object setHealth(float health, int food, float saturation) {
        return new PacketPlayOutUpdateHealth(health, food, saturation);
    }

    @Override
    public Object setExperience(float progress, int totalXp, int level) {
        return new PacketPlayOutExperience(progress, totalXp, level);
    }
}
