package org.mastersmp.packet.schedule;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import org.bukkit.plugin.Plugin;

public final class AsyncWorkerPool {

    private final ExecutorService compute;
    private final ExecutorService io;
    private final ScheduledExecutorService scheduled;
    private volatile boolean shutdown;

    public AsyncWorkerPool(Plugin plugin) {
        String prefix = Objects.requireNonNull(plugin, "plugin").getName();
        int computeThreads = Math.max(2, Runtime.getRuntime().availableProcessors());
        this.compute = new ThreadPoolExecutor(
                computeThreads,
                computeThreads,
                60L,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(),
                namedFactory(prefix + "-compute")
        );
        this.io = new ThreadPoolExecutor(
                2,
                Math.max(2, computeThreads / 2),
                60L,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(),
                namedFactory(prefix + "-io")
        );
        this.scheduled = Executors.newSingleThreadScheduledExecutor(namedFactory(prefix + "-sched"));
    }

    public ExecutorService compute() {
        return compute;
    }

    public ExecutorService io() {
        return io;
    }

    public ScheduledExecutorService scheduled() {
        return scheduled;
    }

    public void execute(Runnable task) {
        if (!shutdown) {
            compute.execute(task);
        }
    }

    public void executeIo(Runnable task) {
        if (!shutdown) {
            io.execute(task);
        }
    }

    public <T> CompletableFuture<T> supply(Supplier<T> supplier) {
        return CompletableFuture.supplyAsync(supplier, compute);
    }

    public <T> CompletableFuture<T> supplyIo(Supplier<T> supplier) {
        return CompletableFuture.supplyAsync(supplier, io);
    }

    public TaskHandle schedule(Runnable task, long delay, TimeUnit unit) {
        return TaskHandle.of(scheduled.schedule(task, delay, unit));
    }

    public TaskHandle scheduleAtFixedRate(Runnable task, long initialDelay, long period, TimeUnit unit) {
        return TaskHandle.of(scheduled.scheduleAtFixedRate(task, initialDelay, period, unit));
    }

    public void shutdown() {
        shutdown = true;
        compute.shutdownNow();
        io.shutdownNow();
        scheduled.shutdownNow();
    }

    private static ThreadFactory namedFactory(String prefix) {
        AtomicInteger idx = new AtomicInteger();
        return runnable -> {
            Thread thread = new Thread(runnable, prefix + "-" + idx.incrementAndGet());
            thread.setDaemon(true);
            return thread;
        };
    }
}
