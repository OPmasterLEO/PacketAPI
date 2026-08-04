package org.mastersmp.packet.nms;

import net.kyori.adventure.text.Component;

public interface ComponentBridge {

    Object fromAdventure(Component component);

    Component toAdventure(Object nmsComponent);

    boolean isWorthLine(Component component);
}
