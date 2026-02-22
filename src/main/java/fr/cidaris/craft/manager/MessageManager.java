package fr.cidaris.craft.manager;

import fr.cidaris.craft.CidarisCraftPlugin;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public class MessageManager {
    private final CidarisCraftPlugin plugin;
    private FileConfiguration messages;

    public MessageManager(CidarisCraftPlugin plugin) {
        this.plugin = plugin;
        loadMessages();
    }

    public void loadMessages() {
        File file = new File(plugin.getDataFolder(), "messages.yml");
        messages = YamlConfiguration.loadConfiguration(file);
    }

    public String get(String path) {
        String prefix = messages.getString("prefix", "");
        String msg = messages.getString(path, "Missing message: " + path);
        return ChatColor.translateAlternateColorCodes('&', prefix + msg);
    }

    public String getRaw(String path) {
        return ChatColor.translateAlternateColorCodes('&', messages.getString(path, ""));
    }
}