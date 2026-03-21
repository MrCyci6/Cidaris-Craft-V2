package fr.cidaris.craft.gui.menus;

import fr.cidaris.craft.CidarisRecipePlugin;
import fr.cidaris.craft.config.files.GuisConfig;
import fr.cidaris.craft.config.files.MainConfig;
import fr.cidaris.craft.config.files.MessagesConfig;
import fr.cidaris.craft.gui.CidarisGui;
import fr.cidaris.craft.hook.impl.HeadDatabaseHook;
import fr.cidaris.craft.model.Cost;
import fr.cidaris.craft.model.CraftDefinition;
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
    private final Inventory inventory;
    private final ConfigurationSection config;

    private final int SIZE;
    private final int UP_SLOT;
    private final int DOWN_SLOT;
    private int scrollOffset;

    public WikiGui(CidarisRecipePlugin plugin, Player player, int scrollOffset) {
        this.plugin = plugin;
        this.player = player;
        this.playerData = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());
        this.scrollOffset = scrollOffset;

        this.config = plugin.getConfigManager().getConfig(GuisConfig.class).get().getConfigurationSection("wiki_tree");

        this.SIZE = config.getInt("size", 54);
        this.UP_SLOT = config.getInt("pagination.up_slot", 45);
        this.DOWN_SLOT = config.getInt("pagination.down_slot", 53);

        String title = config.getString("title", "Arbre")
                .replace("%start%", String.valueOf(scrollOffset))
                .replace("%end%", String.valueOf(scrollOffset + 4));

        this.inventory = Bukkit.createInventory(this, SIZE, ChatColor.translateAlternateColorCodes('&', title));
        setupItems();
    }

    private void setupItems() {
        inventory.clear();

        // Background
        ItemStack bgItem = ConfigItemBuilder.fromConfig(plugin, config.getConfigurationSection("background"));
        for (int i = 0; i < SIZE; i++) inventory.setItem(i, bgItem);

        HeadDatabaseHook hdb = plugin.getHookManager().getHook(HeadDatabaseHook.class);
        ConfigurationSection mainConfig = plugin.getConfigManager().getConfig(MainConfig.class).get();

        // Remplissage des Crafts
        for (CraftDefinition craft : plugin.getCraftManager().getAllCrafts()) {
            int relativeTier = craft.getGuiTier() - scrollOffset;
            if (relativeTier < 0 || relativeTier > (SIZE / 9 - 2)) continue;

            int slot = (relativeTier * 9) + craft.getGuiColumn();
            if (slot < 0 || slot >= (SIZE - 9)) continue;

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

            // Construction de l'icône
            ItemStack icon = (hdb != null && hdb.isHooked() && headId != null) ? hdb.getHead(headId) : new ItemStack(Material.PAPER);
            if (icon == null) icon = new ItemStack(Material.PAPER);

            ItemMeta meta = icon.getItemMeta();

            // Nom depuis guis.yml
            String prefix = config.getString("statuses." + statusKey + ".prefix", "");
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', prefix + " &8- &f" + craft.getName()));

            // Lore depuis guis.yml
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

            inventory.setItem(slot, icon);
        }

        // Pagination
        if (scrollOffset > 0) {
            inventory.setItem(UP_SLOT, ConfigItemBuilder.fromConfig(plugin, config.getConfigurationSection("pagination.up_item")));
        }

        boolean hasMore = plugin.getCraftManager().getAllCrafts().stream().anyMatch(c -> c.getGuiTier() > scrollOffset + (SIZE / 9 - 2));
        if (hasMore) {
            inventory.setItem(DOWN_SLOT, ConfigItemBuilder.fromConfig(plugin, config.getConfigurationSection("pagination.down_item")));
        }
    }

    @Override
    public void onClick(InventoryClickEvent event) {
        event.setCancelled(true);
        int slot = event.getRawSlot();
        if (slot >= SIZE) return;

        if (slot == UP_SLOT && scrollOffset > 0) {
            new WikiGui(plugin, player, scrollOffset - 1).open();
            return;
        } else if (slot == DOWN_SLOT && inventory.getItem(DOWN_SLOT) != null && inventory.getItem(DOWN_SLOT).getType() != Material.STAINED_GLASS_PANE) {
            new WikiGui(plugin, player, scrollOffset + 1).open();
            return;
        }

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