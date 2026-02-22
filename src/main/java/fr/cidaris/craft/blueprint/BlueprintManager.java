package fr.cidaris.craft.blueprint;

import fr.cidaris.craft.CidarisCraftPlugin;
import fr.cidaris.craft.model.CraftDefinition;
import fr.cidaris.craft.model.PlayerData;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class BlueprintManager {

    private final CidarisCraftPlugin plugin;
    private final Random random = new Random();

    public BlueprintManager(CidarisCraftPlugin plugin) {
        this.plugin = plugin;
    }

    public ItemStack createSealedSpecific(String craftId) {
        ItemStack item = new ItemStack(Material.PAPER);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§b§lBlueprint Mystère §7(Spécifique)");
        meta.setLore(Arrays.asList("§7Cet objet contient un plan précis.", "§7Utilisez la table d'analyse pour le révéler."));
        item.setItemMeta(meta);

        item = plugin.getNbt().setString(item, BlueprintKeys.BP_STATE, "sealed");
        item = plugin.getNbt().setString(item, BlueprintKeys.BP_TYPE, "specific");
        item = plugin.getNbt().setString(item, BlueprintKeys.BP_CRAFT, craftId);
        return item;
    }

    public ItemStack createSealedRandom() {
        ItemStack item = new ItemStack(Material.PAPER);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§d§lBlueprint Mystère §7(Aléatoire)");
        meta.setLore(Arrays.asList("§7Cet objet contient un plan aléatoire.", "§7Utilisez la table d'analyse pour le révéler."));
        item.setItemMeta(meta);

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

        ItemStack revealed = new ItemStack(Material.EMPTY_MAP);
        ItemMeta meta = revealed.getItemMeta();
        meta.setDisplayName("§a§lPlan : §e" + resultCraft.getName());
        meta.setLore(Arrays.asList(
                "§7Vous avez analysé ce plan.",
                "",
                "§a► Clic-Droit pour débloquer",
                "§a  sans payer de ressources !"
        ));
        revealed.setItemMeta(meta);

        revealed = plugin.getNbt().setString(revealed, BlueprintKeys.BP_STATE, "revealed");
        revealed = plugin.getNbt().setString(revealed, BlueprintKeys.BP_CRAFT, craftId);
        return revealed;
    }
}