package fr.cidaris.craft.listener;

import fr.cidaris.craft.CidarisRecipePlugin;
import fr.cidaris.craft.keys.BlueprintKeys;
import fr.cidaris.craft.config.files.MessagesConfig;
import fr.cidaris.craft.model.CraftDefinition;
import fr.cidaris.craft.model.PlayerData;
import fr.cidaris.craft.model.enums.UnlockMethod;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class BlueprintInteractListener implements Listener {

    private final CidarisRecipePlugin plugin;

    public BlueprintInteractListener(CidarisRecipePlugin plugin) {
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
            player.sendMessage(plugin.getConfigManager().getConfig(MessagesConfig.class).getMessage("blueprint_already_known"));
            return;
        }

        if (craft.getCommands() != null) {
            for (String cmd : craft.getCommands()) {
                String formattedCmd = cmd.replace("%player%", player.getName()).replace("%craft%", craft.getId());
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), formattedCmd);
            }
        }

        pData.unlockCraft(craftId, UnlockMethod.BLUEPRINT);

        if (item.getAmount() > 1) {
            item.setAmount(item.getAmount() - 1);
        } else {
            player.setItemInHand(null);
        }

        player.sendMessage(plugin.getConfigManager().getConfig(MessagesConfig.class).getMessage("blueprint_success").replace("%craft%", craft.getName()));

        plugin.getWebhookManager().sendUnlockLog(player, craft.getName(), craft.getCommands());
    }
}