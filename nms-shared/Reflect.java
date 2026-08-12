package net.opmasterleo.packet.nms.shared;

import java.lang.reflect.Field;

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
}
