package fr.cidaris.craft.database;

import fr.cidaris.craft.CidarisCraftPlugin;
import fr.cidaris.craft.model.PlayerData;
import fr.cidaris.craft.model.enums.UnlockMethod;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;

public class YamlDatabase implements DatabaseProvider {

    private final CidarisCraftPlugin plugin;
    private final File folder;

    public YamlDatabase(CidarisCraftPlugin plugin) {
        this.plugin = plugin;
        this.folder = new File(plugin.getDataFolder(), "players");
        if (!folder.exists()) {
            folder.mkdirs();
        }
    }

    @Override
    public PlayerData loadPlayer(UUID uuid) {
        PlayerData data = new PlayerData(uuid);
        File file = new File(folder, uuid.toString() + ".yml");

        if (file.exists()) {
            FileConfiguration config = YamlConfiguration.loadConfiguration(file);
            ConfigurationSection section = config.getConfigurationSection("unlocked_crafts");

            if (section != null) {
                for (String craftId : section.getKeys(false)) {
                    try {
                        UnlockMethod method = UnlockMethod.valueOf(section.getString(craftId));
                        data.unlockCraft(craftId, method);
                    } catch (IllegalArgumentException e) {
                        plugin.getLogger().warning("Méthode de déblocage inconnue pour " + craftId);
                    }
                }
            }
        }
        return data;
    }

    @Override
    public void savePlayer(PlayerData data) {
        File file = new File(folder, data.getUuid().toString() + ".yml");
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);

        config.createSection("unlocked_crafts");
        for (Map.Entry<String, UnlockMethod> entry : data.getUnlockedCrafts().entrySet()) {
            config.set("unlocked_crafts." + entry.getKey(), entry.getValue().name());
        }

        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("Impossible de sauvegarder les données de " + data.getUuid());
        }
    }
}