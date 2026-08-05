package org.mastersmp.packet.nms;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.inventory.ItemStack;

import net.kyori.adventure.text.Component;
import org.mastersmp.packet.nms.packet.BlockPosView;
import org.mastersmp.packet.nms.packet.Hand;
import org.mastersmp.packet.nms.packet.InteractAction;
import org.mastersmp.packet.nms.packet.PacketViews;
import org.mastersmp.packet.nms.packet.PlayerActionType;

public interface PacketBridge {

    String classify(Object packet);

    int entityId(Object packet);

    List<Object> unwrapBundle(Object packet);

    Object rebuildBundle(List<Object> packets);

    Object createAddTextDisplay(
            int entityId,
            double x,
            double y,
            double z,
            Component text,
            int lineWidth,
            int backgroundColor,
            byte textOpacity,
            boolean seeThrough
    );

    Object createRemoveEntities(int... entityIds);

    Object createSetEntityData(int entityId, List<?> metadataEntries);

    Object createBlockEvent(World world, int x, int y, int z, Object blockType, int type, int data);

    Object createContainerSetSlot(int containerId, int stateId, int slot, ItemStack item);

    Object createSetHealth(float health, int food, float saturation);

    default int interactEntityId(Object packet) {
        return entityId(packet);
    }

    default InteractAction interactAction(Object packet) {
        return InteractAction.UNKNOWN;
    }

    default PacketViews.InteractView interact(Object packet) {
        return new PacketViews.InteractView(interactEntityId(packet), interactAction(packet), Hand.MAIN_HAND, Optional.empty());
    }

    default PlayerActionType playerAction(Object packet) {
        return PlayerActionType.UNKNOWN;
    }

    default BlockPosView playerActionPos(Object packet) {
        return null;
    }

    default PacketViews.PlayerActionView playerActionView(Object packet) {
        return new PacketViews.PlayerActionView(playerAction(packet), playerActionPos(packet), BlockFace.SELF);
    }

    default Hand useItemHand(Object packet) {
        return Hand.MAIN_HAND;
    }

    default BlockPosView useItemOnPos(Object packet) {
        return null;
    }

    default BlockFace useItemOnFace(Object packet) {
        return BlockFace.SELF;
    }

    default PacketViews.UseItemOnView useItemOn(Object packet) {
        return new PacketViews.UseItemOnView(useItemHand(packet), useItemOnPos(packet), useItemOnFace(packet));
    }

    default Optional<String> soundPath(Object packet) {
        return Optional.empty();
    }

    default double soundX(Object packet) {
        return 0;
    }

    default double soundY(Object packet) {
        return 0;
    }

    default double soundZ(Object packet) {
        return 0;
    }

    default double particleX(Object packet) {
        return 0;
    }

    default double particleY(Object packet) {
        return 0;
    }

    default double particleZ(Object packet) {
        return 0;
    }

    default int entityEventId(Object packet) {
        return -1;
    }

    default boolean systemChatOverlay(Object packet) {
        return false;
    }

    default PacketViews.ExplodeView explode(Object packet) {
        return null;
    }

    default List<?> entityDataValues(Object packet) {
        return List.of();
    }

    default int dataValueId(Object dataValue) {
        return -1;
    }

    default Object dataValueValue(Object dataValue) {
        return null;
    }

    default List<PacketViews.EquipmentEntry> equipmentSlots(Object packet) {
        return List.of();
    }

    default int equipmentEntityId(Object packet) {
        return entityId(packet);
    }

    default float packetHealth(Object packet) {
        return 0f;
    }

    default int packetFood(Object packet) {
        return 0;
    }

    default float packetSaturation(Object packet) {
        return 0f;
    }

    default int vehicleId(Object packet) {
        return -1;
    }

    default int[] passengerIds(Object packet) {
        return new int[0];
    }

    default int linkSourceId(Object packet) {
        return -1;
    }

    default int linkDestId(Object packet) {
        return -1;
    }

    default Set<String> playerInfoActions(Object packet) {
        return Set.of();
    }

    default List<PacketViews.PlayerInfoEntry> playerInfoEntries(Object packet) {
        return List.of();
    }

    default List<UUID> playerInfoRemoveIds(Object packet) {
        return List.of();
    }

    default PacketViews.SuggestionsView commandSuggestions(Object packet) {
        return null;
    }

    default Collection<String> teamPlayers(Object packet) {
        return List.of();
    }

    default PacketViews.ScoreView setScore(Object packet) {
        return null;
    }

    default PacketViews.ObjectiveView setObjective(Object packet) {
        return null;
    }

    default PacketViews.OpenScreenView openScreen(Object packet) {
        return null;
    }

    default int containerCloseId(Object packet) {
        return -1;
    }

    default PacketViews.ContainerSlotView containerSetSlot(Object packet) {
        return null;
    }

    default PacketViews.ContainerContentView containerSetContent(Object packet) {
        return null;
    }

    default Object cursorItem(Object packet) {
        return null;
    }

    default Object createDataValue(int id, Object value) {
        throw new UnsupportedOperationException("createDataValue");
    }

    default Object createSetEquipment(int entityId, List<PacketViews.EquipmentEntry> slots) {
        throw new UnsupportedOperationException("createSetEquipment");
    }

    default Object createPlayerInfoUpdate(Set<String> actions, List<PacketViews.PlayerInfoEntry> entries) {
        throw new UnsupportedOperationException("createPlayerInfoUpdate");
    }

    default Object createExplode(PacketViews.ExplodeView view) {
        throw new UnsupportedOperationException("createExplode");
    }

    default Object withExplosionParticle(Object explodePacket, Object particle) {
        throw new UnsupportedOperationException("withExplosionParticle");
    }

    default Object createContainerSetSlotNms(int containerId, int stateId, int slot, Object nmsItem) {
        throw new UnsupportedOperationException("createContainerSetSlotNms");
    }

    default Object createContainerSetContent(int containerId, int stateId, List<Object> items, Object carried) {
        throw new UnsupportedOperationException("createContainerSetContent");
    }

    default Object createSetCursorItem(Object nmsItem) {
        throw new UnsupportedOperationException("createSetCursorItem");
    }

    default Object createSetPassengers(int vehicleId, int... passengerIds) {
        throw new UnsupportedOperationException("createSetPassengers");
    }
}
