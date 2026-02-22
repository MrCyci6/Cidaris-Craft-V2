package fr.cidaris.craft.manager;

import fr.cidaris.craft.CidarisCraftPlugin;
import fr.cidaris.craft.model.*;
import fr.cidaris.craft.model.enums.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.*;

public class CraftManager {

    private final CidarisCraftPlugin plugin;
    private final Map<String, CraftDefinition> craftRegistry = new LinkedHashMap<>(); // Préserve l'ordre

    public CraftManager(CidarisCraftPlugin plugin) {
        this.plugin = plugin;
    }

    public void loadCrafts() {
        craftRegistry.clear();

        File file = new File(plugin.getDataFolder(), "crafts.yml");
        if (!file.exists()) {
            plugin.saveResource("crafts.yml", false);
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection craftsSection = config.getConfigurationSection("crafts");

        if (craftsSection == null) return;

        for (String key : craftsSection.getKeys(false)) {
            try {
                ConfigurationSection section = craftsSection.getConfigurationSection(key);

                String name = section.getString("name");
                List<String> parents = section.getStringList("parents");
                String consoleCommand = section.getString("console-command", "");

                List<Cost> costs = new ArrayList<>();
                if (section.contains("costs")) {
                    for (Map<?, ?> costMap : section.getMapList("costs")) {
                        CostType cType = CostType.valueOf(costMap.get("type").toString());
                        double amount = Double.parseDouble(costMap.get("amount").toString());
                        String id = costMap.containsKey("id") ? costMap.get("id").toString() : null;
                        if (cType == CostType.VANILLA && costMap.containsKey("material")) {
                            id = costMap.get("material").toString();
                        }
                        costs.add(new Cost(cType, amount, id));
                    }
                }

                ConfigurationSection recipeSec = section.getConfigurationSection("recipe");
                List<String> shape = recipeSec.getStringList("shape");

                Map<Character, Ingredient> ingredients = new HashMap<>();
                ConfigurationSection ingSec = recipeSec.getConfigurationSection("ingredients");
                for (String charKey : ingSec.getKeys(false)) {
                    ConfigurationSection iData = ingSec.getConfigurationSection(charKey);
                    IngredientType iType = IngredientType.valueOf(iData.getString("type"));
                    String iId = iType == IngredientType.VANILLA ? iData.getString("material") : iData.getString("id");
                    ingredients.put(charKey.charAt(0), new Ingredient(iType, iId));
                }

                ConfigurationSection resultSec = recipeSec.getConfigurationSection("result");
                ResultMode rMode = ResultMode.valueOf(resultSec.getString("mode"));
                int rAmount = resultSec.getInt("amount", 1);
                List<String> rCommands = resultSec.getStringList("commands");
                CraftResult result = new CraftResult(rMode, rAmount, rCommands);

                int guiColumn = section.getInt("gui.column", 0);
                int guiTier = section.getInt("gui.tier", 0);
                CraftDefinition craft = new CraftDefinition(key, name, parents, costs, consoleCommand, shape, ingredients, result, guiColumn, guiTier);
                craftRegistry.put(key, craft);

            } catch (Exception e) {
                plugin.getLogger().severe("Erreur lors du chargement du craft : " + key);
                e.printStackTrace();
            }
        }

        plugin.getLogger().info(craftRegistry.size() + " crafts chargés avec succès !");
    }

    public CraftDefinition getCraft(String id) {
        return craftRegistry.get(id);
    }

    public Collection<CraftDefinition> getAllCrafts() {
        return craftRegistry.values();
    }
}