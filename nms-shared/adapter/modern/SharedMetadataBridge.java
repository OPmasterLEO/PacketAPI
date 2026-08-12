package net.opmasterleo.packet.nms.shared;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import net.kyori.adventure.text.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import net.opmasterleo.packet.nms.ComponentBridge;
import net.opmasterleo.packet.nms.MetadataBridge;

import static net.opmasterleo.packet.nms.shared.Reflect.invoke;

public final class SharedMetadataBridge implements MetadataBridge {

    private static final byte SEE_THROUGH = 1 << 1;
    private static final AtomicInteger FALLBACK_IDS = new AtomicInteger(1_000_000);

    private final ComponentBridge components;

    public SharedMetadataBridge(ComponentBridge components) {
        this.components = components;
    }

    @Override
    public Object accessor(Class<?> owner, String fieldName) {
        try {
            var field = owner.getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(null);
        } catch (ReflectiveOperationException error) {
            throw new IllegalStateException("Missing entity data accessor " + owner.getName() + "." + fieldName, error);
        }
    }

    @Override
    public Object dataValue(Object accessor, Object value) {
        if (!(accessor instanceof EntityDataAccessor<?> dataAccessor)) {
            throw new IllegalArgumentException("accessor");
        }
        @SuppressWarnings({"rawtypes", "unchecked"})
        SynchedEntityData.DataValue created = SynchedEntityData.DataValue.create((EntityDataAccessor) dataAccessor, value);
        return created;
    }

    @Override
    public Object dataValue(int id, Object value) {
        throw new UnsupportedOperationException("Use dataValue(accessor, value) on modern NMS");
    }

    @Override
    public List<?> textDisplayValues(
            Component text,
            int lineWidth,
            int backgroundColor,
            byte textOpacity,
            boolean seeThrough
    ) {
        Object vanilla = components.fromAdventure(text == null ? Component.empty() : text);
        byte flags = seeThrough ? SEE_THROUGH : 0;
        List<Object> values = new ArrayList<>(5);
        values.add(dataValue(accessor(Display.TextDisplay.class, "DATA_TEXT_ID"), vanilla));
        values.add(dataValue(accessor(Display.TextDisplay.class, "DATA_LINE_WIDTH_ID"), lineWidth));
        values.add(dataValue(accessor(Display.TextDisplay.class, "DATA_BACKGROUND_COLOR_ID"), backgroundColor));
        values.add(dataValue(accessor(Display.TextDisplay.class, "DATA_TEXT_OPACITY_ID"), textOpacity));
        values.add(dataValue(accessor(Display.TextDisplay.class, "DATA_STYLE_FLAGS_ID"), flags));
        return values;
    }

    @Override
    public int nextEntityId() {
        Object id = invoke(Entity.class, "nextEntityId");
        if (id instanceof Integer i) {
            return i;
        }
        return FALLBACK_IDS.getAndIncrement();
    }
}
