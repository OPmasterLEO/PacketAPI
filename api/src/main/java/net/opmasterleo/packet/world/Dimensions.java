package net.opmasterleo.packet.world;

import org.bukkit.World;

import net.opmasterleo.packet.nms.NmsAdapter;
import net.opmasterleo.packet.nms.WorldBridge;

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
