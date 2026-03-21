package fr.cidaris.craft.manager;

import fr.cidaris.craft.CidarisRecipePlugin;
import fr.cidaris.craft.hook.impl.VaultHook;
import fr.cidaris.craft.keys.CidarisItemKeys;
import fr.cidaris.craft.model.Cost;
import fr.cidaris.craft.model.enums.CostType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class EconomyManager {

    private final CidarisRecipePlugin plugin;

    public EconomyManager(CidarisRecipePlugin plugin) {
        this.plugin = plugin;
    }

    public boolean canAfford(Player player, List<Cost> costs) {
        if (costs == null || costs.isEmpty()) return true;

        for (Cost cost : costs) {
            if (cost.getType() == CostType.MONEY) {
                VaultHook vault = plugin.getHookManager().getHook(VaultHook.class);
                if (vault == null || !vault.isHooked() || !vault.getEconomy().has(player, cost.getAmount())) {
                    return false;
                }
            } else if (cost.getType() == CostType.VANILLA) {
                if (!hasItem(player, cost.getId(), false, (int) cost.getAmount())) return false;
            } else if (cost.getType() == CostType.CUSTOM_ITEM) {
                if (!hasItem(player, cost.getId(), true, (int) cost.getAmount())) return false;
            }
        }
        return true;
    }

    public void pay(Player player, List<Cost> costs) {
        if (costs == null || costs.isEmpty()) return;

        for (Cost cost : costs) {
            VaultHook vault = plugin.getHookManager().getHook(VaultHook.class);
            if (cost.getType() == CostType.MONEY && vault.getEconomy() != null) {
                vault.getEconomy().withdrawPlayer(player, cost.getAmount());
            } else if (cost.getType() == CostType.VANILLA) {
                consumeItem(player, cost.getId(), false, (int) cost.getAmount());
            } else if (cost.getType() == CostType.CUSTOM_ITEM) {
                consumeItem(player, cost.getId(), true, (int) cost.getAmount());
            }
        }
    }

    private boolean hasItem(Player p, String id, boolean custom, int requiredAmount) {
        int count = 0;
        for (ItemStack item : p.getInventory().getContents()) {
            if (item == null) continue;

            boolean match = false;
            if (custom) {
                String customId = plugin.getNbt().getString(item, CidarisItemKeys.CUSTOM_ID);
                match = id.equals(customId);
            } else {
                match = item.getType().name().equals(id);
            }

            if (match) {
                count += item.getAmount();
            }
        }
        return count >= requiredAmount;
    }

    private void consumeItem(Player p, String id, boolean custom, int amountToRemove) {
        int toRemove = amountToRemove;
        ItemStack[] contents = p.getInventory().getContents();

        for (int i = 0; i < contents.length; i++) {
            ItemStack item = contents[i];
            if (item == null) continue;

            boolean match = false;
            if (custom) {
                String customId = plugin.getNbt().getString(item, CidarisItemKeys.CUSTOM_ID);
                match = id.equals(customId);
            } else {
                match = item.getType().name().equals(id);
            }

            if (match) {
                if (item.getAmount() <= toRemove) {
                    toRemove -= item.getAmount();
                    p.getInventory().setItem(i, null);
                } else {
                    item.setAmount(item.getAmount() - toRemove);
                    toRemove = 0;
                }
            }
            if (toRemove <= 0) break;
        }
        p.updateInventory();
    }
}