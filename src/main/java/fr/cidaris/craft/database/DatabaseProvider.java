package fr.cidaris.craft.database;

import fr.cidaris.craft.model.PlayerData;
import java.util.UUID;

public interface DatabaseProvider {
    PlayerData loadPlayer(UUID uuid);
    void savePlayer(PlayerData data);
}