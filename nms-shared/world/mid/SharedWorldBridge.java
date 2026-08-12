package net.opmasterleo.packet.nms.shared;

import java.util.stream.Stream;

import org.bukkit.World;
import org.bukkit.craftbukkit.NMS.CraftWorld;
import org.bukkit.entity.Entity;

import net.opmasterleo.packet.nms.WorldBridge;

import static net.opmasterleo.packet.nms.shared.Reflect.invoke;

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
        return world.getEntities().stream().map(entity -> new EntityHandle(
                entity,
                entity.getEntityId(),
                entity.getLocation().getBlockX() >> 4,
                entity.getLocation().getBlockZ() >> 4
        ));
    }

    @Override
    public float tickRate(World world) {
        return 20f;
    }

    @Override
    public void freeze(World world, boolean frozen) {
    }

    @Override
    public int maxHorizontalCoord() {
        return 30_000_000;
    }

    @Override
    public double borderSize(World world) {
        return world.getWorldBorder().getSize();
    }

    @Override
    public int chunkHeight(Object craftChunk, HeightmapType type, int x, int z) {
        if (craftChunk instanceof org.bukkit.Chunk chunk) {
            return chunk.getWorld().getHighestBlockYAt(chunk.getX() * 16 + (x & 15), chunk.getZ() * 16 + (z & 15));
        }
        return 0;
    }

    @Override
    public boolean hasCeiling(World world) {
        return world.getEnvironment() == World.Environment.NETHER;
    }

    @Override
    public boolean hasSkyLight(World world) {
        return world.getEnvironment() != World.Environment.NETHER;
    }

    @Override
    public void blockEvent(World world, int x, int y, int z, Object block, int type, int data) {
        world.getBlockAt(x, y, z).getState().update(true, false);
    }

    @Override
    public Entity getBukkit(Object entityHandle) {
        if (entityHandle instanceof Entity entity) {
            return entity;
        }
        Object bukkit = invoke(entityHandle, "getBukkitEntity");
        return bukkit instanceof Entity entity ? entity : null;
    }
}
