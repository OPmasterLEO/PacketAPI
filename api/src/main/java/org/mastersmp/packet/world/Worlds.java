package org.mastersmp.packet.world;

import java.util.stream.Stream;

import org.bukkit.World;

import org.mastersmp.packet.nms.NmsAdapter;
import org.mastersmp.packet.nms.WorldBridge;

public final class Worlds {

    private final WorldBridge worlds;

    public Worlds(NmsAdapter adapter) {
        this.worlds = adapter.worlds();
    }

    public Object serverLevel(World world) {
        return worlds.serverLevel(world);
    }

    public Stream<WorldBridge.EntityHandle> entities(World world) {
        return worlds.entities(world);
    }

    public float tickRate(World world) {
        return worlds.tickRate(world);
    }

    public void freeze(World world, boolean frozen) {
        worlds.freeze(world, frozen);
    }

    public int maxHorizontalCoord() {
        return worlds.maxHorizontalCoord();
    }

    public double borderSize(World world) {
        return worlds.borderSize(world);
    }
}
