package org.mastersmp.packet.nms.shared;

import java.lang.reflect.Field;

final class Reflect {

    private Reflect() {
    }

    static Field field(Class<?> owner, String... names) {
        for (String name : names) {
            try {
                Field field = owner.getDeclaredField(name);
                field.setAccessible(true);
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
