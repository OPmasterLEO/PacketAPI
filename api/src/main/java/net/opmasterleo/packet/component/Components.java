package net.opmasterleo.packet.component;

import net.kyori.adventure.text.Component;
import net.opmasterleo.packet.nms.ComponentBridge;
import net.opmasterleo.packet.nms.NmsAdapter;

public final class Components {

    private final ComponentBridge components;

    public Components(NmsAdapter adapter) {
        this.components = adapter.components();
    }

    public Object fromAdventure(Component component) {
        return components.fromAdventure(component);
    }

    public Component toAdventure(Object nmsComponent) {
        return components.toAdventure(nmsComponent);
    }

    public boolean isWorthLine(Component component) {
        return components.isWorthLine(component);
    }
}
