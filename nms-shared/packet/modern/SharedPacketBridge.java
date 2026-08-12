package net.opmasterleo.packet.nms.shared;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import com.mojang.datafixers.util.Pair;
import net.kyori.adventure.text.Component;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.common.ClientboundDisconnectPacket;
import net.minecraft.network.protocol.common.ClientboundKeepAlivePacket;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundAnimatePacket;
import net.minecraft.network.protocol.game.ClientboundBlockDestructionPacket;
import net.minecraft.network.protocol.game.ClientboundBlockEventPacket;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundBundlePacket;
import net.minecraft.network.protocol.game.ClientboundClearTitlesPacket;
import net.minecraft.network.protocol.game.ClientboundEntityEventPacket;
import net.minecraft.network.protocol.game.ClientboundGameEventPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoRemovePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.network.protocol.game.ClientboundRotateHeadPacket;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetCameraPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.network.protocol.game.ClientboundSetEquipmentPacket;
import net.minecraft.network.protocol.game.ClientboundSetExperiencePacket;
import net.minecraft.network.protocol.game.ClientboundSetHealthPacket;
import net.minecraft.network.protocol.game.ClientboundSetPassengersPacket;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTimePacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import net.minecraft.network.protocol.game.ClientboundTabListPacket;
import net.minecraft.network.protocol.game.ClientboundTakeItemEntityPacket;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.opmasterleo.packet.nms.PacketBridge;
import net.opmasterleo.packet.nms.packet.BlockPosView;
import net.opmasterleo.packet.nms.packet.Hand;
import net.opmasterleo.packet.nms.packet.InteractAction;
import net.opmasterleo.packet.nms.packet.PacketViews;
import net.opmasterleo.packet.nms.packet.PlayerActionType;

import static net.opmasterleo.packet.nms.shared.Reflect.constructSetTime;
import static net.opmasterleo.packet.nms.shared.Reflect.entityType;
import static net.opmasterleo.packet.nms.shared.Reflect.field;
import static net.opmasterleo.packet.nms.shared.Reflect.get;
import static net.opmasterleo.packet.nms.shared.Reflect.invoke;

public final class SharedPacketBridge implements PacketBridge {

    @Override
    public String classify(Object packet) {
        if (packet == null) {
            return "Unknown";
        }
        String simple = packet.getClass().getSimpleName();
        if (simple.startsWith("Clientbound")) {
            return strip(simple, "Clientbound", "Packet");
        }
        if (simple.startsWith("Serverbound")) {
            return strip(simple, "Serverbound", "Packet");
        }
        return simple;
    }

    @Override
    public int entityId(Object packet) {
        if (packet == null) {
            return -1;
        }
        // Method names drift across modern buckets (id/getId/getEntity/getEntityId).
        Object value = invoke(packet, "getEntityId", "getEntity", "getId", "id", "entityId");
        if (value instanceof Integer i) {
            return i;
        }
        Object fieldValue = get(field(packet.getClass(), "id", "entityId", "entity"), packet);
        return fieldValue instanceof Integer i ? i : -1;
    }

    @Override
    public List<Object> unwrapBundle(Object packet) {
        if (!(packet instanceof ClientboundBundlePacket bundle)) {
            return List.of();
        }
        List<Object> out = new ArrayList<>();
        bundle.subPackets().forEach(out::add);
        return out;
    }

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public Object rebuildBundle(List<Object> packets) {
        List raw = new ArrayList(packets);
        return new ClientboundBundlePacket(raw);
    }

    @Override
    public Object addEntity(PacketViews.AddEntitySpec spec) {
        EntityType<?> type = spec.nmsEntityType() instanceof EntityType<?> provided
                ? provided
                : entityType("ARMOR_STAND");
        PacketViews.EntityPose pose = spec.pose();
        PacketViews.Vec3d velocity = spec.velocity();
        return new ClientboundAddEntityPacket(
                spec.entityId(),
                spec.uuid(),
                pose.x(),
                pose.y(),
                pose.z(),
                pose.pitch(),
                pose.yaw(),
                type,
                spec.data(),
                new Vec3(velocity.x(), velocity.y(), velocity.z()),
                spec.headYaw()
        );
    }

    @Override
    public Object addTextDisplay(PacketViews.TextDisplaySpec spec) {
        PacketViews.Vec3d pos = spec.pos();
        return new ClientboundAddEntityPacket(
                spec.entityId(),
                UUID.randomUUID(),
                pos.x(),
                pos.y(),
                pos.z(),
                0f,
                0f,
                entityType("TEXT_DISPLAY"),
                0,
                Vec3.ZERO,
                0.0
        );
    }

    @Override
    public Object removeEntities(int... entityIds) {
        return new ClientboundRemoveEntitiesPacket(entityIds);
    }

    @Override
    public Object setEntityData(int entityId, List<?> metadataEntries) {
        @SuppressWarnings("unchecked")
        List<SynchedEntityData.DataValue<?>> values = (List<SynchedEntityData.DataValue<?>>) metadataEntries;
        return new ClientboundSetEntityDataPacket(entityId, values);
    }

    @Override
    public Object entityVelocity(int entityId, double vx, double vy, double vz) {
        return new ClientboundSetEntityMotionPacket(entityId, new Vec3(vx, vy, vz));
    }

    @Override
    public Object entityEvent(Object nmsEntity, byte event) {
        if (!(nmsEntity instanceof Entity entity)) {
            throw new IllegalArgumentException("entityEvent requires net.minecraft.world.entity.Entity");
        }
        return new ClientboundEntityEventPacket(entity, event);
    }

    @Override
    public Object animate(Object nmsEntity, int action) {
        if (!(nmsEntity instanceof Entity entity)) {
            throw new IllegalArgumentException("animate requires net.minecraft.world.entity.Entity");
        }
        return new ClientboundAnimatePacket(entity, action);
    }

    @Override
    public Object rotateHead(Object nmsEntity, float yaw) {
        if (!(nmsEntity instanceof Entity entity)) {
            throw new IllegalArgumentException("rotateHead requires net.minecraft.world.entity.Entity");
        }
        return new ClientboundRotateHeadPacket(entity, (byte) Math.floor(yaw * 256.0f / 360.0f));
    }

    @Override
    public Object setCamera(Object nmsEntity) {
        if (!(nmsEntity instanceof Entity entity)) {
            throw new IllegalArgumentException("setCamera requires net.minecraft.world.entity.Entity");
        }
        return new ClientboundSetCameraPacket(entity);
    }

    @Override
    public Object setPassengers(Object nmsVehicle) {
        if (!(nmsVehicle instanceof Entity entity)) {
            throw new IllegalArgumentException("setPassengers requires net.minecraft.world.entity.Entity");
        }
        return new ClientboundSetPassengersPacket(entity);
    }

    @Override
    public Object collectItem(int collectedId, int collectorId, int amount) {
        return new ClientboundTakeItemEntityPacket(collectedId, collectorId, amount);
    }

    @Override
    public Object setEquipment(int entityId, List<PacketViews.EquipmentEntry> slots) {
        List<Pair<EquipmentSlot, net.minecraft.world.item.ItemStack>> pairs = new ArrayList<>(slots.size());
        for (PacketViews.EquipmentEntry entry : slots) {
            net.minecraft.world.item.ItemStack item = entry.nmsItem() instanceof net.minecraft.world.item.ItemStack stack
                    ? stack
                    : net.minecraft.world.item.ItemStack.EMPTY;
            pairs.add(Pair.of(equipmentSlot(entry.slot()), item));
        }
        return new ClientboundSetEquipmentPacket(entityId, pairs);
    }

    @Override
    public Object blockUpdate(int x, int y, int z, Object nmsBlockState) {
        BlockState state = nmsBlockState instanceof BlockState blockState
                ? blockState
                : Blocks.AIR.defaultBlockState();
        return new ClientboundBlockUpdatePacket(new BlockPos(x, y, z), state);
    }

    @Override
    public Object blockEvent(int x, int y, int z, Object nmsBlock, int type, int data) {
        Block block = nmsBlock instanceof Block b ? b : Blocks.ENDER_CHEST;
        return new ClientboundBlockEventPacket(new BlockPos(x, y, z), block, type, data);
    }

    @Override
    public Object blockDestruction(int entityId, int x, int y, int z, int progress) {
        return new ClientboundBlockDestructionPacket(entityId, new BlockPos(x, y, z), progress);
    }

    @Override
    public Object gameEvent(int eventId, float value) {
        return new ClientboundGameEventPacket(new ClientboundGameEventPacket.Type(eventId), value);
    }

    @Override
    public Object setTime(long gameTime, long dayTime) {
        Object packet = constructSetTime(ClientboundSetTimePacket.class, gameTime, dayTime);
        if (packet == null) {
            throw new IllegalStateException("Unable to construct ClientboundSetTimePacket");
        }
        return packet;
    }

    @Override
    public Object setHealth(float health, int food, float saturation) {
        return new ClientboundSetHealthPacket(health, food, saturation);
    }

    @Override
    public Object setExperience(float progress, int totalXp, int level) {
        return new ClientboundSetExperiencePacket(progress, totalXp, level);
    }

    @Override
    public Object systemChat(Component message, boolean overlay) {
        return new ClientboundSystemChatPacket(vanilla(message), overlay);
    }

    @Override
    public Object actionBar(Component message) {
        return new ClientboundSetActionBarTextPacket(vanilla(message));
    }

    @Override
    public Object tabList(Component header, Component footer) {
        return new ClientboundTabListPacket(vanilla(header), vanilla(footer));
    }

    @Override
    public Object titleText(Component title) {
        return new ClientboundSetTitleTextPacket(vanilla(title));
    }

    @Override
    public Object subtitleText(Component subtitle) {
        return new ClientboundSetSubtitleTextPacket(vanilla(subtitle));
    }

    @Override
    public Object titleTimes(int fadeIn, int stay, int fadeOut) {
        return new ClientboundSetTitlesAnimationPacket(fadeIn, stay, fadeOut);
    }

    @Override
    public Object clearTitles(boolean resetTimes) {
        return new ClientboundClearTitlesPacket(resetTimes);
    }

    @Override
    public Object playerInfoRemove(List<UUID> profileIds) {
        return new ClientboundPlayerInfoRemovePacket(profileIds);
    }

    @Override
    public Object playerInfoAdd(Object nmsServerPlayer) {
        if (!(nmsServerPlayer instanceof ServerPlayer sp)) {
            throw new IllegalArgumentException("playerInfoAdd requires net.minecraft.server.level.ServerPlayer");
        }
        return new ClientboundPlayerInfoUpdatePacket(
                EnumSet.of(
                        ClientboundPlayerInfoUpdatePacket.Action.ADD_PLAYER,
                        ClientboundPlayerInfoUpdatePacket.Action.UPDATE_GAME_MODE,
                        ClientboundPlayerInfoUpdatePacket.Action.UPDATE_LISTED,
                        ClientboundPlayerInfoUpdatePacket.Action.UPDATE_LATENCY,
                        ClientboundPlayerInfoUpdatePacket.Action.UPDATE_DISPLAY_NAME
                ),
                List.of(sp)
        );
    }

    @Override
    public Object keepAlive(long id) {
        return new ClientboundKeepAlivePacket(id);
    }

    @Override
    public Object disconnect(Component reason) {
        return new ClientboundDisconnectPacket(vanilla(reason));
    }

    @Override
    public PacketViews.InteractView interact(Object packet) {
        if (!(packet instanceof ServerboundInteractPacket interact)) {
            return PacketBridge.super.interact(packet);
        }
        boolean attack = Boolean.TRUE.equals(invoke(interact, "isAttack"));
        InteractAction action = attack ? InteractAction.ATTACK : InteractAction.INTERACT;
        return new PacketViews.InteractView(entityId(interact), action, Hand.MAIN_HAND);
    }

    @Override
    public PacketViews.PlayerActionView playerAction(Object packet) {
        if (!(packet instanceof ServerboundPlayerActionPacket action)) {
            return PacketBridge.super.playerAction(packet);
        }
        BlockPos pos = action.getPos();
        return new PacketViews.PlayerActionView(playerActionType(action.getAction()), new BlockPosView(pos.getX(), pos.getY(), pos.getZ()));
    }

    private static PlayerActionType playerActionType(ServerboundPlayerActionPacket.Action action) {
        // String switch keeps older modern buckets compiling when newer enums add values (e.g. STAB).
        return switch (action.name()) {
            case "START_DESTROY_BLOCK" -> PlayerActionType.START_DESTROY_BLOCK;
            case "ABORT_DESTROY_BLOCK" -> PlayerActionType.ABORT_DESTROY_BLOCK;
            case "STOP_DESTROY_BLOCK" -> PlayerActionType.STOP_DESTROY_BLOCK;
            case "DROP_ALL_ITEMS" -> PlayerActionType.DROP_ALL_ITEMS;
            case "DROP_ITEM" -> PlayerActionType.DROP_ITEM;
            case "RELEASE_USE_ITEM" -> PlayerActionType.RELEASE_USE_ITEM;
            case "SWAP_ITEM_WITH_OFFHAND" -> PlayerActionType.SWAP_ITEM_WITH_OFFHAND;
            case "STAB" -> PlayerActionType.UNKNOWN;
            default -> PlayerActionType.UNKNOWN;
        };
    }

    private static EquipmentSlot equipmentSlot(String name) {
        if (name == null) {
            return EquipmentSlot.MAINHAND;
        }
        return switch (name.toLowerCase(Locale.ROOT)) {
            case "offhand", "off_hand" -> EquipmentSlot.OFFHAND;
            case "feet", "boots" -> EquipmentSlot.FEET;
            case "legs", "leggings" -> EquipmentSlot.LEGS;
            case "chest" -> EquipmentSlot.CHEST;
            case "head", "helmet" -> EquipmentSlot.HEAD;
            case "body" -> EquipmentSlot.BODY;
            default -> EquipmentSlot.MAINHAND;
        };
    }

    private static net.minecraft.network.chat.Component vanilla(Component component) {
        return io.papermc.paper.adventure.PaperAdventure.asVanilla(
                component == null ? Component.empty() : component
        );
    }

    private static String strip(String value, String prefix, String suffix) {
        String out = value;
        if (out.startsWith(prefix)) {
            out = out.substring(prefix.length());
        }
        if (out.endsWith(suffix)) {
            out = out.substring(0, out.length() - suffix.length());
        }
        return out;
    }
}
