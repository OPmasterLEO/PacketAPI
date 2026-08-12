package org.mastersmp.packet.nms.shared;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundAnimatePacket;
import net.minecraft.network.protocol.game.ClientboundBlockDestructionPacket;
import net.minecraft.network.protocol.game.ClientboundBlockEventPacket;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundEntityEventPacket;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.network.protocol.game.ClientboundRotateHeadPacket;
import net.minecraft.network.protocol.game.ClientboundSetCameraPacket;
import net.minecraft.network.protocol.game.ClientboundSetExperiencePacket;
import net.minecraft.network.protocol.game.ClientboundSetHealthPacket;
import net.minecraft.network.protocol.game.ClientboundSetPassengersPacket;
import net.minecraft.network.protocol.game.ClientboundTakeItemEntityPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.mastersmp.packet.nms.PacketBridge;

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
        return value instanceof Integer i ? i : -1;
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
    public Object removeEntities(int... entityIds) {
        return new ClientboundRemoveEntitiesPacket(entityIds);
    }

    @Override
    public Object entityEvent(Object nmsEntity, byte event) {
        if (!(nmsEntity instanceof Entity entity)) {
            throw new IllegalArgumentException("entityEvent requires net.minecraft.world.entity.Entity");
        }
        return new ClientboundEntityEventPacket(entity, event);
    }

    @Override
    public Object animate(Object nmsEntity, int action) {
        if (!(nmsEntity instanceof Entity entity)) {
            throw new IllegalArgumentException("animate requires net.minecraft.world.entity.Entity");
        }
        return new ClientboundAnimatePacket(entity, action);
    }

    @Override
    public Object rotateHead(Object nmsEntity, float yaw) {
        if (!(nmsEntity instanceof Entity entity)) {
            throw new IllegalArgumentException("rotateHead requires net.minecraft.world.entity.Entity");
        }
        return new ClientboundRotateHeadPacket(entity, (byte) Math.floor(yaw * 256.0f / 360.0f));
    }

    @Override
    public Object setCamera(Object nmsEntity) {
        if (!(nmsEntity instanceof Entity entity)) {
            throw new IllegalArgumentException("setCamera requires net.minecraft.world.entity.Entity");
        }
        return new ClientboundSetCameraPacket(entity);
    }

    @Override
    public Object setPassengers(Object nmsVehicle) {
        if (!(nmsVehicle instanceof Entity entity)) {
            throw new IllegalArgumentException("setPassengers requires net.minecraft.world.entity.Entity");
        }
        return new ClientboundSetPassengersPacket(entity);
    }

    @Override
    public Object collectItem(int collectedId, int collectorId, int amount) {
        return new ClientboundTakeItemEntityPacket(collectedId, collectorId, amount);
    }

    @Override
    public Object blockUpdate(int x, int y, int z, Object nmsBlockState) {
        BlockState state = nmsBlockState instanceof BlockState blockState
                ? blockState
                : Blocks.AIR.defaultBlockState();
        return new ClientboundBlockUpdatePacket(new BlockPos(x, y, z), state);
    }

    @Override
    public Object blockEvent(int x, int y, int z, Object nmsBlock, int type, int data) {
        Block block = nmsBlock instanceof Block b ? b : Blocks.ENDER_CHEST;
        return new ClientboundBlockEventPacket(new BlockPos(x, y, z), block, type, data);
    }

    @Override
    public Object blockDestruction(int entityId, int x, int y, int z, int progress) {
        return new ClientboundBlockDestructionPacket(entityId, new BlockPos(x, y, z), progress);
    }

    @Override
    public Object setHealth(float health, int food, float saturation) {
        return new ClientboundSetHealthPacket(health, food, saturation);
    }

    @Override
    public Object setExperience(float progress, int totalXp, int level) {
        return new ClientboundSetExperiencePacket(progress, totalXp, level);
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
