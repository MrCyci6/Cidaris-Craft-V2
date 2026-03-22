package fr.cidaris.craft.manager;

import fr.cidaris.craft.CidarisRecipePlugin;
import fr.cidaris.craft.config.files.MainConfig;
import fr.cidaris.craft.model.*;
import fr.cidaris.craft.model.enums.*;
import fr.cidaris.craft.utils.ConfigItemBuilder;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.*;

public class CraftManager {

    private final CidarisRecipePlugin plugin;
    private final Map<String, CraftDefinition> craftRegistry = new LinkedHashMap<>();
    private final Map<String, CraftDefinition> gridCrafts = new HashMap<>();
    private final Map<String, PathData> gridDecorations = new HashMap<>();

    private ItemStack lockedPathItem;
    private ItemStack unlockedPathItem;

    public CraftManager(CidarisRecipePlugin plugin) {
        this.plugin = plugin;
    }

    public void loadCrafts() {
        craftRegistry.clear();
        gridCrafts.clear();

        File file = new File(plugin.getDataFolder(), "crafts.yml");
        if (!file.exists()) {
            plugin.saveResource("crafts.yml", false);
        }
        FileConfiguration mainConfig = plugin.getConfigManager().getConfig(MainConfig.class).get();
        lockedPathItem = ConfigItemBuilder.fromConfig(plugin, mainConfig.getConfigurationSection("wiki_path_item"));
        unlockedPathItem = ConfigItemBuilder.fromConfig(plugin, mainConfig.getConfigurationSection("wiki_path_item_unlocked"));

        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection craftsSection = config.getConfigurationSection("crafts");

        if (craftsSection == null) return;

        for (String key : craftsSection.getKeys(false)) {
            try {
                ConfigurationSection section = craftsSection.getConfigurationSection(key);

                String name = section.getString("name");
                List<String> parents = section.getStringList("parents");

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

                List<String> commands = section.getStringList("commands");
                String rarity = section.getString("rarity", "commun");

                int guiX = section.getInt("gui.x", 0);
                int guiY = section.getInt("gui.y", 0);
                CraftDefinition craft = new CraftDefinition(key, name, parents, costs, commands, rarity, guiX, guiY);
                craftRegistry.put(key, craft);
                gridCrafts.put(craft.getGuiX() + ":" + craft.getGuiY(), craft);

            } catch (Exception e) {
                plugin.getLogger().severe("Erreur lors du chargement du craft : " + key);
                e.printStackTrace();
            }
        }
        generatePaths();

        plugin.getLogger().info(craftRegistry.size() + " crafts chargés avec succès !");
    }

    private void generatePaths() {
        gridDecorations.clear();
        ItemStack pathItem = new ItemStack(Material.STICK);

        for (CraftDefinition craft : craftRegistry.values()) {
            if (craft.getParents() == null || craft.getParents().isEmpty()) {
                drawPath(0, 0, craft.getGuiX(), craft.getGuiY(), craft.getId());
                continue;
            }
            for (String parentId : craft.getParents()) {
                CraftDefinition parent = craftRegistry.get(parentId);
                if (parent != null) {
                    drawPath(parent.getGuiX(), parent.getGuiY(), craft.getGuiX(), craft.getGuiY(), craft.getId());
                }
            }
        }
    }

    private void drawPath(int x1, int y1, int x2, int y2, String targetCraftId) {
        int stepX = Integer.compare(x2, x1);
        for (int x = x1 + stepX; x != x2; x += stepX) {
            addDecoration(x, y1, targetCraftId);
        }

        if (x1 != x2 && y1 != y2) {
            addDecoration(x2, y1, targetCraftId);
        }

        int stepY = Integer.compare(y2, y1);
        for (int y = y1 + stepY; y != y2; y += stepY) {
            addDecoration(x2, y, targetCraftId);
        }
    }

    private void addDecoration(int x, int y, String targetCraftId) {
        String key = x + ":" + y;
        if (!gridCrafts.containsKey(key)) {
            gridDecorations.computeIfAbsent(key, k -> new PathData()).addTarget(targetCraftId);        }
    }

    public CraftDefinition getCraftAt(int x, int y) {
        return gridCrafts.get(x + ":" + y);
    }

    public PathData getPathAt(int x, int y) {
        return gridDecorations.get(x + ":" + y);
    }

    public CraftDefinition getCraft(String id) {
        return craftRegistry.get(id);
    }

    public Collection<CraftDefinition> getAllCrafts() {
        return craftRegistry.values();
    }

    public ItemStack getLockedPathItem() {
        return lockedPathItem;
    }

    public ItemStack getUnlockedPathItem() {
        return unlockedPathItem;
    }
}