package fr.cidaris.craft.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public abstract class AbstractConfig {

    protected final JavaPlugin plugin;
    private final String fileName;
    private File file;
    private FileConfiguration config;

    public AbstractConfig(JavaPlugin plugin, String fileName) {
        this.plugin = plugin;
        this.fileName = fileName;
    }

    public void load() {
        this.file = new File(plugin.getDataFolder(), fileName);
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            plugin.saveResource(fileName, false);
        }

        // On crée une nouvelle instance vierge
        this.config = new YamlConfiguration();

        try {
            // LECTURE FORCÉE EN UTF-8 (Indispensable pour les accents et symboles en 1.8 !)
            InputStreamReader reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8);
            this.config.load(reader);
            reader.close();
        } catch (Exception e) {
            plugin.getLogger().severe("==========================================");
            plugin.getLogger().severe("ERREUR DANS LE FICHIER : " + fileName);
            plugin.getLogger().severe("Il y a une erreur de syntaxe YAML (espace manquant, guillemet oublié...).");
            plugin.getLogger().severe("Détail de l'erreur : " + e.getMessage());
            plugin.getLogger().severe("==========================================");
        }
    }

    public void save() {
        try {
            config.save(file);
        } catch (Exception e) {
            plugin.getLogger().severe("Impossible de sauvegarder le fichier : " + fileName);
        }
    }

    public FileConfiguration get() {
        return config;
    }

    public String getFileName() {
        return fileName;
    }
}