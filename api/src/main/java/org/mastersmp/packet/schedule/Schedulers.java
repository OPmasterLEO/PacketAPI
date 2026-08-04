package org.mastersmp.packet.schedule;

import java.util.Objects;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public final class Schedulers {

    private final PlatformDetect platform;
    private final AsyncWorkerPool workers;
    private final AdvancedScheduler advanced;
    private final FastScheduler fast;
    private final DirectScheduler direct;

    public Schedulers(JavaPlugin plugin) {
        Plugin p = Objects.requireNonNull(plugin, "plugin");
        this.platform = new PlatformDetect(p);
        this.workers = new AsyncWorkerPool(p);
        this.advanced = new AdvancedScheduler(p, platform, workers);
        this.fast = new FastScheduler(p, advanced);
        this.direct = new DirectScheduler(p, platform, workers);
    }

    public PlatformDetect platform() {
        return platform;
    }

    public boolean isFolia() {
        return platform.isFolia();
    }

    public boolean isCanvas() {
        return platform.isCanvas();
    }

    public boolean isPaper() {
        return platform.isPaper();
    }

    public AdvancedScheduler advanced() {
        return advanced;
    }

    public FastScheduler fast() {
        return fast;
    }

    public DirectScheduler direct() {
        return direct;
    }

    public AsyncWorkerPool workers() {
        return workers;
    }

    public boolean isOwnedByCurrentRegion(Entity entity) {
        return advanced.isOwnedByCurrentRegion(entity);
    }

    public boolean isOwnedByCurrentRegion(Location location) {
        return advanced.isOwnedByCurrentRegion(location);
    }

    public void shutdown() {
        fast.shutdown();
        workers.shutdown();
    }
}
