package org.mastersmp.packet.nms;

import java.util.stream.Stream;

import org.bukkit.World;
import org.bukkit.entity.Entity;

public interface WorldBridge {

    Object serverLevel(World world);

    Stream<EntityHandle> entities(World world);

    float tickRate(World world);

    void freeze(World world, boolean frozen);

    int maxHorizontalCoord();

    double borderSize(World world);

    int chunkHeight(Object craftChunk, HeightmapType type, int x, int z);

    boolean hasCeiling(World world);

    boolean hasSkyLight(World world);

    void blockEvent(World world, int x, int y, int z, Object block, int type, int data);

    Entity getBukkit(Object entityHandle);

    enum HeightmapType {
        MOTION_BLOCKING,
        MOTION_BLOCKING_NO_LEAVES,
        OCEAN_FLOOR,
        WORLD_SURFACE
    }

    record EntityHandle(Object handle, int entityId, int chunkX, int chunkZ) {
    }
}
