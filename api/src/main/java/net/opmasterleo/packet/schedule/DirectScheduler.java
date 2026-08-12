package net.opmasterleo.packet.schedule;

import java.util.Objects;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;

public final class DirectScheduler {

    private final Plugin plugin;
    private final PlatformDetect platform;
    private final AsyncWorkerPool workers;

    DirectScheduler(Plugin plugin, PlatformDetect platform, AsyncWorkerPool workers) {
        this.plugin = plugin;
        this.platform = platform;
        this.workers = workers;
    }

    public void executeGlobal(Runnable task) {
        Objects.requireNonNull(task, "task");
        if (platform.isPaper()) {
            Object scheduler = invoke(Bukkit.getServer(), "getGlobalRegionScheduler");
            invoke(
                    scheduler,
                    "execute",
                    new Class<?>[]{Plugin.class, Runnable.class},
                    plugin,
                    task
            );
            return;
        }
        Bukkit.getScheduler().runTask(plugin, task);
    }

    public void executeEntity(Entity entity, Runnable task) {
        Objects.requireNonNull(entity, "entity");
        Objects.requireNonNull(task, "task");
        if (platform.isPaper()) {
            Object scheduler = invoke(entity, "getScheduler");
            invoke(
                    scheduler,
                    "execute",
                    new Class<?>[]{Plugin.class, Runnable.class, Runnable.class},
                    plugin,
                    task,
                    null
            );
            return;
        }
        Bukkit.getScheduler().runTask(plugin, task);
    }

    public void executeRegion(Location location, Runnable task) {
        Objects.requireNonNull(location, "location");
        Objects.requireNonNull(task, "task");
        if (location.getWorld() == null) {
            return;
        }
        if (platform.isPaper()) {
            Object scheduler = invoke(Bukkit.getServer(), "getRegionScheduler");
            invoke(
                    scheduler,
                    "execute",
                    new Class<?>[]{Plugin.class, Location.class, Runnable.class},
                    plugin,
                    location,
                    task
            );
            return;
        }
        Bukkit.getScheduler().runTask(plugin, task);
    }

    public void executeAsync(Runnable task) {
        workers.execute(task);
    }

    public void executeIo(Runnable task) {
        workers.executeIo(task);
    }

    private static Object invoke(Object target, String method, Class<?>[] types, Object... args) {
        try {
            return target.getClass().getMethod(method, types).invoke(target, args);
        } catch (ReflectiveOperationException error) {
            throw new IllegalStateException("Failed to invoke " + method, error);
        }
    }

    private static Object invoke(Object target, String method) {
        try {
            return target.getClass().getMethod(method).invoke(target);
        } catch (ReflectiveOperationException error) {
            throw new IllegalStateException("Failed to invoke " + method, error);
        }
    }
}
