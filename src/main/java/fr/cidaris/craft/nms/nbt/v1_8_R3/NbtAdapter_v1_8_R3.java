package fr.cidaris.craft.nms.nbt.v1_8_R3;

import fr.cidaris.craft.nms.nbt.NbtAdapter;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Material;

public class NbtAdapter_v1_8_R3 implements NbtAdapter {

    @Override
    public ItemStack setString(ItemStack item, String key, String value) {
        if (item == null || item.getType() == Material.AIR) return item;
        net.minecraft.server.v1_8_R3.ItemStack nmsItem = CraftItemStack.asNMSCopy(item);
        if (nmsItem == null) return item;

        NBTTagCompound compound = nmsItem.hasTag() ? nmsItem.getTag() : new NBTTagCompound();
        compound.setString(key, value);
        nmsItem.setTag(compound);

        return CraftItemStack.asBukkitCopy(nmsItem);
    }

    @Override
    public String getString(ItemStack item, String key) {
        if (item == null || item.getType() == Material.AIR) return null;
        net.minecraft.server.v1_8_R3.ItemStack nmsItem = CraftItemStack.asNMSCopy(item);
        if (nmsItem == null || !nmsItem.hasTag()) return null;

        NBTTagCompound compound = nmsItem.getTag();
        return compound.hasKey(key) ? compound.getString(key) : null;
    }

    @Override
    public ItemStack removeTag(ItemStack item, String key) {
        if (item == null || item.getType() == Material.AIR) return item;
        net.minecraft.server.v1_8_R3.ItemStack nmsItem = CraftItemStack.asNMSCopy(item);
        if (nmsItem == null || !nmsItem.hasTag()) return item;

        NBTTagCompound compound = nmsItem.getTag();
        compound.remove(key);
        nmsItem.setTag(compound);

        return CraftItemStack.asBukkitCopy(nmsItem);
    }
}