package fr.cidaris.craft.model;

import fr.cidaris.craft.manager.CraftManager;
import fr.cidaris.craft.model.enums.UnlockMethod;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerData {
    private final UUID uuid;
    private final Map<String, UnlockMethod> unlockedCrafts;

    public PlayerData(UUID uuid) {
        this.uuid = uuid;
        this.unlockedCrafts = new HashMap<>();
    }

    public UUID getUuid() { return uuid; }

    public Map<String, UnlockMethod> getUnlockedCrafts() { return unlockedCrafts; }

    public boolean hasUnlocked(String craftId) {
        return unlockedCrafts.containsKey(craftId);
    }

    public UnlockMethod getUnlockMethod(String craftId) {
        return unlockedCrafts.get(craftId);
    }

    public void unlockCraft(String craftId, UnlockMethod method) {
        unlockedCrafts.put(craftId, method);
    }


    public boolean canBuyNormally(CraftDefinition craft, CraftManager craftManager) {
        if (craft.getParents() == null || craft.getParents().isEmpty()) {
            return true;
        }

        for (String parentId : craft.getParents()) {
            if (!hasUnlocked(parentId)) {
                return false;
            }

            if (getUnlockMethod(parentId) != UnlockMethod.NORMAL) {
                CraftDefinition parentCraft = craftManager.getCraft(parentId);
                if (parentCraft != null && !canBuyNormally(parentCraft, craftManager)) {
                    return false;
                }
            }
        }

        return true;
    }
}