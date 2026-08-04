package org.mastersmp.packet.item;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionType;

import net.kyori.adventure.text.Component;
import org.mastersmp.packet.nms.ItemBridge;
import org.mastersmp.packet.nms.NmsAdapter;

public final class Items {

    private final ItemBridge items;

    public Items(NmsAdapter adapter) {
        this.items = adapter.items();
    }

    public Object toNms(ItemStack item) {
        return items.toNms(item);
    }

    public ItemStack toBukkit(Object nmsItem) {
        return items.toBukkit(nmsItem);
    }

    public Object mirror(Object nmsItem) {
        return items.mirror(nmsItem);
    }

    public List<Component> getLore(Object nmsItem) {
        return items.getLore(nmsItem);
    }

    public void setLore(Object nmsItem, List<Component> lore) {
        items.setLore(nmsItem, lore);
    }

    public void removeLore(Object nmsItem) {
        items.removeLore(nmsItem);
    }

    public void filterLoreLines(Object nmsItem, Predicate<Component> keep) {
        items.filterLoreLines(nmsItem, keep);
    }

    public boolean hasCustomName(Object nmsItem) {
        return items.hasCustomName(nmsItem);
    }

    public boolean hasCustomModelData(Object nmsItem) {
        return items.hasCustomModelData(nmsItem);
    }

    public Map<Enchantment, Integer> getEnchantments(Object nmsItem) {
        return items.getEnchantments(nmsItem);
    }

    public Map<Enchantment, Integer> getStoredEnchantments(Object nmsItem) {
        return items.getStoredEnchantments(nmsItem);
    }

    public Material materialOf(Object nmsItem) {
        return items.materialOf(nmsItem);
    }

    public ItemStack enchant(ItemStack item, Enchantment enchantment, int level) {
        return items.enchant(item, enchantment, level);
    }

    public ItemStack applyPotion(ItemStack item, PotionType potionType) {
        return items.applyPotion(item, potionType);
    }

    public ItemStack sanitizeSpawnerTooltip(ItemStack item) {
        return items.sanitizeSpawnerTooltip(item);
    }

    public Object copyWithCount(Object nmsItem, int count) {
        return items.copyWithCount(nmsItem, count);
    }

    public boolean isEmpty(Object nmsItem) {
        return items.isEmpty(nmsItem);
    }

    public Object air() {
        return items.air();
    }
}
