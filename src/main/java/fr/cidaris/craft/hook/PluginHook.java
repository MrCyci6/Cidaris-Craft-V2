package fr.cidaris.craft.hook;

import org.bukkit.plugin.Plugin;

public interface PluginHook {
    String getPluginName();

    boolean setup(Plugin plugin);

    boolean isHooked();
}