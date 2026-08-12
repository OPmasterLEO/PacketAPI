package net.opmasterleo.packet.world;

import org.bukkit.entity.Entity;

import net.opmasterleo.packet.nms.NmsAdapter;
import net.opmasterleo.packet.nms.WorldBridge;

public final class Entities {

    private final WorldBridge worlds;

    public Entities(NmsAdapter adapter) {
        this.worlds = adapter.worlds();
    }

    public Entity getBukkit(Object entityHandle) {
        return worlds.getBukkit(entityHandle);
    }
}
