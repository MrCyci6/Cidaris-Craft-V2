package fr.cidaris.craft.manager;

import fr.cidaris.craft.database.DatabaseProvider;
import fr.cidaris.craft.model.PlayerData;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerDataManager {

    private final DatabaseProvider database;
    private final Map<UUID, PlayerData> cache = new HashMap<>();

    public PlayerDataManager(DatabaseProvider database) {
        this.database = database;
    }

    public void loadIntoCache(UUID uuid) {
        PlayerData data = database.loadPlayer(uuid);
        cache.put(uuid, data);
    }

    public void saveAndRemoveFromCache(UUID uuid) {
        PlayerData data = cache.remove(uuid);
        if (data != null) {
            database.savePlayer(data);
        }
    }

    public PlayerData getPlayerData(UUID uuid) {
        return cache.get(uuid);
    }

    public void saveAll() {
        for (PlayerData data : cache.values()) {
            database.savePlayer(data);
        }
    }
}