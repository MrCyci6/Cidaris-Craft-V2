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
            List<CraftDefinition> available = new ArrayList<>();
            for (CraftDefinition craft : plugin.getCraftManager().getAllCrafts()) {
                if (!playerData.hasUnlocked(craft.getId())) {
                    available.add(craft);
                }
            }

            if (available.isEmpty()) {
                return null;
            }

            CraftDefinition picked = available.get(random.nextInt(available.size()));
            craftId = picked.getId();
        }

        CraftDefinition resultCraft = plugin.getCraftManager().getCraft(craftId);
        if (resultCraft == null) return null;


        ConfigurationSection bpConfig = plugin.getConfigManager().getConfig(MainConfig.class).get().getConfigurationSection("blueprint_revealed");
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("%craft_name%", resultCraft.getName());
        ItemStack revealed = ConfigItemBuilder.fromConfig(plugin, bpConfig, placeholders);

        revealed = plugin.getNbt().setString(revealed, BlueprintKeys.BP_STATE, "revealed");
        revealed = plugin.getNbt().setString(revealed, BlueprintKeys.BP_CRAFT, craftId);
        return revealed;
    }
}