package fr.cidaris.craft.gui.menus;

import fr.cidaris.craft.CidarisCraftPlugin;
import fr.cidaris.craft.blueprint.BlueprintKeys;
import fr.cidaris.craft.gui.CidarisGui;
import fr.cidaris.craft.model.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;

public class BlueprintGui implements CidarisGui {

    private final CidarisCraftPlugin plugin;
    private final Player player;
    private final Inventory inventory;

    private final int INPUT_SLOT = 11;
    private final int BUTTON_SLOT = 13;
    private final int OUTPUT_SLOT = 15;

    public BlueprintGui(CidarisCraftPlugin plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        this.inventory = Bukkit.createInventory(this, 27, "§8» §dAnalyse de Blueprint");

        setupBackground();
    }

    private void setupBackground() {
        ItemStack glass = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 15);
        ItemMeta gMeta = glass.getItemMeta();
        gMeta.setDisplayName(" ");
        glass.setItemMeta(gMeta);

        for (int i = 0; i < 27; i++) {
            if (i != INPUT_SLOT && i != BUTTON_SLOT && i != OUTPUT_SLOT) {
                inventory.setItem(i, glass);
            }
        }

        ItemStack button = new ItemStack(Material.WOOL, 1, (short) 1);
        ItemMeta bMeta = button.getItemMeta();
        bMeta.setDisplayName("§a§lFouiller le Blueprint");
        button.setItemMeta(bMeta);
        inventory.setItem(BUTTON_SLOT, button);
    }

    @Override
    public Inventory getInventory() { return inventory; }

    @Override
    public void onClick(InventoryClickEvent event) {
        int slot = event.getRawSlot();

        if (slot >= 27 || slot == INPUT_SLOT || slot == OUTPUT_SLOT) {
            event.setCancelled(false);
            return;
        }

        if (slot == BUTTON_SLOT) {
            event.setCancelled(true);

            ItemStack inputItem = inventory.getItem(INPUT_SLOT);
            if (inputItem == null || inputItem.getType() == Material.AIR) return;

            String state = plugin.getNbt().getString(inputItem, BlueprintKeys.BP_STATE);
            if (!"sealed".equals(state)) {
                player.sendMessage("§cCet objet n'est pas un blueprint non fouillé.");
                return;
            }

            if (inventory.getItem(OUTPUT_SLOT) != null) {
                player.sendMessage("§cVeuillez récupérer le blueprint analysé d'abord !");
                return;
            }

            PlayerData pData = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());
            ItemStack revealed = plugin.getBlueprintManager().revealBlueprint(inputItem, pData);

            if (revealed == null) {
                player.sendMessage("§cVous avez déjà débloqué tous les crafts possibles !");
                return;
            }

            if (inputItem.getAmount() > 1) {
                inputItem.setAmount(inputItem.getAmount() - 1);
            } else {
                inventory.setItem(INPUT_SLOT, null);
            }

            inventory.setItem(OUTPUT_SLOT, revealed);
            player.sendMessage("§aAnalyse terminée avec succès !");
            return;
        }

        event.setCancelled(true);
    }

    @Override
    public void onClose(InventoryCloseEvent event) {
        int[] slotsToReturn = {INPUT_SLOT, OUTPUT_SLOT};
        for (int slot : slotsToReturn) {
            ItemStack item = inventory.getItem(slot);
            if (item != null && item.getType() != Material.AIR) {
                HashMap<Integer, ItemStack> leftOvers = player.getInventory().addItem(item);
                for (ItemStack drop : leftOvers.values()) {
                    player.getWorld().dropItem(player.getLocation(), drop);
                }
                inventory.setItem(slot, null);
            }
        }
    }
}