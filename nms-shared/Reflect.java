package net.opmasterleo.packet.nms.shared;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Map;

/**
 * Reflection helpers for public Mojang-mapped members and mid/legacy private NMS fields.
 * Prefer {@link Class#getField(String)} / {@link Class#getMethod} when possible.
 */
final class Reflect {

    private Reflect() {
    }

    static Field field(Class<?> owner, String... names) {
        for (String name : names) {
            try {
                return owner.getField(name);
            } catch (NoSuchFieldException ignored) {
            }
            try {
                Field field = owner.getDeclaredField(name);
                // Mid/legacy CraftBukkit fields are not public; Module opens are unavailable at runtime.
                try {
                    field.trySetAccessible();
                } catch (SecurityException ignored) {
                    continue;
                }
                return field;
            } catch (NoSuchFieldException ignored) {
            }
        }
        return null;
    }

    static Object get(Field field, Object instance) {
        if (field == null) {
            return null;
        }
        try {
            return field.get(instance);
        } catch (IllegalAccessException ignored) {
            return null;
        }
    }

    static void set(Field field, Object instance, Object value) {
        if (field == null) {
            return;
        }
        try {
            field.set(instance, value);
        } catch (IllegalAccessException ignored) {
        }
    }

    static Object invoke(Object target, String name, Class<?>[] types, Object... args) {
        if (target == null) {
            return null;
        }
        try {
            Class<?> type = target instanceof Class<?> clazz ? clazz : target.getClass();
            Object receiver = target instanceof Class<?> ? null : target;
            return type.getMethod(name, types).invoke(receiver, args);
        } catch (ReflectiveOperationException ignored) {
            return null;
        }
    }

    static Object invoke(Object target, String... names) {
        if (target == null) {
            return null;
        }
        Class<?> type = target instanceof Class<?> clazz ? clazz : target.getClass();
        Object receiver = target instanceof Class<?> ? null : target;
        for (String name : names) {
            try {
                return type.getMethod(name).invoke(receiver);
            } catch (ReflectiveOperationException ignored) {
            }
        }
        return null;
    }

    static Object construct(Class<?> type, Class<?>[] paramTypes, Object... args) {
        if (type == null) {
            return null;
        }
        try {
            Constructor<?> ctor = type.getDeclaredConstructor(paramTypes);
            try {
                ctor.trySetAccessible();
            } catch (SecurityException ignored) {
                return null;
            }
            return ctor.newInstance(args);
        } catch (ReflectiveOperationException ignored) {
            return null;
        }
    }

    /**
     * Best-effort ClientboundSetTimePacket construction across modern API drift:
     * pre-26 {@code (long, long, boolean)} vs 26.x {@code (long, Map)}.
     */
    static Object constructSetTime(Class<?> type, long gameTime, long dayTime) {
        Object packet = construct(type, new Class<?>[]{long.class, long.class, boolean.class}, gameTime, dayTime, true);
        if (packet != null) {
            return packet;
        }
        for (Constructor<?> ctor : type.getDeclaredConstructors()) {
            Class<?>[] params = ctor.getParameterTypes();
            try {
                ctor.trySetAccessible();
            } catch (SecurityException ignored) {
                continue;
            }
            try {
                if (params.length == 2 && params[0] == long.class && Map.class.isAssignableFrom(params[1])) {
                    return ctor.newInstance(gameTime, Map.of());
                }
                if (params.length == 3
                        && params[0] == long.class
                        && params[1] == long.class
                        && Map.class.isAssignableFrom(params[2])) {
                    return ctor.newInstance(gameTime, dayTime, Map.of());
                }
                if (params.length == 3
                        && params[0] == long.class
                        && params[1] == long.class
                        && params[2] == boolean.class) {
                    return ctor.newInstance(gameTime, dayTime, true);
                }
            } catch (ReflectiveOperationException ignored) {
            }
        }
        return null;
    }

    /**
     * Resolve built-in entity types across modern API drift:
     * pre-26.2 constants live on {@code EntityType}, 26.2+ on {@code EntityTypes}.
     */
    @SuppressWarnings("unchecked")
    static <T> T entityType(String name) {
        for (String owner : new String[]{
                "net.minecraft.world.entity.EntityType",
                "net.minecraft.world.entity.EntityTypes"
        }) {
            try {
                Class<?> type = Class.forName(owner);
                Object value = get(field(type, name), null);
                if (value != null) {
                    return (T) value;
                }
            } catch (ClassNotFoundException ignored) {
            }
        }
        throw new IllegalStateException("Missing EntityType constant: " + name);
    }
}
