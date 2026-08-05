package org.mastersmp.packet.nms;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import net.kyori.adventure.text.Component;

public interface MenuBridge {

    MenuInfo active(Player player);

    boolean isPlayerInventoryMenu(Object menuType);

    boolean isStorageMenu(Object menuType);

    boolean isInteractive(Object menuType);

    PlayerInvSlots playerInvSlotMapping();

    ItemStack getCarried(Player player);

    boolean isCarrying(Player player);

    void sendSlotChange(Player player, int slot, ItemStack item);

    int incrementStateId(Player player);

    default int playerInventoryContainerId() {
        return 0;
    }

    default boolean isPlayerInventoryContainerId(int containerId) {
        return containerId == playerInventoryContainerId();
    }

    default boolean isPlayerInventoryMenu(Player player) {
        MenuInfo info = active(player);
        return info != null && isPlayerInventoryContainerId(info.containerId());
    }

    default int inferTopSlotCount(Object menuType) {
        return 0;
    }

    default int resolveTopSlotCount(Player player, Object menuType, int containerId) {
        MenuInfo info = active(player);
        if (info != null && info.containerId() == containerId && info.slotCount() > 0) {
            return info.slotCount();
        }
        return inferTopSlotCount(menuType);
    }

    default void forceRemoteSlot(Player player, int slot, ItemStack item) {
        sendSlotChange(player, slot, item);
    }

    default void broadcastCarried(Player player) {
    }

    default void pushAllSlots(Player player) {
    }

    record MenuInfo(int containerId, String typeKey, Component title, int slotCount) {
    }

    record PlayerInvSlots(
            int craftingStart,
            int craftingSize,
            int armorStart,
            int armorSize,
            int inventoryStart,
            int inventorySize,
            int hotbarStart,
            int hotbarSize,
            int offhand
    ) {
    }
}
