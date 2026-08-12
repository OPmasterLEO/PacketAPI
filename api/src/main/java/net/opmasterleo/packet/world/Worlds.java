package net.opmasterleo.packet.world;

import java.util.stream.Stream;

import org.bukkit.World;

import net.opmasterleo.packet.nms.NmsAdapter;
import net.opmasterleo.packet.nms.WorldBridge;

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
