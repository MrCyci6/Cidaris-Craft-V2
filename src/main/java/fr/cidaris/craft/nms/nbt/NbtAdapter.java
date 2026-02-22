package fr.cidaris.craft.nms.nbt;

import org.bukkit.inventory.ItemStack;

public interface NbtAdapter {
    ItemStack setString(ItemStack item, String key, String value);
    String getString(ItemStack item, String key);
    ItemStack removeTag(ItemStack item, String key);
}