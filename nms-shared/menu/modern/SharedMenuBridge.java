package net.opmasterleo.packet.nms.shared;

import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import net.kyori.adventure.text.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.opmasterleo.packet.nms.MenuBridge;

import static net.opmasterleo.packet.nms.shared.Reflect.invoke;

public final class SharedMenuBridge implements MenuBridge {

    private static final PlayerInvSlots PLAYER_SLOTS = new PlayerInvSlots(
            1, 4,
            5, 4,
            9, 27,
            36, 9,
            45
    );

    @Override
    public MenuInfo active(Player player) {
        ServerPlayer sp = handle(player);
        if (sp == null) {
            return null;
        }
        AbstractContainerMenu menu = sp.containerMenu;
        String typeKey = "minecraft:generic_9x3";
        if (menu.getType() != null) {
            Object key = invoke(MenuType.class, "getKey", new Class<?>[]{MenuType.class}, menu.getType());
            if (key == null) {
                key = invoke(menu.getType(), "toString");
            }
            if (key != null) {
                typeKey = String.valueOf(key);
            }
        }
        Component title = Component.empty();
        Object titleObj = invoke(menu, "getTitle", "title");
        if (titleObj instanceof net.minecraft.network.chat.Component nmsTitle) {
            title = io.papermc.paper.adventure.PaperAdventure.asAdventure(nmsTitle);
        }
        return new MenuInfo(menu.containerId, typeKey, title, menu.slots.size());
    }

    @Override
    public boolean isPlayerInventoryMenu(Object menuType) {
        return menuType == null;
    }

    @Override
    public boolean isStorageMenu(Object menuType) {
        return menuType == MenuType.GENERIC_9x3
                || menuType == MenuType.GENERIC_9x6
                || menuType == MenuType.GENERIC_3x3
                || menuType == MenuType.HOPPER;
    }

    @Override
    public boolean isInteractive(Object menuType) {
        return !isStorageMenu(menuType) && menuType != null;
    }

    @Override
    public PlayerInvSlots playerInvSlotMapping() {
        return PLAYER_SLOTS;
    }

    @Override
    public ItemStack getCarried(Player player) {
        ServerPlayer sp = handle(player);
        if (sp == null) {
            return null;
        }
        return org.bukkit.craftbukkit.inventory.CraftItemStack.asBukkitCopy(sp.containerMenu.getCarried());
    }

    @Override
    public boolean isCarrying(Player player) {
        ServerPlayer sp = handle(player);
        return sp != null && !sp.containerMenu.getCarried().isEmpty();
    }

    @Override
    public void sendSlotChange(Player player, int slot, ItemStack item) {
        ServerPlayer sp = handle(player);
        if (sp == null) {
            return;
        }
        sp.containerMenu.setRemoteSlot(slot, org.bukkit.craftbukkit.inventory.CraftItemStack.asNMSCopy(item));
        sp.containerMenu.broadcastFullState();
    }

    @Override
    public int incrementStateId(Player player) {
        ServerPlayer sp = handle(player);
        if (sp == null) {
            return 0;
        }
        return sp.containerMenu.incrementStateId();
    }

    private static ServerPlayer handle(Player player) {
        if (player instanceof CraftPlayer craft) {
            return craft.getHandle();
        }
        return null;
    }
}
