package net.opmasterleo.packet.schedule;

import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

import org.bukkit.scheduler.BukkitTask;

public interface TaskHandle {

    void cancel();

    boolean isCancelled();

    static TaskHandle noop() {
        return Noop.INSTANCE;
    }

    static TaskHandle of(BukkitTask task) {
        return new BukkitHandle(task);
    }

    static TaskHandle of(Future<?> future) {
        return new FutureHandle(future);
    }

    static TaskHandle ofFolia(Object scheduledTask) {
        return new FoliaHandle(scheduledTask);
    }

    final class Noop implements TaskHandle {
        private static final Noop INSTANCE = new Noop();

        @Override
        public void cancel() {
        }

        @Override
        public boolean isCancelled() {
            return true;
        }
    }

    final class BukkitHandle implements TaskHandle {
        private final BukkitTask task;

        private BukkitHandle(BukkitTask task) {
            this.task = task;
        }

        @Override
        public void cancel() {
            task.cancel();
        }

        @Override
        public boolean isCancelled() {
            return task.isCancelled();
        }
    }

    final class FutureHandle implements TaskHandle {
        private final Future<?> future;
        private final AtomicBoolean cancelled = new AtomicBoolean();

        private FutureHandle(Future<?> future) {
            this.future = future;
        }

        @Override
        public void cancel() {
            cancelled.set(true);
            future.cancel(false);
        }

        @Override
        public boolean isCancelled() {
            return cancelled.get() || future.isCancelled();
        }
    }

    final class FoliaHandle implements TaskHandle {
        private final Object task;
        private final AtomicBoolean cancelled = new AtomicBoolean();

        private FoliaHandle(Object task) {
            this.task = task;
        }

        @Override
        public void cancel() {
            cancelled.set(true);
            try {
                task.getClass().getMethod("cancel").invoke(task);
            } catch (ReflectiveOperationException ignored) {
            }
        }

        @Override
        public boolean isCancelled() {
            if (cancelled.get()) {
                return true;
            }
            try {
                Object value = task.getClass().getMethod("isCancelled").invoke(task);
                return value instanceof Boolean b && b;
            } catch (ReflectiveOperationException ignored) {
                return cancelled.get();
            }
        }
    }
}
