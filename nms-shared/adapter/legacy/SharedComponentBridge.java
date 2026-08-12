package net.opmasterleo.packet.nms.shared;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.opmasterleo.packet.nms.ComponentBridge;

public final class SharedComponentBridge implements ComponentBridge {

    @Override
    public Object fromAdventure(Component component) {
        return GsonComponentSerializer.gson().serialize(component);
    }

    @Override
    public Component toAdventure(Object nmsComponent) {
        if (nmsComponent instanceof String json) {
            return GsonComponentSerializer.gson().deserialize(json);
        }
        return Component.empty();
    }

    @Override
    public boolean isWorthLine(Component component) {
        if (component == null) {
            return false;
        }
        String plain = PlainTextComponentSerializer.plainText().serialize(component);
        return plain.contains("Worth") || plain.contains("worth");
    }
}
