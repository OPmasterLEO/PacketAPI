package org.mastersmp.packet.nms;

import java.util.List;

import net.kyori.adventure.text.Component;

public interface MetadataBridge {

    Object accessor(Class<?> owner, String fieldName);

    Object dataValue(Object accessor, Object value);

    Object dataValue(int id, Object value);

    List<?> textDisplayValues(
            Component text,
            int lineWidth,
            int backgroundColor,
            byte textOpacity,
            boolean seeThrough
    );

    int nextEntityId();
}
