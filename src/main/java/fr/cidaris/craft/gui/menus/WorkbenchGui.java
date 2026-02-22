package fr.cidaris.craft.gui.menus;

import fr.cidaris.craft.CidarisCraftPlugin;
import fr.cidaris.craft.gui.CidarisGui;
import fr.cidaris.craft.model.CraftDefinition;
import fr.cidaris.craft.model.Ingredient;
import fr.cidaris.craft.model.PlayerData;
import fr.cidaris.craft.model.enums.IngredientType;
import fr.cidaris.craft.model.enums.ResultMode;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class WorkbenchGui implements CidarisGui {

    private final CidarisCraftPlugin plugin;
    private final Player player;
    private final PlayerData playerData;
    private final Inventory inventory;

    private final int[] CRAFT_SLOTS = {10, 11, 12, 19, 20, 21, 28, 29, 30};
    private final int RESULT_SLOT = 24;

    private CraftDefinition currentRecipe = null;

    public WorkbenchGui(CidarisCraftPlugin plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        this.playerData = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());

        this.inventory = Bukkit.createInventory(this, 54, "§8» §3Workbench");
        setupBackground();
    }

    private void setupBackground() {
        ItemStack glass = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 15);
        ItemMeta meta = glass.getItemMeta();
        meta.setDisplayName(" ");
        glass.setItemMeta(meta);

        for (int i = 0; i < 54; i++) {
            inventory.setItem(i, glass);
        }

        for (int slot : CRAFT_SLOTS) {
            inventory.setItem(slot, null);
        }
        inventory.setItem(RESULT_SLOT, null);
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    @Override
    public void onClose(InventoryCloseEvent event) {
        for (int slot : CRAFT_SLOTS) {
            ItemStack item = inventory.getItem(slot);

            if (item != null && item.getType() != Material.AIR) {
                java.util.HashMap<Integer, ItemStack> leftOvers = player.getInventory().addItem(item);

                for (ItemStack drop : leftOvers.values()) {
                    player.getWorld().dropItem(player.getLocation(), drop);
                }

                inventory.setItem(slot, null);
            }
        }
    }

    @Override
    public void onClick(InventoryClickEvent event) {
        int slot = event.getRawSlot();

        if (slot >= 54 || isCraftSlot(slot)) {
            event.setCancelled(false);

            Bukkit.getScheduler().runTaskLater(plugin, this::checkRecipe, 1L);
            return;
        }

        if (slot == RESULT_SLOT) {
            event.setCancelled(true);
            if (currentRecipe != null && playerData.hasUnlocked(currentRecipe.getId())) {
                craftItem();
            }
            return;
        }

        event.setCancelled(true);
    }

    private boolean isCraftSlot(int slot) {
        for (int s : CRAFT_SLOTS) {
            if (s == slot) return true;
        }
        return false;
    }

    private void checkRecipe() {
        ItemStack[] grid = new ItemStack[9];
        for (int i = 0; i < 9; i++) {
            grid[i] = inventory.getItem(CRAFT_SLOTS[i]);
        }

        currentRecipe = null;
        for (CraftDefinition craft : plugin.getCraftManager().getAllCrafts()) {
            if (matches(craft, grid)) {
                currentRecipe = craft;
                break;
            }
        }

        if (currentRecipe != null) {
            if (playerData.hasUnlocked(currentRecipe.getId())) {
                ItemStack resultIcon = new ItemStack(Material.WORKBENCH);
                ItemMeta meta = resultIcon.getItemMeta();
                meta.setDisplayName("§aFabriquer : §e" + currentRecipe.getName());
                meta.setLore(Arrays.asList("§7Cliquez pour crafter !"));
                resultIcon.setItemMeta(meta);
                inventory.setItem(RESULT_SLOT, resultIcon);
            } else {
                ItemStack locked = new ItemStack(Material.BARRIER);
                ItemMeta meta = locked.getItemMeta();
                meta.setDisplayName("§c§lCraft Verrouillé");
                meta.setLore(Arrays.asList("§7Vous devez débloquer", "§7ce craft dans le Wiki."));
                locked.setItemMeta(meta);
                inventory.setItem(RESULT_SLOT, locked);
            }
        } else {
            inventory.setItem(RESULT_SLOT, null);
        }
    }

    private boolean matches(CraftDefinition craft, ItemStack[] grid) {
        List<String> shape = craft.getShape();
        Map<Character, Ingredient> ingredients = craft.getIngredients();

        for (int i = 0; i < 9; i++) {
            int row = i / 3;
            int col = i % 3;
            char c = shape.get(row).charAt(col);
            ItemStack item = grid[i];

            if (c == ' ') {
                if (item != null && item.getType() != Material.AIR) return false;
            } else {
                Ingredient req = ingredients.get(c);
                if (req == null) return false;
                if (item == null || item.getType() == Material.AIR) return false;

                if (req.getType() == IngredientType.VANILLA) {
                    if (!item.getType().name().equals(req.getId())) return false;
                    if (plugin.getNbt().getString(item, "cidaris-item-id") != null) return false;
                } else if (req.getType() == IngredientType.CUSTOM_ITEM) {
                    String customId = plugin.getNbt().getString(item, "cidaris-item-id");
                    if (customId == null || !customId.equals(req.getId())) return false;
                }
            }
        }
        return true;
    }

    private void craftItem() {
        for (int slot : CRAFT_SLOTS) {
            ItemStack item = inventory.getItem(slot);
            if (item != null && item.getType() != Material.AIR) {
                if (item.getAmount() > 1) {
                    item.setAmount(item.getAmount() - 1);
                } else {
                    inventory.setItem(slot, null);
                }
            }
        }

        if (currentRecipe.getResult().getMode() == ResultMode.COMMAND) {
            for (String cmd : currentRecipe.getResult().getCommands()) {
                String finalCmd = cmd.replace("%player%", player.getName());
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), finalCmd);
            }
        }

        checkRecipe();
    }
}