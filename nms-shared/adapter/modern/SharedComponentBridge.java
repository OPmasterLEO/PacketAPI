package org.mastersmp.packet.nms.shared;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.mastersmp.packet.nms.ComponentBridge;

public final class SharedComponentBridge implements ComponentBridge {

    @Override
    public Object fromAdventure(Component component) {
        return io.papermc.paper.adventure.PaperAdventure.asVanilla(component);
    }

    @Override
    public Component toAdventure(Object nmsComponent) {
        if (nmsComponent instanceof net.minecraft.network.chat.Component component) {
            return io.papermc.paper.adventure.PaperAdventure.asAdventure(component);
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
