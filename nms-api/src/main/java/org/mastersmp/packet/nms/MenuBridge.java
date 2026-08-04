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
