package net.opmasterleo.packet.nms.shared;

import org.bukkit.craftbukkit.NMS.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import net.kyori.adventure.text.Component;
import net.minecraft.server.level.ServerPlayer;
import net.opmasterleo.packet.nms.MenuBridge;

import static net.opmasterleo.packet.nms.shared.Reflect.field;
import static net.opmasterleo.packet.nms.shared.Reflect.get;
import static net.opmasterleo.packet.nms.shared.Reflect.invoke;

public final class SharedMenuBridge implements MenuBridge {

    private static final PlayerInvSlots PLAYER_SLOTS = new PlayerInvSlots(
            1, 4, 5, 4, 9, 27, 36, 9, 45
    );

    @Override
    public MenuInfo active(Player player) {
        if (!(player instanceof CraftPlayer craft)) {
            return null;
        }
        ServerPlayer sp = craft.getHandle();
        Object menu = invoke(sp, "containerMenu", "container");
        if (menu == null) {
            menu = get(field(sp.getClass(), "containerMenu", "activeContainer"), sp);
        }
        if (menu == null) {
            return null;
        }
        Object id = get(field(menu.getClass(), "containerId", "windowId"), menu);
        Object slots = get(field(menu.getClass(), "slots", "items"), menu);
        int slotCount = slots instanceof java.util.Collection<?> c ? c.size() : 0;
        return new MenuInfo(id instanceof Integer i ? i : 0, "minecraft:generic_9x3", Component.empty(), slotCount);
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
        return carried != null && !carried.getType().isAir();
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
