package org.mastersmp.packet.world;

import org.bukkit.World;

import org.mastersmp.packet.nms.NmsAdapter;
import org.mastersmp.packet.nms.WorldBridge;

public final class Dimensions {

    private final WorldBridge worlds;

    public Dimensions(NmsAdapter adapter) {
        this.worlds = adapter.worlds();
    }

    public boolean hasCeiling(World world) {
        return worlds.hasCeiling(world);
    }

    public boolean hasSkyLight(World world) {
        return worlds.hasSkyLight(world);
    }
}
