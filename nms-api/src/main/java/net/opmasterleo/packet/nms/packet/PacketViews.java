package net.opmasterleo.packet.nms.packet;

import java.util.UUID;

import net.kyori.adventure.text.Component;

/**
 * Version-agnostic views / construction specs for packets whose layout breaks across versions.
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

    public record Vec3d(double x, double y, double z) {
        public static final Vec3d ZERO = new Vec3d(0, 0, 0);
    }

    public record EntityPose(double x, double y, double z, float pitch, float yaw) {
        public EntityPose at(double x, double y, double z) {
            return new EntityPose(x, y, z, pitch, yaw);
        }
    }

    /**
     * Spec for {@code ClientboundAddEntityPacket} / {@code PacketPlayOutSpawnEntity}.
     */
    public record AddEntitySpec(
            int entityId,
            UUID uuid,
            EntityPose pose,
            Object nmsEntityType,
            int data,
            Vec3d velocity,
            double headYaw
    ) {
        public AddEntitySpec {
            if (uuid == null) {
                uuid = UUID.randomUUID();
            }
            if (pose == null) {
                pose = new EntityPose(0, 0, 0, 0f, 0f);
            }
            if (velocity == null) {
                velocity = Vec3d.ZERO;
            }
        }
    }

    public record TextDisplayStyle(
            Component text,
            int lineWidth,
            int backgroundColor,
            byte textOpacity,
            boolean seeThrough
    ) {
        public TextDisplayStyle {
            if (text == null) {
                text = Component.empty();
            }
        }
    }

    /**
     * Spec for spawning a text display (add-entity + metadata).
     */
    public record TextDisplaySpec(int entityId, Vec3d pos, TextDisplayStyle style) {
        public TextDisplaySpec {
            if (pos == null) {
                pos = Vec3d.ZERO;
            }
            if (style == null) {
                style = new TextDisplayStyle(Component.empty(), 200, 0x40000000, (byte) -1, false);
            }
        }
    }
}
