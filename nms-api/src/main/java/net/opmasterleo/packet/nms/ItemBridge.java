package net.opmasterleo.packet.nms;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionType;

import net.kyori.adventure.text.Component;

public interface ItemBridge {

    Object toNms(ItemStack item);

    ItemStack toBukkit(Object nmsItem);

    Object mirror(Object nmsItem);

    List<Component> getLore(Object nmsItem);

    void setLore(Object nmsItem, List<Component> lore);

    void removeLore(Object nmsItem);

    void filterLoreLines(Object nmsItem, Predicate<Component> keep);

    boolean hasCustomName(Object nmsItem);

    boolean hasCustomModelData(Object nmsItem);

    Map<Enchantment, Integer> getEnchantments(Object nmsItem);

    Map<Enchantment, Integer> getStoredEnchantments(Object nmsItem);

    Material materialOf(Object nmsItem);

    ItemStack enchant(ItemStack item, Enchantment enchantment, int level);

    ItemStack applyPotion(ItemStack item, PotionType potionType);

    ItemStack sanitizeSpawnerTooltip(ItemStack item);

    Object copyWithCount(Object nmsItem, int count);

    boolean isEmpty(Object nmsItem);

    Object air();

    default Object toNmsNullable(ItemStack item) {
        return item == null || item.getType().isAir() ? null : toNms(item);
    }

    default void syncToBukkit(ItemStack bukkit, Object nmsItem) {
        ItemStack synced = toBukkit(nmsItem);
        if (bukkit == null || synced == null) {
            return;
        }
        bukkit.setAmount(synced.getAmount());
        if (synced.hasItemMeta()) {
            bukkit.setItemMeta(synced.getItemMeta());
        }
    }

    default boolean isSpawner(Object nmsItem) {
        return materialOf(nmsItem) == Material.SPAWNER;
    }

    default void sanitizeSpawnerTooltipNms(Object nmsItem) {
        sanitizeSpawnerTooltip(toBukkit(nmsItem));
    }

    default ItemStack applyPotion(ItemStack item, String potionKey, boolean extended, boolean upgraded) {
        return item;
    }

    default Object enchantNms(Object nmsItem, Enchantment enchantment, int level) {
        ItemStack bukkit = toBukkit(nmsItem);
        return toNms(enchant(bukkit, enchantment, level));
    }

    default void hideAdditionalTooltip(ItemStack item) {
    }

    default void hideAttributeTooltip(ItemStack item) {
    }

    default int customModelData(ItemStack item) {
        return 0;
    }

    default void setCustomModelData(ItemStack item, int modelData) {
    }

    default void clearEnchantments(Object nmsItem) {
    }

    default void setDamage(Object nmsItem, int damage) {
    }

    default void setCount(Object nmsItem, int count) {
    }

    default Object createNamed(Material material, Component name, List<Component> lore) {
        ItemStack stack = new ItemStack(material);
        var meta = stack.getItemMeta();
        if (meta != null) {
            meta.displayName(name);
            meta.lore(lore);
            stack.setItemMeta(meta);
        }
        return toNms(stack);
    }
}
