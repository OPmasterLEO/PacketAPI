package net.opmasterleo.packet.schedule;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;

public final class FastScheduler {

    private final AdvancedScheduler advanced;
    private final ConcurrentLinkedQueue<Runnable> globalQueue = new ConcurrentLinkedQueue<>();
    private final AtomicBoolean globalFlushScheduled = new AtomicBoolean();
    private TaskHandle globalFlushHandle = TaskHandle.noop();

    FastScheduler(Plugin plugin, AdvancedScheduler advanced) {
        this.advanced = Objects.requireNonNull(advanced, "advanced");
    }

    public void coalesceGlobal(Runnable task) {
        globalQueue.add(Objects.requireNonNull(task, "task"));
        if (globalFlushScheduled.compareAndSet(false, true)) {
            globalFlushHandle = advanced.runGlobal(this::flushGlobal);
        }
    }

    public void coalesceEntity(Entity entity, Runnable task) {
        Objects.requireNonNull(entity, "entity");
        Objects.requireNonNull(task, "task");
        ConcurrentLinkedQueue<Runnable> queue = entityQueues(entity);
        queue.add(task);
        if (entityFlushFlags(entity).compareAndSet(false, true)) {
            advanced.runEntity(entity, () -> flushEntity(entity));
        }
    }

    public void shutdown() {
        globalFlushHandle.cancel();
        globalQueue.clear();
        globalFlushScheduled.set(false);
        ENTITY_QUEUES.clear();
        ENTITY_FLAGS.clear();
    }

    private void flushGlobal() {
        try {
            List<Runnable> batch = new ArrayList<>(64);
            Runnable next;
            while ((next = globalQueue.poll()) != null) {
                batch.add(next);
            }
            for (Runnable task : batch) {
                try {
                    task.run();
                } catch (Throwable ignored) {
                }
            }
        } finally {
            globalFlushScheduled.set(false);
            if (!globalQueue.isEmpty() && globalFlushScheduled.compareAndSet(false, true)) {
                globalFlushHandle = advanced.runGlobal(this::flushGlobal);
            }
        }
    }

    private void flushEntity(Entity entity) {
        ConcurrentLinkedQueue<Runnable> queue = entityQueues(entity);
        AtomicBoolean flag = entityFlushFlags(entity);
        try {
            Runnable next;
            while ((next = queue.poll()) != null) {
                try {
                    next.run();
                } catch (Throwable ignored) {
                }
            }
        } finally {
            flag.set(false);
            if (!queue.isEmpty() && flag.compareAndSet(false, true)) {
                advanced.runEntity(entity, () -> flushEntity(entity));
            }
        }
    }

    @SuppressWarnings("unchecked")
    private ConcurrentLinkedQueue<Runnable> entityQueues(Entity entity) {
        Object key = entity.getUniqueId();
        return (ConcurrentLinkedQueue<Runnable>) ENTITY_QUEUES.computeIfAbsent(key, k -> new ConcurrentLinkedQueue<>());
    }

    private AtomicBoolean entityFlushFlags(Entity entity) {
        Object key = entity.getUniqueId();
        return ENTITY_FLAGS.computeIfAbsent(key, k -> new AtomicBoolean());
    }

    private static final ConcurrentHashMap<Object, ConcurrentLinkedQueue<Runnable>> ENTITY_QUEUES =
            new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<Object, AtomicBoolean> ENTITY_FLAGS =
            new ConcurrentHashMap<>();
}
