package fr.cidaris.craft.manager;

import fr.cidaris.craft.CidarisRecipePlugin;
import fr.cidaris.craft.keys.BlueprintKeys;
import fr.cidaris.craft.config.files.MainConfig;
import fr.cidaris.craft.model.CraftDefinition;
import fr.cidaris.craft.model.PlayerData;
import fr.cidaris.craft.utils.ConfigItemBuilder;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class BlueprintManager {

    private final CidarisRecipePlugin plugin;
    private final Random random = new Random();

    public BlueprintManager(CidarisRecipePlugin plugin) {
        this.plugin = plugin;
    }

    public ItemStack createSealedSpecific(String craftId) {
        ConfigurationSection bpConfig = plugin.getConfigManager().getConfig(MainConfig.class).get().getConfigurationSection("blueprint");
        ItemStack item = ConfigItemBuilder.fromConfig(plugin, bpConfig);

        item = plugin.getNbt().setString(item, BlueprintKeys.BP_STATE, "sealed");
        item = plugin.getNbt().setString(item, BlueprintKeys.BP_TYPE, "specific");
        item = plugin.getNbt().setString(item, BlueprintKeys.BP_CRAFT, craftId);
        return item;
    }

    public ItemStack createSealedRandom() {
        ConfigurationSection bpConfig = plugin.getConfigManager().getConfig(MainConfig.class).get().getConfigurationSection("blueprint");
        ItemStack item = ConfigItemBuilder.fromConfig(plugin, bpConfig);

        item = plugin.getNbt().setString(item, BlueprintKeys.BP_STATE, "sealed");
        item = plugin.getNbt().setString(item, BlueprintKeys.BP_TYPE, "random_all");
        return item;
    }

    public ItemStack revealBlueprint(ItemStack sealed, PlayerData playerData) {
        String type = plugin.getNbt().getString(sealed, BlueprintKeys.BP_TYPE);
        String craftId = plugin.getNbt().getString(sealed, BlueprintKeys.BP_CRAFT);

        if ("random_all".equals(type)) {
            List<CraftDefinition> available = (List<CraftDefinition>) plugin.getCraftManager().getAllCrafts();
            /*for (CraftDefinition craft : plugin.getCraftManager().getAllCrafts()) {
                if (!playerData.hasUnlocked(craft.getId())) {
                    available.add(craft);
                }
            }*/

            if (available.isEmpty()) {
                return null;
            }

            Map<String, List<CraftDefinition>> craftsByRarity = new HashMap<>();
            for (CraftDefinition craft : available) {
                String rarity = craft.getRarity();
                craftsByRarity.computeIfAbsent(rarity, k -> new ArrayList<>()).add(craft);
            }

            ConfigurationSection rarityWeights = plugin.getConfigManager().getConfig(MainConfig.class).get().getConfigurationSection("blueprint_rarities");
            double totalWeight = 0.0;
            Map<String, Double> activeWeights = new HashMap<>();

            for (String rarity : craftsByRarity.keySet()) {
                double weight = (rarityWeights != null && rarityWeights.contains(rarity)) ? rarityWeights.getDouble(rarity) : 0.0;
                activeWeights.put(rarity, weight);
                totalWeight += weight;
            }

            double randomValue = random.nextDouble() * totalWeight;
            double currentWeight = 0.0;
            String selectedRarity = null;

            for (Map.Entry<String, Double> entry : activeWeights.entrySet()) {
                currentWeight += entry.getValue();
                if (randomValue <= currentWeight) {
                    selectedRarity = entry.getKey();
                    break;
                }
            }

            if (selectedRarity == null) {
                selectedRarity = activeWeights.keySet().iterator().next();
            }

            List<CraftDefinition> pool = craftsByRarity.get(selectedRarity);
            CraftDefinition picked = pool.get(random.nextInt(pool.size()));
            craftId = picked.getId();
        }

        CraftDefinition resultCraft = plugin.getCraftManager().getCraft(craftId);
        if (resultCraft == null) return null;


        ConfigurationSection bpConfig = plugin.getConfigManager().getConfig(MainConfig.class).get().getConfigurationSection("blueprint_revealed");
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("%craft_name%", resultCraft.getName());
        placeholders.put("%rarity%", resultCraft.getRarity());
        ItemStack revealed = ConfigItemBuilder.fromConfig(plugin, bpConfig, placeholders);

        revealed = plugin.getNbt().setString(revealed, BlueprintKeys.BP_STATE, "revealed");
        revealed = plugin.getNbt().setString(revealed, BlueprintKeys.BP_CRAFT, craftId);
        return revealed;
    }
}