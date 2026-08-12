package net.opmasterleo.packet.packet;

import java.util.List;
import java.util.UUID;

import org.bukkit.World;
import org.bukkit.entity.Player;

import net.kyori.adventure.text.Component;
import net.opmasterleo.packet.event.PacketSendEvent;
import net.opmasterleo.packet.nms.MetadataBridge;
import net.opmasterleo.packet.nms.NmsAdapter;
import net.opmasterleo.packet.nms.PacketBridge;
import net.opmasterleo.packet.nms.PlayerBridge;
import net.opmasterleo.packet.nms.packet.PacketViews;
import net.opmasterleo.packet.nms.packet.PacketWrap;

/**
 * High-level send facade. Constructs NMS packets in the version adapter and writes them through
 * {@code ServerPlayer.connection.send} — never Netty. Menu/container/sign packets are not sent
 * here (PacketUxUi / SignGUI).
 */
public final class Packets {

    private final PlayerBridge players;
    private final PacketBridge packets;
    private final MetadataBridge metadata;
    private final PacketTypeCache types = new PacketTypeCache();

    public Packets(NmsAdapter adapter) {
        this.players = adapter.players();
        this.packets = adapter.packets();
        this.metadata = adapter.metadata();
    }

    public PacketWrap wrap(Object nmsPacket) {
        return PacketWrap.of(nmsPacket);
    }

    public String classify(Object nmsPacket) {
        if (nmsPacket == null) {
            return "Unknown";
        }
        return types.getOrCompute(nmsPacket.getClass(), type -> packets.classify(nmsPacket));
    }

    public void send(Player player, Object nmsPacket) {
        if (player == null || nmsPacket == null) {
            return;
        }
        Object outgoing = nmsPacket;
        PacketSendEvent event = PacketSendEvent.callIfListened(player, nmsPacket, players.handle(player));
        if (event != null) {
            if (event.isCancelled()) {
                return;
            }
            outgoing = event.getPacket();
            if (outgoing == null) {
                return;
            }
        }
        players.send(player, outgoing);
    }

    public void sendAll(Player player, Object... nmsPackets) {
        if (nmsPackets == null) {
            return;
        }
        for (Object packet : nmsPackets) {
            send(player, packet);
        }
    }

    public Object handle(Player player) {
        return players.handle(player);
    }

    public void spawnTextDisplay(Player player, PacketViews.TextDisplaySpec spec) {
        Object add = packets.addTextDisplay(spec);
        PacketViews.TextDisplayStyle style = spec.style();
        Object data;
        try {
            data = packets.setEntityData(spec.entityId(), metadata.textDisplayValues(style));
        } catch (RuntimeException ignored) {
            send(player, add);
            return;
        }
        try {
            send(player, packets.rebuildBundle(List.of(add, data)));
        } catch (RuntimeException ignored) {
            send(player, add);
            send(player, data);
        }
    }

    public void destroyEntities(Player player, int... entityIds) {
        send(player, packets.removeEntities(entityIds));
    }

    public void setEntityData(Player player, int entityId, List<?> metadataEntries) {
        send(player, packets.setEntityData(entityId, metadataEntries));
    }

    public void blockEvent(World world, int x, int y, int z, Object blockType, int type, int data) {
        Object packet = packets.blockEvent(x, y, z, blockType, type, data);
        for (Player player : world.getPlayers()) {
            send(player, packet);
        }
    }

    public void blockUpdate(Player player, int x, int y, int z, Object nmsBlockState) {
        send(player, packets.blockUpdate(x, y, z, nmsBlockState));
    }

    public void setHealth(Player player, float health, int food, float saturation) {
        send(player, packets.setHealth(health, food, saturation));
    }

    public void setExperience(Player player, float progress, int totalXp, int level) {
        send(player, packets.setExperience(progress, totalXp, level));
    }

    public void systemChat(Player player, Component message, boolean overlay) {
        send(player, packets.systemChat(message, overlay));
    }

    public void actionBar(Player player, Component message) {
        send(player, packets.actionBar(message));
    }

    public void tabList(Player player, Component header, Component footer) {
        send(player, packets.tabList(header, footer));
    }

    public void title(Player player, Component title, Component subtitle, int fadeIn, int stay, int fadeOut) {
        send(player, packets.titleTimes(fadeIn, stay, fadeOut));
        send(player, packets.subtitleText(subtitle));
        send(player, packets.titleText(title));
    }

    public void clearTitle(Player player, boolean resetTimes) {
        send(player, packets.clearTitles(resetTimes));
    }

    public void playerInfoRemove(Player player, List<UUID> profileIds) {
        send(player, packets.playerInfoRemove(profileIds));
    }

    public void playerInfoAdd(Player viewer, Object nmsServerPlayer) {
        send(viewer, packets.playerInfoAdd(nmsServerPlayer));
    }

    public void setEquipment(Player player, int entityId, List<PacketViews.EquipmentEntry> slots) {
        send(player, packets.setEquipment(entityId, slots));
    }

    public void entityVelocity(Player player, int entityId, double vx, double vy, double vz) {
        send(player, packets.entityVelocity(entityId, vx, vy, vz));
    }

    public void gameEvent(Player player, int eventId, float value) {
        send(player, packets.gameEvent(eventId, value));
    }

    public PacketBridge nms() {
        return packets;
    }
}
