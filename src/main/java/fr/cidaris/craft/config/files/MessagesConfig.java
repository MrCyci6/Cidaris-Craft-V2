package fr.cidaris.craft.config.files;

import fr.cidaris.craft.config.AbstractConfig;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

public class MessagesConfig extends AbstractConfig {

    public MessagesConfig(JavaPlugin plugin) {
        super(plugin, "messages.yml");
    }

    public String getMessage(String path) {
        String msg = get().getString(path);
        if (msg == null) return "§cMessage manquant : " + path;

        String prefix = get().getString("prefix", "");
        return ChatColor.translateAlternateColorCodes('&', prefix + msg);
    }

    public String getRawMessage(String path) {
        String msg = get().getString(path);
        if (msg == null) return "§cMessage manquant : " + path;
        return ChatColor.translateAlternateColorCodes('&', msg);
    }
}