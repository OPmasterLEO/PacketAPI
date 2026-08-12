package net.opmasterleo.packet.nms.packet;

/**
 * Version-agnostic views for packets whose layout <em>does</em> break across Minecraft versions.
 * Menu/container/sign packets are owned by PacketUxUi / SignGUI and are not duplicated here.
 */
public final class PacketViews {

    private PacketViews() {
    }

    public record EquipmentEntry(String slot, Object nmsItem) {
    }

    public record InteractView(int entityId, InteractAction action, Hand hand) {
    }

    public record PlayerActionView(PlayerActionType action, BlockPosView pos) {
    }
}
