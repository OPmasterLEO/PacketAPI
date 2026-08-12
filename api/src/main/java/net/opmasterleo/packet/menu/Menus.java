package net.opmasterleo.packet.menu;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import net.opmasterleo.packet.nms.MenuBridge;
import net.opmasterleo.packet.nms.NmsAdapter;

public final class Menus {

    private final MenuBridge menus;

    public Menus(NmsAdapter adapter) {
        this.menus = adapter.menus();
    }

    public MenuBridge.MenuInfo active(Player player) {
        return menus.active(player);
    }

    public boolean isPlayerInventoryMenu(Object menuType) {
        return menus.isPlayerInventoryMenu(menuType);
    }

    public boolean isStorageMenu(Object menuType) {
        return menus.isStorageMenu(menuType);
    }

    public boolean isInteractive(Object menuType) {
        return menus.isInteractive(menuType);
    }

    public MenuBridge.PlayerInvSlots playerInvSlotMapping() {
        return menus.playerInvSlotMapping();
    }

    public ItemStack getCarried(Player player) {
        return menus.getCarried(player);
    }

    public boolean isCarrying(Player player) {
        return menus.isCarrying(player);
    }

    public void sendSlotChange(Player player, int slot, ItemStack item) {
        menus.sendSlotChange(player, slot, item);
    }

    public int incrementStateId(Player player) {
        return menus.incrementStateId(player);
    }
}
