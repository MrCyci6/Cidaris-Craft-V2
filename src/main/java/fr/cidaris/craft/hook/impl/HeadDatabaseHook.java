package fr.cidaris.craft.hook.impl;

import fr.cidaris.craft.hook.PluginHook;
import me.arcaniax.hdb.api.HeadDatabaseAPI;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

public class HeadDatabaseHook implements PluginHook {
    private HeadDatabaseAPI api;
    private boolean hooked = false;

    @Override
    public String getPluginName() { return "HeadDatabase"; }

    @Override
    public boolean setup(Plugin plugin) {
        if (Bukkit.getPluginManager().getPlugin("HeadDatabase") != null) {
            this.api = new HeadDatabaseAPI();
            this.hooked = true;
            return true;
        }
        return false;
    }

    public ItemStack getHead(String id) {
        if (!hooked || id == null) return null;
        try {
            return api.getItemHead(id);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public boolean isHooked() { return hooked; }
}