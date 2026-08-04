package org.mastersmp.packet.channel;

import java.util.ArrayList;
import java.util.List;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
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
        if (channel == null || type == null) {
            return;
        }
        try {
            ChannelHandler handler = channel.pipeline().get(type);
            if (handler != null) {
                channel.pipeline().remove(handler);
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
        channel.eventLoop().submit(task).addListener(listener);
    }
}
