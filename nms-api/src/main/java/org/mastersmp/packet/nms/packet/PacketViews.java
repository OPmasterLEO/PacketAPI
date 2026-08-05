package org.mastersmp.packet.nms.packet;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.bukkit.block.BlockFace;
import org.bukkit.util.Vector;

import net.kyori.adventure.text.Component;

public final class PacketViews {

    private PacketViews() {
    }

    public record ExplodeView(
            double x,
            double y,
            double z,
            float power,
            int blockCount,
            Vector knockback,
            Object particle,
            Object sound,
            Object blockParticles
    ) {
    }

    public record EquipmentEntry(String slot, Object nmsItem) {
    }

    public record PlayerInfoEntry(
            UUID profileId,
            String name,
            boolean listed,
            int latency,
            String gameMode,
            Component displayName
    ) {
    }

    public record SuggestionsView(int id, int start, int length, List<String> entries) {
    }

    public record ScoreView(String owner, String objective, int score, Component display) {
    }

    public record ObjectiveView(String name, int method, String renderType) {
    }

    public record OpenScreenView(int containerId, Object menuType, String typeKey, Component title) {
    }

    public record ContainerSlotView(int containerId, int stateId, int slot, Object nmsItem) {
    }

    public record ContainerContentView(int containerId, int stateId, List<Object> items, Object carried) {
    }

    public record InteractView(int entityId, InteractAction action, Hand hand, Optional<Vector> location) {
    }

    public record PlayerActionView(PlayerActionType action, BlockPosView pos, BlockFace face) {
    }

    public record UseItemOnView(Hand hand, BlockPosView pos, BlockFace face) {
    }
}
