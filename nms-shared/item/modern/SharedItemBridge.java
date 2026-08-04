package org.mastersmp.packet.nms.shared;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import org.bukkit.Material;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionType;

import net.kyori.adventure.text.Component;
import org.mastersmp.packet.nms.ItemBridge;

public final class SharedItemBridge implements ItemBridge {

    @Override
    public Object toNms(ItemStack item) {
        return CraftItemStack.asNMSCopy(item);
    }

    @Override
    public ItemStack toBukkit(Object nmsItem) {
        if (nmsItem instanceof net.minecraft.world.item.ItemStack stack) {
            return CraftItemStack.asBukkitCopy(stack);
        }
        return null;
    }

    @Override
    public Object mirror(Object nmsItem) {
        if (nmsItem instanceof net.minecraft.world.item.ItemStack stack) {
            return CraftItemStack.asCraftMirror(stack);
        }
        return null;
    }

    @Override
    public List<Component> getLore(Object nmsItem) {
        ItemStack bukkit = toBukkit(nmsItem);
        if (bukkit == null || !bukkit.hasItemMeta()) {
            return List.of();
        }
        List<Component> lore = bukkit.getItemMeta().lore();
        return lore == null ? List.of() : List.copyOf(lore);
    }

    @Override
    public void setLore(Object nmsItem, List<Component> lore) {
        ItemStack bukkit = asMutableBukkit(nmsItem);
        if (bukkit == null) {
            return;
        }
        ItemMeta meta = bukkit.getItemMeta();
        meta.lore(lore);
        bukkit.setItemMeta(meta);
        copyBack(nmsItem, bukkit);
    }

    @Override
    public void removeLore(Object nmsItem) {
        setLore(nmsItem, null);
    }

    @Override
    public void filterLoreLines(Object nmsItem, Predicate<Component> keep) {
        List<Component> lore = new ArrayList<>(getLore(nmsItem));
        lore.removeIf(line -> !keep.test(line));
        setLore(nmsItem, lore);
    }

    @Override
    public boolean hasCustomName(Object nmsItem) {
        ItemStack bukkit = toBukkit(nmsItem);
        return bukkit != null && bukkit.hasItemMeta() && bukkit.getItemMeta().hasDisplayName();
    }

    @Override
    public boolean hasCustomModelData(Object nmsItem) {
        ItemStack bukkit = toBukkit(nmsItem);
        return bukkit != null && bukkit.hasItemMeta() && bukkit.getItemMeta().hasCustomModelData();
    }

    @Override
    public Map<Enchantment, Integer> getEnchantments(Object nmsItem) {
        ItemStack bukkit = toBukkit(nmsItem);
        return bukkit == null ? Map.of() : Map.copyOf(bukkit.getEnchantments());
    }

    @Override
    public Map<Enchantment, Integer> getStoredEnchantments(Object nmsItem) {
        ItemStack bukkit = toBukkit(nmsItem);
        if (bukkit == null || !(bukkit.getItemMeta() instanceof EnchantmentStorageMeta meta)) {
            return Map.of();
        }
        return Map.copyOf(meta.getStoredEnchants());
    }

    @Override
    public Material materialOf(Object nmsItem) {
        ItemStack bukkit = toBukkit(nmsItem);
        return bukkit == null ? Material.AIR : bukkit.getType();
    }

    @Override
    public ItemStack enchant(ItemStack item, Enchantment enchantment, int level) {
        ItemStack copy = item.clone();
        copy.addUnsafeEnchantment(enchantment, level);
        return copy;
    }

    @Override
    public ItemStack applyPotion(ItemStack item, PotionType potionType) {
        ItemStack copy = item.clone();
        if (copy.getItemMeta() instanceof PotionMeta meta) {
            meta.setBasePotionType(potionType);
            copy.setItemMeta(meta);
        }
        return copy;
    }

    @Override
    public ItemStack sanitizeSpawnerTooltip(ItemStack item) {
        return item.clone();
    }

    @Override
    public Object copyWithCount(Object nmsItem, int count) {
        if (nmsItem instanceof net.minecraft.world.item.ItemStack stack) {
            return stack.copyWithCount(count);
        }
        return air();
    }

    @Override
    public boolean isEmpty(Object nmsItem) {
        return !(nmsItem instanceof net.minecraft.world.item.ItemStack stack) || stack.isEmpty();
    }

    @Override
    public Object air() {
        return net.minecraft.world.item.ItemStack.EMPTY;
    }

    private ItemStack asMutableBukkit(Object nmsItem) {
        if (nmsItem instanceof net.minecraft.world.item.ItemStack stack) {
            return CraftItemStack.asCraftMirror(stack);
        }
        return null;
    }

    private void copyBack(Object nmsItem, ItemStack bukkit) {
        if (!(nmsItem instanceof net.minecraft.world.item.ItemStack target)) {
            return;
        }
        net.minecraft.world.item.ItemStack source = CraftItemStack.asNMSCopy(bukkit);
        target.setCount(source.getCount());
        target.applyComponents(source.getComponents());
    }
}
