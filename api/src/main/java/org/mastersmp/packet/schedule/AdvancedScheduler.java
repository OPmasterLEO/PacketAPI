package org.mastersmp.packet.schedule;

import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;

public final class AdvancedScheduler {

    private final Plugin plugin;
    private final PlatformDetect platform;
    private final AsyncWorkerPool workers;

    AdvancedScheduler(Plugin plugin, PlatformDetect platform, AsyncWorkerPool workers) {
        this.plugin = plugin;
        this.platform = platform;
        this.workers = workers;
    }

    public TaskHandle runGlobal(Runnable task) {
        return runGlobalLater(task, 0L);
    }

    public TaskHandle runGlobalLater(Runnable task, long delayTicks) {
        Objects.requireNonNull(task, "task");
        if (platform.isPaper()) {
            Object scheduler = invoke(Bukkit.getServer(), "getGlobalRegionScheduler");
            Object scheduled = invoke(
                    scheduler,
                    "runDelayed",
                    new Class<?>[]{Plugin.class, Consumer.class, long.class},
                    plugin,
                    (Consumer<Object>) t -> task.run(),
                    Math.max(1L, delayTicks)
            );
            return TaskHandle.ofFolia(scheduled);
        }
        return TaskHandle.of(Bukkit.getScheduler().runTaskLater(plugin, task, delayTicks));
    }

    public TaskHandle runGlobalRepeating(Runnable task, long delayTicks, long periodTicks) {
        Objects.requireNonNull(task, "task");
        if (platform.isPaper()) {
            Object scheduler = invoke(Bukkit.getServer(), "getGlobalRegionScheduler");
            Object scheduled = invoke(
                    scheduler,
                    "runAtFixedRate",
                    new Class<?>[]{Plugin.class, Consumer.class, long.class, long.class},
                    plugin,
                    (Consumer<Object>) t -> task.run(),
                    Math.max(1L, delayTicks),
                    Math.max(1L, periodTicks)
            );
            return TaskHandle.ofFolia(scheduled);
        }
        return TaskHandle.of(Bukkit.getScheduler().runTaskTimer(plugin, task, delayTicks, periodTicks));
    }

    public TaskHandle runEntity(Entity entity, Runnable task) {
        return runEntityLater(entity, task, 0L);
    }

    public TaskHandle runEntityLater(Entity entity, Runnable task, long delayTicks) {
        Objects.requireNonNull(entity, "entity");
        Objects.requireNonNull(task, "task");
        if (platform.isPaper()) {
            Object scheduler = invoke(entity, "getScheduler");
            Object scheduled = invoke(
                    scheduler,
                    "runDelayed",
                    new Class<?>[]{Plugin.class, Consumer.class, Runnable.class, long.class},
                    plugin,
                    (Consumer<Object>) t -> task.run(),
                    null,
                    Math.max(1L, delayTicks)
            );
            return scheduled == null ? TaskHandle.noop() : TaskHandle.ofFolia(scheduled);
        }
        return TaskHandle.of(Bukkit.getScheduler().runTaskLater(plugin, task, delayTicks));
    }

    public TaskHandle runRegion(Location location, Runnable task) {
        return runRegionLater(location, task, 0L);
    }

    public TaskHandle runRegionLater(Location location, Runnable task, long delayTicks) {
        Objects.requireNonNull(location, "location");
        Objects.requireNonNull(task, "task");
        World world = location.getWorld();
        if (world == null) {
            return TaskHandle.noop();
        }
        if (platform.isPaper()) {
            Object scheduler = invoke(Bukkit.getServer(), "getRegionScheduler");
            Object scheduled = invoke(
                    scheduler,
                    "runDelayed",
                    new Class<?>[]{Plugin.class, Location.class, Consumer.class, long.class},
                    plugin,
                    location,
                    (Consumer<Object>) t -> task.run(),
                    Math.max(1L, delayTicks)
            );
            return TaskHandle.ofFolia(scheduled);
        }
        return TaskHandle.of(Bukkit.getScheduler().runTaskLater(plugin, task, delayTicks));
    }

    public TaskHandle runAsync(Runnable task) {
        workers.execute(task);
        return TaskHandle.noop();
    }

    public TaskHandle runAsyncLater(Runnable task, long delay, TimeUnit unit) {
        return workers.schedule(task, delay, unit);
    }

    public TaskHandle runOffNetty(Entity entity, Runnable task) {
        if (Bukkit.isPrimaryThread() || isOwnedByCurrentRegion(entity)) {
            task.run();
            return TaskHandle.noop();
        }
        return runEntity(entity, task);
    }

    public TaskHandle runOffNetty(Location location, Runnable task) {
        if (Bukkit.isPrimaryThread() || isOwnedByCurrentRegion(location)) {
            task.run();
            return TaskHandle.noop();
        }
        return runRegion(location, task);
    }

    public boolean isOwnedByCurrentRegion(Entity entity) {
        if (!platform.isPaper() || entity == null) {
            return Bukkit.isPrimaryThread();
        }
        try {
            return Bukkit.isOwnedByCurrentRegion(entity);
        } catch (Throwable ignored) {
            return Bukkit.isPrimaryThread();
        }
    }

    public boolean isOwnedByCurrentRegion(Location location) {
        if (!platform.isPaper() || location == null || location.getWorld() == null) {
            return Bukkit.isPrimaryThread();
        }
        try {
            return Bukkit.isOwnedByCurrentRegion(location);
        } catch (Throwable ignored) {
            return Bukkit.isPrimaryThread();
        }
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
