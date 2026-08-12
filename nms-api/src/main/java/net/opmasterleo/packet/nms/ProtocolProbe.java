package net.opmasterleo.packet.nms;

public final class ProtocolProbe {

    private ProtocolProbe() {
    }

    public static int protocolVersion() {
        Object shared = sharedConstants();
        if (shared == null) {
            return -1;
        }
        Object version = invoke(shared, "getCurrentVersion");
        if (version == null) {
            version = invokeStatic("net.minecraft.SharedConstants", "getCurrentVersion");
        }
        if (version == null) {
            return -1;
        }
        Integer protocol = asInt(invoke(version, "getProtocolVersion"));
        if (protocol != null) {
            return protocol;
        }
        protocol = asInt(invoke(version, "protocolVersion"));
        return protocol != null ? protocol : -1;
    }

    public static int dataVersion() {
        Object shared = sharedConstants();
        if (shared == null) {
            return -1;
        }
        Object version = invoke(shared, "getCurrentVersion");
        if (version == null) {
            version = invokeStatic("net.minecraft.SharedConstants", "getCurrentVersion");
        }
        if (version == null) {
            return -1;
        }
        Object data = invoke(version, "getDataVersion");
        if (data != null) {
            Integer id = asInt(invoke(data, "getVersion"));
            if (id != null) {
                return id;
            }
            id = asInt(invoke(data, "version"));
            if (id != null) {
                return id;
            }
        }
        Integer direct = asInt(invoke(version, "getDataVersion"));
        if (direct != null) {
            return direct;
        }
        direct = asInt(invoke(version, "dataVersion"));
        return direct != null ? direct : -1;
    }

    private static Object sharedConstants() {
        try {
            return Class.forName("net.minecraft.SharedConstants");
        } catch (ClassNotFoundException ignored) {
            return null;
        }
    }

    private static Object invoke(Object target, String method) {
        try {
            Class<?> type = target instanceof Class<?> clazz ? clazz : target.getClass();
            Object receiver = target instanceof Class<?> ? null : target;
            return type.getMethod(method).invoke(receiver);
        } catch (ReflectiveOperationException ignored) {
            return null;
        }
    }

    private static Object invokeStatic(String className, String method) {
        try {
            return Class.forName(className).getMethod(method).invoke(null);
        } catch (ReflectiveOperationException ignored) {
            return null;
        }
    }

    private static Integer asInt(Object value) {
        if (value instanceof Integer i) {
            return i;
        }
        if (value instanceof Number n) {
            return n.intValue();
        }
        return null;
    }
}
