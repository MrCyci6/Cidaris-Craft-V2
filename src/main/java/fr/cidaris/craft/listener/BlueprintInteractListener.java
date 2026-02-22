package fr.cidaris.craft.listener;

import fr.cidaris.craft.CidarisCraftPlugin;
import fr.cidaris.craft.blueprint.BlueprintKeys;
import fr.cidaris.craft.model.CraftDefinition;
import fr.cidaris.craft.model.PlayerData;
import fr.cidaris.craft.model.enums.UnlockMethod;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class BlueprintInteractListener implements Listener {

    private final CidarisCraftPlugin plugin;

    public BlueprintInteractListener(CidarisCraftPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Player player = event.getPlayer();
        ItemStack item = player.getItemInHand();

        if (item == null || item.getType() == Material.AIR) return;

        String state = plugin.getNbt().getString(item, BlueprintKeys.BP_STATE);
        if (!"revealed".equals(state)) return;

        event.setCancelled(true);
        String craftId = plugin.getNbt().getString(item, BlueprintKeys.BP_CRAFT);
        CraftDefinition craft = plugin.getCraftManager().getCraft(craftId);

        if (craft == null) return;

        PlayerData pData = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());

        if (pData.hasUnlocked(craftId)) {
            player.sendMessage("§cVous connaissez déjà ce craft ! Vous pouvez l'échanger à un autre joueur.");
            return;
        }

        pData.unlockCraft(craftId, UnlockMethod.BLUEPRINT);

        if (item.getAmount() > 1) {
            item.setAmount(item.getAmount() - 1);
        } else {
            player.setItemInHand(null);
        }

        player.sendMessage("§dVous avez lu le plan et débloqué : §e" + craft.getName() + " §d!");
    }
}