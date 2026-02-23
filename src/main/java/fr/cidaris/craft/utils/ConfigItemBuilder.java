package fr.cidaris.craft.utils;

import fr.cidaris.craft.CidarisCraftPlugin;
import fr.cidaris.craft.hook.impl.HeadDatabaseHook;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConfigItemBuilder {

    // Méthode 1 : Sans placeholders (pour les items statiques comme les vitres ou flèches)
    public static ItemStack fromConfig(CidarisCraftPlugin plugin, ConfigurationSection section) {
        return fromConfig(plugin, section, new HashMap<>());
    }

    // Méthode 2 : Avec placeholders dynamiques
    public static ItemStack fromConfig(CidarisCraftPlugin plugin, ConfigurationSection section, Map<String, String> placeholders) {
        if (section == null) return new ItemStack(Material.PAPER);

        ItemStack item = null;

        // 1. Hook HeadDatabase
        if (section.contains("hdb")) {
            String hdbId = section.getString("hdb");
            HeadDatabaseHook hdbHook = plugin.getHookManager().getHook(HeadDatabaseHook.class);
            if (hdbHook != null && hdbHook.isHooked()) {
                item = hdbHook.getHead(hdbId);
            }
        }

        // 2. Fallback Vanilla
        if (item == null) {
            String matName = section.getString("material", "PAPER");
            Material mat = Material.getMaterial(matName.toUpperCase());
            if (mat == null) mat = Material.PAPER;

            short data = (short) section.getInt("data", 0);
            item = new ItemStack(mat, 1, data);
        }

        // 3. Application du Nom et du Lore avec remplacement des Placeholders
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {

            // Traitement du nom
            if (section.contains("name")) {
                String name = section.getString("name");
                for (Map.Entry<String, String> entry : placeholders.entrySet()) {
                    name = name.replace(entry.getKey(), entry.getValue());
                }
                meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
            }

            // Traitement du lore
            if (section.contains("lore")) {
                List<String> lore = new ArrayList<>();
                for (String line : section.getStringList("lore")) {
                    for (Map.Entry<String, String> entry : placeholders.entrySet()) {
                        line = line.replace(entry.getKey(), entry.getValue());
                    }
                    lore.add(ChatColor.translateAlternateColorCodes('&', line));
                }
                meta.setLore(lore);
            }

            // Traitement du glow (enchantement caché)
            if (section.getBoolean("glow", false)) {
                meta.addEnchant(org.bukkit.enchantments.Enchantment.DURABILITY, 1, true);
                meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
            }

            item.setItemMeta(meta);
        }

        return item;
    }
}