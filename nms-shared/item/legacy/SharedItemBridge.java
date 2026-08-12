package net.opmasterleo.packet.nms.shared;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import org.bukkit.Material;
import org.bukkit.craftbukkit.NMS.inventory.CraftItemStack;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionType;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.opmasterleo.packet.nms.ItemBridge;

public final class SharedItemBridge implements ItemBridge {

    @Override
    public Object toNms(ItemStack item) {
        return CraftItemStack.asNMSCopy(item);
    }

    @Override
    public ItemStack toBukkit(Object nmsItem) {
        return CraftItemStack.asBukkitCopy((net.minecraft.server.NMS.ItemStack) nmsItem);
    }

    @Override
    public Object mirror(Object nmsItem) {
        return CraftItemStack.asCraftMirror((net.minecraft.server.NMS.ItemStack) nmsItem);
    }

    @Override
    public List<Component> getLore(Object nmsItem) {
        ItemStack bukkit = toBukkit(nmsItem);
        if (bukkit == null || !bukkit.hasItemMeta()) {
            return List.of();
        }
        ItemMeta meta = bukkit.getItemMeta();
        if (meta == null || !meta.hasLore()) {
            return List.of();
        }
        List<String> lore = meta.getLore();
        if (lore == null || lore.isEmpty()) {
            return List.of();
        }
        List<Component> out = new ArrayList<>(lore.size());
        for (String line : lore) {
            out.add(LegacyComponentSerializer.legacySection().deserialize(line));
        }
        return List.copyOf(out);
    }

    @Override
    public void setLore(Object nmsItem, List<Component> lore) {
        ItemStack bukkit = CraftItemStack.asCraftMirror((net.minecraft.server.NMS.ItemStack) nmsItem);
        ItemMeta meta = bukkit.getItemMeta();
        if (lore == null) {
            meta.setLore(null);
        } else {
            List<String> lines = new ArrayList<>(lore.size());
            for (Component line : lore) {
                lines.add(LegacyComponentSerializer.legacySection().serialize(line));
            }
            meta.setLore(lines);
        }
        bukkit.setItemMeta(meta);
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
        return item.clone();
    }

    @Override
    public ItemStack sanitizeSpawnerTooltip(ItemStack item) {
        return item.clone();
    }

    @Override
    public Object copyWithCount(Object nmsItem, int count) {
        net.minecraft.server.NMS.ItemStack stack = ((net.minecraft.server.NMS.ItemStack) nmsItem).cloneItemStack();
        stack.setCount(count);
        return stack;
    }

    @Override
    public boolean isEmpty(Object nmsItem) {
        return nmsItem == null || ((net.minecraft.server.NMS.ItemStack) nmsItem).isEmpty();
    }

    @Override
    public Object air() {
        return net.minecraft.server.NMS.ItemStack.b;
    }
}
