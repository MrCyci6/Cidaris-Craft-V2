package fr.cidaris.craft.gui.menus;

import fr.cidaris.craft.CidarisRecipePlugin;
import fr.cidaris.craft.keys.BlueprintKeys;
import fr.cidaris.craft.config.files.GuisConfig;
import fr.cidaris.craft.config.files.MessagesConfig;
import fr.cidaris.craft.gui.CidarisGui;
import fr.cidaris.craft.model.PlayerData;
import fr.cidaris.craft.utils.ConfigItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;

public class BlueprintGui implements CidarisGui {

    private final CidarisRecipePlugin plugin;
    private final Player player;
    private final Inventory inventory;
    private final ConfigurationSection config;

    private final int INPUT_SLOT;
    private final int BUTTON_SLOT;
    private final int OUTPUT_SLOT;
    private final int SIZE;

    public BlueprintGui(CidarisRecipePlugin plugin, Player player) {
        this.plugin = plugin;
        this.player = player;

        this.config = plugin.getConfigManager().getConfig(GuisConfig.class).get().getConfigurationSection("blueprint_menu");

        String title = ChatColor.translateAlternateColorCodes('&', config.getString("title", "&8» &dAnalyse de Blueprint"));
        this.SIZE = config.getInt("size", 27);
        this.INPUT_SLOT = config.getInt("input_slot", 11);
        this.OUTPUT_SLOT = config.getInt("output_slot", 15);

        ConfigurationSection buttonSec = config.getConfigurationSection("dynamic_items.search_button");
        this.BUTTON_SLOT = buttonSec != null ? buttonSec.getInt("slot", 13) : 13;

        this.inventory = Bukkit.createInventory(this, SIZE, title);
        setupItems();
    }

    private void setupItems() {
        ConfigurationSection staticItems = config.getConfigurationSection("items");
        if (staticItems != null) {
            for (String key : staticItems.getKeys(false)) {
                ConfigurationSection itemSec = staticItems.getConfigurationSection(key);
                ItemStack item = ConfigItemBuilder.fromConfig(plugin, itemSec);

                if (itemSec.contains("slot")) {
                    int slot = itemSec.getInt("slot");
                    if (slot >= 0 && slot < SIZE) {
                        inventory.setItem(slot, item);
                    }
                }

                if (itemSec.contains("slots")) {
                    for (int slot : itemSec.getIntegerList("slots")) {
                        if (slot >= 0 && slot < SIZE) {
                            inventory.setItem(slot, item);
                        }
                    }
                }
            }
        }

        ConfigurationSection buttonSec = config.getConfigurationSection("dynamic_items.search_button");
        if (buttonSec != null) {
            ItemStack button = ConfigItemBuilder.fromConfig(plugin, buttonSec);
            inventory.setItem(BUTTON_SLOT, button);
        }
    }

    @Override
    public Inventory getInventory() { return inventory; }

    @Override
    public void onClick(InventoryClickEvent event) {
        int slot = event.getRawSlot();

        if (slot >= SIZE || slot == INPUT_SLOT || slot == OUTPUT_SLOT) {
            event.setCancelled(false);
            return;
        }

        if (slot == BUTTON_SLOT) {
            event.setCancelled(true);
            ItemStack inputItem = inventory.getItem(INPUT_SLOT);

            if (inputItem == null || inputItem.getType() == Material.AIR) return;

            String state = plugin.getNbt().getString(inputItem, BlueprintKeys.BP_STATE);
            if (!"sealed".equals(state)) {
                player.sendMessage(plugin.getConfigManager().getConfig(MessagesConfig.class).getMessage("blueprint_not_sealed"));
                return;
            }

            if (inventory.getItem(OUTPUT_SLOT) != null) {
                player.sendMessage(plugin.getConfigManager().getConfig(MessagesConfig.class).getMessage("blueprint_output_full"));
                return;
            }

            PlayerData pData = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());
            ItemStack revealed = plugin.getBlueprintManager().revealBlueprint(inputItem, pData);

            if (revealed == null) {
                player.sendMessage(plugin.getConfigManager().getConfig(MessagesConfig.class).getMessage("blueprint_all_unlocked"));
                return;
            }

            if (inputItem.getAmount() > 1) {
                inputItem.setAmount(inputItem.getAmount() - 1);
            } else {
                inventory.setItem(INPUT_SLOT, null);
            }

            inventory.setItem(OUTPUT_SLOT, revealed);
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