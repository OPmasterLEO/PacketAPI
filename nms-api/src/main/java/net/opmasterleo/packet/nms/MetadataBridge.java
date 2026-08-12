package net.opmasterleo.packet.nms;

import java.util.List;

import net.kyori.adventure.text.Component;
import net.opmasterleo.packet.nms.packet.PacketViews;

public interface MetadataBridge {

    Object accessor(Class<?> owner, String fieldName);

    Object dataValue(Object accessor, Object value);

    Object dataValue(int id, Object value);

    List<?> textDisplayValues(PacketViews.TextDisplayStyle style);

    default List<?> textDisplayValues(
            Component text,
            int lineWidth,
            int backgroundColor,
            byte textOpacity,
            boolean seeThrough
    ) {
        return textDisplayValues(new PacketViews.TextDisplayStyle(text, lineWidth, backgroundColor, textOpacity, seeThrough));
    }

    int nextEntityId();
}
