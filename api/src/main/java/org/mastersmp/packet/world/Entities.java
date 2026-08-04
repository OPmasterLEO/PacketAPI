package org.mastersmp.packet.world;

import org.bukkit.entity.Entity;

import org.mastersmp.packet.nms.NmsAdapter;
import org.mastersmp.packet.nms.WorldBridge;

public final class Entities {

    private final WorldBridge worlds;

    public Entities(NmsAdapter adapter) {
        this.worlds = adapter.worlds();
    }

    public Entity getBukkit(Object entityHandle) {
        return worlds.getBukkit(entityHandle);
    }
}
