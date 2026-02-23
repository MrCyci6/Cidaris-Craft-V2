package fr.cidaris.craft.config.files;

import fr.cidaris.craft.config.AbstractConfig;
import org.bukkit.plugin.java.JavaPlugin;

public class GuisConfig extends AbstractConfig {
    public GuisConfig(JavaPlugin plugin) {
        super(plugin, "guis.yml");
    }
}