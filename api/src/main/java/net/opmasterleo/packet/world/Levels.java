package net.opmasterleo.packet.world;

import org.bukkit.World;

import net.opmasterleo.packet.nms.NmsAdapter;
import net.opmasterleo.packet.nms.WorldBridge;

public final class Levels {

    private final WorldBridge worlds;

    public Levels(NmsAdapter adapter) {
        this.worlds = adapter.worlds();
    }

    public void blockEvent(World world, int x, int y, int z, Object block, int type, int data) {
        worlds.blockEvent(world, x, y, z, block, type, data);
    }
}
