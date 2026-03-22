package fr.cidaris.craft.gui.menus;

import fr.cidaris.craft.CidarisRecipePlugin;
import fr.cidaris.craft.config.files.GuisConfig;
import fr.cidaris.craft.config.files.MainConfig;
import fr.cidaris.craft.config.files.MessagesConfig;
import fr.cidaris.craft.gui.CidarisGui;
import fr.cidaris.craft.hook.impl.HeadDatabaseHook;
import fr.cidaris.craft.keys.CidarisItemKeys;
import fr.cidaris.craft.model.Cost;
import fr.cidaris.craft.model.CraftDefinition;
import fr.cidaris.craft.model.PathData;
import fr.cidaris.craft.model.PlayerData;
import fr.cidaris.craft.model.enums.UnlockMethod;
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
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class WikiGui implements CidarisGui {

    private final CidarisRecipePlugin plugin;
    private final Player player;
    private final PlayerData playerData;

    private final int camX;
    private final int camY;
    private final Inventory inventory;

    private final ConfigurationSection config;

    private final int SIZE, UP_SLOT, DOWN_SLOT, LEFT_SLOT, RIGHT_SLOT;

    public WikiGui(CidarisRecipePlugin plugin, Player player, int camX, int camY) {
        this.plugin = plugin;
        this.player = player;
        this.playerData = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());
        this.camX = camX;
        this.camY = camY;

        this.config = plugin.getConfigManager().getConfig(GuisConfig.class).get().getConfigurationSection("wiki_tree");
        this.SIZE = config.getInt("size", 54);

        ConfigurationSection paginSec = config.getConfigurationSection("pagination");
        this.UP_SLOT = paginSec.getInt("up_slot", 45);
        this.DOWN_SLOT = paginSec.getInt("down_slot", 53);
        this.RIGHT_SLOT = paginSec.getInt("right_slot", 51);
        this.LEFT_SLOT = paginSec.getInt("left_slot", 47);

        String title = ChatColor.translateAlternateColorCodes('&', config.getString("title", "Arbre"));
        this.inventory = Bukkit.createInventory(this, SIZE, title);
        setupItems();
    }

    private void setupItems() {
        inventory.clear();

        ConfigurationSection paginSec = config.getConfigurationSection("pagination");
        ItemStack bgItem = ConfigItemBuilder.fromConfig(plugin, config.getConfigurationSection("background"));
        ItemStack originItem = ConfigItemBuilder.fromConfig(plugin, plugin.getConfigManager().getConfig(MainConfig.class).get().getConfigurationSection("wiki_origin_item"));

        ItemMeta originMeta = originItem.getItemMeta();
        if (originMeta != null) {
            originMeta.addEnchant(org.bukkit.enchantments.Enchantment.DURABILITY, 1, true);
            originMeta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
            originItem.setItemMeta(originMeta);
        }

        for (int i = 0; i < SIZE; i++) {

            if (i == UP_SLOT) { inventory.setItem(i, ConfigItemBuilder.fromConfig(plugin, paginSec.getConfigurationSection("up_item"))); continue; }
            if (i == DOWN_SLOT) { inventory.setItem(i, ConfigItemBuilder.fromConfig(plugin, paginSec.getConfigurationSection("down_item"))); continue; }
            if (i == LEFT_SLOT) { inventory.setItem(i, ConfigItemBuilder.fromConfig(plugin, paginSec.getConfigurationSection("left_item"))); continue; }
            if (i == RIGHT_SLOT) { inventory.setItem(i, ConfigItemBuilder.fromConfig(plugin, paginSec.getConfigurationSection("right_item"))); continue; }

            int invX = i % 9;
            int invY = i / 9;

            int targetX = camX + (invX - 4);
            int targetY = camY + (invY - 2);

            if (targetX == 0 && targetY == 0) {
                inventory.setItem(i, originItem);
            } else if (plugin.getCraftManager().getCraftAt(targetX, targetY) != null) {
                CraftDefinition craft = plugin.getCraftManager().getCraftAt(targetX, targetY);
                inventory.setItem(i, buildCraftIcon(craft));
            } else if (plugin.getCraftManager().getPathAt(targetX, targetY) != null) {

                PathData pathData = plugin.getCraftManager().getPathAt(targetX, targetY);
                boolean isPathUnlocked = false;

                for (String targetId : pathData.getTargets()) {
                    if (playerData.hasUnlocked(targetId) || player.hasPermission("cidaris.bypass")) {
                        isPathUnlocked = true;
                        break;
                    }
                }

                if (isPathUnlocked) {
                    inventory.setItem(i, plugin.getCraftManager().getUnlockedPathItem());
                } else {
                    inventory.setItem(i, plugin.getCraftManager().getLockedPathItem());
                }
            } else {
                inventory.setItem(i, bgItem);
            }
        }
    }

    private ItemStack buildCraftIcon(CraftDefinition craft) {
        ConfigurationSection mainConfig = plugin.getConfigManager().getConfig(MainConfig.class).get();
        HeadDatabaseHook hdb = plugin.getHookManager().getHook(HeadDatabaseHook.class);

        String statusKey;
        String headId;

        if (playerData.hasUnlocked(craft.getId()) || player.hasPermission("cidaris.bypass")) {
            statusKey = "unlocked";
            headId = mainConfig.getString("wiki-heads.unlocked");
        } else if (!playerData.canBuyNormally(craft, plugin.getCraftManager())) {
            statusKey = "locked_parents";
            headId = mainConfig.getString("wiki-heads.locked-parents");
        } else if (!plugin.getEconomyManager().canAfford(player, craft.getCosts())) {
            statusKey = "locked_resources";
            headId = mainConfig.getString("wiki-heads.locked-resources");
        } else {
            statusKey = "unlockable";
            headId = mainConfig.getString("wiki-heads.unlockable");
        }

        ItemStack icon = (hdb != null && hdb.isHooked() && headId != null) ? hdb.getHead(headId) : new ItemStack(Material.PAPER);
        if (icon == null) icon = new ItemStack(Material.PAPER);

        ItemMeta meta = icon.getItemMeta();

        String prefix = config.getString("statuses." + statusKey + ".prefix", "");
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', prefix + " &8- &f" + craft.getName()));

        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.translateAlternateColorCodes('&', config.getString("craft_format.lore_header")));

        if (craft.getCosts().isEmpty()) {
            lore.add(ChatColor.translateAlternateColorCodes('&', config.getString("craft_format.lore_free")));
        } else {
            for (Cost c : craft.getCosts()) {
                String costLine = config.getString("craft_format.lore_cost")
                        .replace("%amount%", String.valueOf(c.getAmount()))
                        .replace("%item%", c.getId() != null ? c.getId() : "Monnaie");
                lore.add(ChatColor.translateAlternateColorCodes('&', costLine));
            }
        }

        lore.add(ChatColor.translateAlternateColorCodes('&', config.getString("craft_format.lore_footer")));

        for (String appendLine : config.getStringList("statuses." + statusKey + ".lore_append")) {
            if (statusKey.equals("unlocked")) {
                String methodStr = playerData.hasUnlocked(craft.getId()) ? playerData.getUnlockMethod(craft.getId()).name() : "BYPASS ADMIN";
                appendLine = appendLine.replace("%method%", methodStr);
            }
            lore.add(ChatColor.translateAlternateColorCodes('&', appendLine));
        }

        meta.setLore(lore);
        icon.setItemMeta(meta);
        icon = plugin.getNbt().setString(icon, "gui-craft-id", craft.getId());

        return icon;
    }

    @Override
    public void onClick(InventoryClickEvent event) {
        event.setCancelled(true);
        int slot = event.getRawSlot();
        if (slot >= SIZE) return;

        if (slot == UP_SLOT) { new WikiGui(plugin, player, camX, camY - 1).open(); return; }
        else if (slot == DOWN_SLOT) { new WikiGui(plugin, player, camX, camY + 1).open(); return; }
        else if (slot == LEFT_SLOT) { new WikiGui(plugin, player, camX - 1, camY).open(); return; }
        else if (slot == RIGHT_SLOT) { new WikiGui(plugin, player, camX + 1, camY).open(); return; }

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;

        String craftId = plugin.getNbt().getString(clicked, "gui-craft-id");
        if (craftId == null) return;

        CraftDefinition craft = plugin.getCraftManager().getCraft(craftId);
        if (craft == null) return;

        if (playerData.hasUnlocked(craftId)) {
            player.sendMessage(plugin.getConfigManager().getConfig(MessagesConfig.class).getMessage("already_unlocked"));
            return;
        }

        if (!playerData.canBuyNormally(craft, plugin.getCraftManager())) {
            player.sendMessage(plugin.getConfigManager().getConfig(MessagesConfig.class).getMessage("unlock_error_parents"));
            return;
        }

        if (!plugin.getEconomyManager().canAfford(player, craft.getCosts())) {
            player.sendMessage(plugin.getConfigManager().getConfig(MessagesConfig.class).getMessage("unlock_error_funds"));
            return;
        }

        plugin.getEconomyManager().pay(player, craft.getCosts());

        if (craft.getCommands() != null) {
            for (String cmd : craft.getCommands()) {
                String formattedCmd = cmd.replace("%player%", player.getName()).replace("%craft%", craft.getId());
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), formattedCmd);
            }
        }

        playerData.unlockCraft(craftId, UnlockMethod.NORMAL);
        player.sendMessage(plugin.getConfigManager().getConfig(MessagesConfig.class).getMessage("unlock_success_wiki").replace("%craft%", craft.getName()));
        plugin.getWebhookManager().sendUnlockLog(player, craft.getName(), craft.getCommands());

        setupItems();
    }

    @Override
    public void onClose(InventoryCloseEvent event) {}

    public void open() { player.openInventory(this.inventory); }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}