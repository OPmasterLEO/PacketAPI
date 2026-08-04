package org.mastersmp.packet.nms;

import java.util.List;
import java.util.Map;

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

    void filterLoreLines(Object nmsItem, java.util.function.Predicate<Component> keep);

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
}
