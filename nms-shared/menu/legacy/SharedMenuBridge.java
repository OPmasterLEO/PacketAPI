package org.mastersmp.packet.nms.shared;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import net.kyori.adventure.text.Component;
import org.mastersmp.packet.nms.MenuBridge;

public final class SharedMenuBridge implements MenuBridge {

    private static final PlayerInvSlots PLAYER_SLOTS = new PlayerInvSlots(
            1, 4, 5, 4, 9, 27, 36, 9, 40
    );

    @Override
    public MenuInfo active(Player player) {
        if (player == null) {
            return null;
        }
        return new MenuInfo(
                0,
                "minecraft:generic_9x3",
                Component.empty(),
                player.getOpenInventory().countSlots()
        );
    }

    @Override
    public boolean isPlayerInventoryMenu(Object menuType) {
        return menuType == null;
    }

    @Override
    public boolean isStorageMenu(Object menuType) {
        return true;
    }

    @Override
    public boolean isInteractive(Object menuType) {
        return false;
    }

    @Override
    public PlayerInvSlots playerInvSlotMapping() {
        return PLAYER_SLOTS;
    }

    @Override
    public ItemStack getCarried(Player player) {
        return player.getItemOnCursor();
    }

    @Override
    public boolean isCarrying(Player player) {
        ItemStack carried = getCarried(player);
        return carried != null && carried.getAmount() > 0 && !carried.getType().name().equals("AIR");
    }

    @Override
    public void sendSlotChange(Player player, int slot, ItemStack item) {
        player.getOpenInventory().setItem(slot, item);
    }

    @Override
    public int incrementStateId(Player player) {
        return 0;
    }
}
