package net.opmasterleo.packet.world;

import org.bukkit.Chunk;

import net.opmasterleo.packet.nms.NmsAdapter;
import net.opmasterleo.packet.nms.WorldBridge;

public final class Chunks {

    private final WorldBridge worlds;

    public Chunks(NmsAdapter adapter) {
        this.worlds = adapter.worlds();
    }

    public int getHeight(Chunk chunk, WorldBridge.HeightmapType type, int x, int z) {
        return worlds.chunkHeight(chunk, type, x, z);
    }
}
