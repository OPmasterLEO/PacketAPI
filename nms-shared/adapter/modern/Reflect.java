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
        try {
            var method = target.getClass().getMethod(name, types);
            return method.invoke(target, args);
        } catch (ReflectiveOperationException ignored) {
            return null;
        }
    }

    static Object invoke(Object target, String... names) {
        for (String name : names) {
            try {
                return target.getClass().getMethod(name).invoke(target);
            } catch (ReflectiveOperationException ignored) {
            }
        }
        return null;
    }
}
