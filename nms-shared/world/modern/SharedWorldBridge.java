package org.mastersmp.packet.nms.shared;

import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.bukkit.World;
import org.bukkit.craftbukkit.CraftChunk;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.entity.Entity;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;
import org.mastersmp.packet.nms.WorldBridge;

public final class SharedWorldBridge implements WorldBridge {

    @Override
    public Object serverLevel(World world) {
        if (world instanceof CraftWorld craft) {
            return craft.getHandle();
        }
        return null;
    }

    @Override
    public Stream<EntityHandle> entities(World world) {
        Object level = serverLevel(world);
        if (!(level instanceof ServerLevel serverLevel)) {
            return Stream.empty();
        }
        return StreamSupport.stream(serverLevel.getAllEntities().spliterator(), false)
                .map(entity -> new EntityHandle(
                        entity,
                        entity.getId(),
                        entity.chunkPosition().x,
                        entity.chunkPosition().z
                ));
    }

    @Override
    public float tickRate(World world) {
        Object level = serverLevel(world);
        if (!(level instanceof ServerLevel serverLevel)) {
            return 20f;
        }
        try {
            Object manager = Reflect.invoke(serverLevel.getServer(), "tickRateManager");
            Object rate = Reflect.invoke(manager, "tickrate");
            return rate instanceof Number n ? n.floatValue() : 20f;
        } catch (Throwable ignored) {
            return 20f;
        }
    }

    @Override
    public void freeze(World world, boolean frozen) {
        Object level = serverLevel(world);
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        try {
            Object manager = Reflect.invoke(serverLevel.getServer(), "tickRateManager");
            Reflect.invoke(manager, "setFrozen", new Class<?>[]{boolean.class}, frozen);
        } catch (Throwable ignored) {
        }
    }

    @Override
    public int maxHorizontalCoord() {
        try {
            FieldValue value = new FieldValue();
            Object constant = Reflect.get(Reflect.field(BlockPos.class, "MAX_HORIZONTAL_COORDINATE", "MAX_HORIZONTAL_COORD"), null);
            if (constant instanceof Integer i) {
                return i;
            }
            return 30_000_000;
        } catch (Throwable ignored) {
            return 30_000_000;
        }
    }

    @Override
    public double borderSize(World world) {
        return world.getWorldBorder().getSize();
    }

    @Override
    public int chunkHeight(Object craftChunk, HeightmapType type, int x, int z) {
        if (!(craftChunk instanceof CraftChunk chunk)) {
            return worldFloor(craftChunk);
        }
        LevelChunk handle = chunk.getHandle(net.minecraft.world.level.chunk.status.ChunkStatus.FULL);
        Heightmap.Types nmsType = switch (type) {
            case MOTION_BLOCKING -> Heightmap.Types.MOTION_BLOCKING;
            case MOTION_BLOCKING_NO_LEAVES -> Heightmap.Types.MOTION_BLOCKING_NO_LEAVES;
            case OCEAN_FLOOR -> Heightmap.Types.OCEAN_FLOOR;
            case WORLD_SURFACE -> Heightmap.Types.WORLD_SURFACE;
        };
        return handle.getHeight(nmsType, x & 15, z & 15);
    }

    @Override
    public boolean hasCeiling(World world) {
        Object level = serverLevel(world);
        if (!(level instanceof ServerLevel serverLevel)) {
            return false;
        }
        return serverLevel.dimensionType().hasCeiling();
    }

    @Override
    public boolean hasSkyLight(World world) {
        Object level = serverLevel(world);
        if (!(level instanceof ServerLevel serverLevel)) {
            return true;
        }
        return serverLevel.dimensionType().hasSkyLight();
    }

    @Override
    public void blockEvent(World world, int x, int y, int z, Object block, int type, int data) {
        Object level = serverLevel(world);
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        Block nmsBlock = block instanceof Block b ? b : net.minecraft.world.level.block.Blocks.ENDER_CHEST;
        serverLevel.blockEvent(new BlockPos(x, y, z), nmsBlock, type, data);
    }

    @Override
    public Entity getBukkit(Object entityHandle) {
        if (entityHandle instanceof net.minecraft.world.entity.Entity entity) {
            return entity.getBukkitEntity();
        }
        if (entityHandle instanceof CraftEntity craft) {
            return craft;
        }
        return null;
    }

    private static int worldFloor(Object craftChunk) {
        return 0;
    }

    private static final class FieldValue {
    }
}
