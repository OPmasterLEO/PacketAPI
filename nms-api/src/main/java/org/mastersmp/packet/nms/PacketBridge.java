package org.mastersmp.packet.nms;

import java.util.List;
import java.util.UUID;

import net.kyori.adventure.text.Component;
import org.mastersmp.packet.nms.packet.Hand;
import org.mastersmp.packet.nms.packet.InteractAction;
import org.mastersmp.packet.nms.packet.PacketViews;
import org.mastersmp.packet.nms.packet.PlayerActionType;

/**
 * Direct NMS packet factory. Menu/container/sign packets are owned by PacketUxUi / SignGUI
 * and are not duplicated. Methods without a default must be implemented for every era;
 * the rest throw unless that protocol layout exists on the running version.
 */
public interface PacketBridge {

    String classify(Object packet);

    int entityId(Object packet);

    List<Object> unwrapBundle(Object packet);

    default boolean clientbound(Object packet) {
        if (packet == null) {
            return false;
        }
        String name = packet.getClass().getSimpleName();
        return name.startsWith("Clientbound") || name.startsWith("PacketPlayOut");
    }

    default Object rebuildBundle(List<Object> packets) {
        throw unsupported("bundle");
    }

    default Object addEntity(
            int entityId,
            UUID uuid,
            double x,
            double y,
            double z,
            float pitch,
            float yaw,
            Object nmsEntityType,
            int data,
            double vx,
            double vy,
            double vz,
            double headYaw
    ) {
        throw unsupported("addEntity");
    }

    default Object addTextDisplay(
            int entityId,
            double x,
            double y,
            double z,
            Component text,
            int lineWidth,
            int backgroundColor,
            byte textOpacity,
            boolean seeThrough
    ) {
        throw unsupported("addTextDisplay");
    }

    Object removeEntities(int... entityIds);

    default Object setEntityData(int entityId, List<?> metadataEntries) {
        throw unsupported("setEntityData");
    }

    default Object entityVelocity(int entityId, double vx, double vy, double vz) {
        throw unsupported("entityVelocity");
    }

    default Object entityEvent(Object nmsEntity, byte event) {
        throw unsupported("entityEvent");
    }

    default Object animate(Object nmsEntity, int action) {
        throw unsupported("animate");
    }

    default Object rotateHead(Object nmsEntity, float yaw) {
        throw unsupported("rotateHead");
    }

    default Object setCamera(Object nmsEntity) {
        throw unsupported("setCamera");
    }

    default Object setPassengers(Object nmsVehicle) {
        throw unsupported("setPassengers");
    }

    default Object collectItem(int collectedId, int collectorId, int amount) {
        throw unsupported("collectItem");
    }

    default Object setEquipment(int entityId, List<PacketViews.EquipmentEntry> slots) {
        throw unsupported("setEquipment");
    }

    default Object blockUpdate(int x, int y, int z, Object nmsBlockState) {
        throw unsupported("blockUpdate");
    }

    Object blockEvent(int x, int y, int z, Object nmsBlock, int type, int data);

    default Object blockDestruction(int entityId, int x, int y, int z, int progress) {
        throw unsupported("blockDestruction");
    }

    default Object gameEvent(int eventId, float value) {
        throw unsupported("gameEvent");
    }

    default Object setTime(long gameTime, long dayTime) {
        throw unsupported("setTime");
    }

    Object setHealth(float health, int food, float saturation);

    default Object setExperience(float progress, int totalXp, int level) {
        throw unsupported("setExperience");
    }

    default Object systemChat(Component message, boolean overlay) {
        throw unsupported("systemChat");
    }

    default Object actionBar(Component message) {
        throw unsupported("actionBar");
    }

    default Object tabList(Component header, Component footer) {
        throw unsupported("tabList");
    }

    default Object titleText(Component title) {
        throw unsupported("titleText");
    }

    default Object subtitleText(Component subtitle) {
        throw unsupported("subtitleText");
    }

    default Object titleTimes(int fadeIn, int stay, int fadeOut) {
        throw unsupported("titleTimes");
    }

    default Object clearTitles(boolean resetTimes) {
        throw unsupported("clearTitles");
    }

    default Object playerInfoRemove(List<UUID> profileIds) {
        throw unsupported("playerInfoRemove");
    }

    default Object playerInfoAdd(Object nmsServerPlayer) {
        throw unsupported("playerInfoAdd");
    }

    default Object keepAlive(long id) {
        throw unsupported("keepAlive");
    }

    default Object disconnect(Component reason) {
        throw unsupported("disconnect");
    }

    default PacketViews.InteractView interact(Object packet) {
        return new PacketViews.InteractView(entityId(packet), InteractAction.UNKNOWN, Hand.MAIN_HAND);
    }

    default PacketViews.PlayerActionView playerAction(Object packet) {
        return new PacketViews.PlayerActionView(PlayerActionType.UNKNOWN, null);
    }

    private static NmsUnsupportedException unsupported(String packet) {
        return new NmsUnsupportedException("Packet '" + packet + "' is not available on this Minecraft version");
    }
}
