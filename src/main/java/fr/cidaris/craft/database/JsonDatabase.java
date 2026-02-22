package fr.cidaris.craft.database;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import fr.cidaris.craft.CidarisCraftPlugin;
import fr.cidaris.craft.model.PlayerData;
import fr.cidaris.craft.model.enums.UnlockMethod;

import java.io.*;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.UUID;

public class JsonDatabase implements DatabaseProvider {

    private final CidarisCraftPlugin plugin;
    private final File folder;
    private final Gson gson;

    public JsonDatabase(CidarisCraftPlugin plugin) {
        this.plugin = plugin;
        this.folder = new File(plugin.getDataFolder(), "players");

        if (!folder.exists()) {
            folder.mkdirs();
        }

        this.gson = new GsonBuilder()
                .setPrettyPrinting()
                .create();
    }

    @Override
    public PlayerData loadPlayer(UUID uuid) {
        PlayerData data = new PlayerData(uuid);
        File file = new File(folder, uuid.toString() + ".json");

        if (!file.exists()) {
            return data;
        }

        try (Reader reader = new FileReader(file)) {
            Type type = new TypeToken<Map<String, Map<String, String>>>(){}.getType();
            Map<String, Map<String, String>> jsonMap = gson.fromJson(reader, type);

            if (jsonMap != null && jsonMap.containsKey("unlocked_crafts")) {
                Map<String, String> crafts = jsonMap.get("unlocked_crafts");

                crafts.forEach((craftId, methodName) -> {
                    try {
                        UnlockMethod method = UnlockMethod.valueOf(methodName);
                        data.unlockCraft(craftId, method);
                    } catch (IllegalArgumentException e) {
                        plugin.getLogger().warning("Méthode de déblocage inconnue pour " + craftId);
                    }
                });
            }
        } catch (IOException e) {
            plugin.getLogger().severe("Erreur lors de la lecture du fichier JSON pour " + uuid);
        }

        return data;
    }

    @Override
    public void savePlayer(PlayerData data) {
        File file = new File(folder, data.getUuid().toString() + ".json");

        Map<String, Map<String, UnlockMethod>> wrapper = Map.of(
                "unlocked_crafts", data.getUnlockedCrafts()
        );

        try (Writer writer = new FileWriter(file)) {
            gson.toJson(wrapper, writer);
        } catch (IOException e) {
            plugin.getLogger().severe("Impossible de sauvegarder les données JSON de " + data.getUuid());
        }
    }
}