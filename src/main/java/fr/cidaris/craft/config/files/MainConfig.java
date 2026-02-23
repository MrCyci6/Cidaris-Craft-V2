package fr.cidaris.craft.config.files;

import fr.cidaris.craft.config.AbstractConfig;
import org.bukkit.plugin.java.JavaPlugin;

public class MainConfig extends AbstractConfig {
    public MainConfig(JavaPlugin plugin) {
        super(plugin, "config.yml");
    }
}