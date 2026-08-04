package org.mastersmp.packet.packet;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class PacketTypeCache {

    private final Map<Class<?>, String> names = new ConcurrentHashMap<>();

    public String getOrCompute(Class<?> type, java.util.function.Function<Class<?>, String> computer) {
        return names.computeIfAbsent(type, computer);
    }

    public void put(Class<?> type, String name) {
        names.put(type, name);
    }

    public void clear() {
        names.clear();
    }

    public int size() {
        return names.size();
    }
}
