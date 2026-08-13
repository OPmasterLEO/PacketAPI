package net.opmasterleo.packet.channel;

import java.util.ArrayList;
import java.util.List;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

public final class ChannelOps {

    private ChannelOps() {
    }

    public static ChannelHandler get(Channel channel, String name) {
        if (channel == null || name == null) {
            return null;
        }
        try {
            return channel.pipeline().get(name);
        } catch (Throwable ignored) {
            return null;
        }
    }

    public static void remove(Channel channel, String name) {
        if (channel == null || name == null) {
            return;
        }
        try {
            if (channel.pipeline().get(name) != null) {
                channel.pipeline().remove(name);
            }
        } catch (Throwable ignored) {
        }
    }

    public static void remove(Channel channel, Class<? extends ChannelHandler> type) {
        removeAll(channel, type);
    }

    public static void removeAll(Channel channel, Class<? extends ChannelHandler> type) {
        if (channel == null || type == null) {
            return;
        }
        try {
            List<String> toRemove = new ArrayList<>();
            for (String name : channel.pipeline().names()) {
                ChannelHandler handler = channel.pipeline().get(name);
                if (handler != null && type.isInstance(handler)) {
                    toRemove.add(name);
                }
            }
            for (String name : toRemove) {
                channel.pipeline().remove(name);
            }
        } catch (Throwable ignored) {
        }
    }

    public static List<String> pipelineNames(Channel channel) {
        if (channel == null) {
            return List.of();
        }
        try {
            return new ArrayList<>(channel.pipeline().names());
        } catch (Throwable ignored) {
            return List.of();
        }
    }

    public static void runInEventLoop(Channel channel, Runnable task) {
        if (channel == null || task == null) {
            return;
        }
        if (channel.eventLoop().inEventLoop()) {
            task.run();
            return;
        }
        channel.eventLoop().execute(task);
    }

    public static void runInEventLoop(Channel channel, Runnable task, GenericFutureListener<? extends Future<? super Void>> listener) {
        if (channel == null || task == null) {
            return;
        }
        if (channel.eventLoop().inEventLoop()) {
            task.run();
            return;
        }
        @SuppressWarnings({"unchecked", "rawtypes"})
        GenericFutureListener raw = listener;
        channel.eventLoop().submit(task).addListener(raw);
    }

    public static void runCoalesced(Channel channel, AttributeKey<Boolean> gate, Runnable task) {
        if (channel == null || gate == null || task == null) {
            return;
        }
        runInEventLoop(channel, () -> {
            if (Boolean.TRUE.equals(channel.attr(gate).get())) {
                return;
            }
            channel.attr(gate).set(true);
            channel.eventLoop().execute(() -> {
                try {
                    task.run();
                } finally {
                    channel.attr(gate).set(false);
                }
            });
        });
    }
}
