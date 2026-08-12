package net.opmasterleo.packet.nms;

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

    default Entity getById(World world, int entityId) {
        return entities(world)
                .filter(handle -> handle.entityId() == entityId)
                .map(handle -> getBukkit(handle.handle()))
                .filter(entity -> entity != null)
                .findFirst()
                .orElse(null);
    }

    default boolean tickFrozen(World world) {
        return false;
    }

    default int maxHorizontalBlock(World world) {
        int server = maxHorizontalCoord();
        double border = borderSize(world) / 2.0;
        return (int) Math.min(server, border);
    }

    default int minY(World world) {
        return world.getMinHeight();
    }

    default int logicalHeight(World world) {
        return world.getMaxHeight() - world.getMinHeight();
    }

    enum HeightmapType {
        MOTION_BLOCKING,
        MOTION_BLOCKING_NO_LEAVES,
        OCEAN_FLOOR,
        WORLD_SURFACE
    }

    record EntityHandle(Object handle, int entityId, int chunkX, int chunkZ) {
    }
}
