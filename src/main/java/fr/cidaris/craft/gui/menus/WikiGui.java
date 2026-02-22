package fr.cidaris.craft.gui.menus;

import fr.cidaris.craft.CidarisCraftPlugin;
import fr.cidaris.craft.gui.CidarisGui;
import fr.cidaris.craft.hook.impl.HeadDatabaseHook;
import fr.cidaris.craft.model.CraftDefinition;
import fr.cidaris.craft.model.PlayerData;
import fr.cidaris.craft.model.enums.UnlockMethod;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class WikiGui implements CidarisGui {

    private final CidarisCraftPlugin plugin;
    private final Player player;
    private final PlayerData playerData;
    private final Inventory inventory;

    private int scrollOffset;

    public WikiGui(CidarisCraftPlugin plugin, Player player, int scrollOffset) {
        this.plugin = plugin;
        this.player = player;
        this.playerData = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());
        this.scrollOffset = scrollOffset;

        this.inventory = Bukkit.createInventory(this, 54, "§8» §6Arbre (Tiers " + scrollOffset + "-"+(scrollOffset+4)+")");
        setupItems();
    }

    private void setupItems() {
        inventory.clear();
        HeadDatabaseHook hdb = plugin.getHookManager().getHook(HeadDatabaseHook.class);

        for (CraftDefinition craft : plugin.getCraftManager().getAllCrafts()) {
            int relativeTier = craft.getGuiTier() - scrollOffset;

            if (relativeTier < 0 || relativeTier > 4) continue;

            int slot = (relativeTier * 9) + craft.getGuiColumn();
            if (slot < 0 || slot >= 45) continue;

            String headId;
            String statusPrefix;

            if (playerData.hasUnlocked(craft.getId())) {
                headId = plugin.getConfig().getString("wiki-heads.unlocked");
                statusPrefix = "§a§l✔ §aDébloqué";
            } else if (!playerData.canBuyNormally(craft, plugin.getCraftManager())) {
                headId = plugin.getConfig().getString("wiki-heads.locked-parents");
                statusPrefix = "§c§l✖ §cParents Requis";
            } else if (!plugin.getEconomyManager().canAfford(player, craft.getCosts())) {
                headId = plugin.getConfig().getString("wiki-heads.locked-resources");
                statusPrefix = "§6§l$ §6Ressources Insuffisantes";
            } else {
                headId = plugin.getConfig().getString("wiki-heads.unlockable");
                statusPrefix = "§e§l! §ePrêt à débloquer";
            }

            ItemStack icon = (hdb != null && hdb.isHooked()) ? hdb.getHead(headId) : new ItemStack(Material.PAPER);
            if (icon == null) icon = new ItemStack(Material.PAPER);

            ItemMeta meta = icon.getItemMeta();
            meta.setDisplayName(statusPrefix + " §8- §f" + craft.getName());

            List<String> lore = new ArrayList<>();
            lore.add("§8§m-----------------------");
            craft.getCosts().forEach(c -> lore.add(" §7• §f" + c.display()));
            lore.add("§8§m-----------------------");

            meta.setLore(lore);
            icon.setItemMeta(meta);
            icon = plugin.getNbt().setString(icon, "gui-craft-id", craft.getId());

            inventory.setItem(slot, icon);
        }

        ItemStack glass = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 15);
        for (int i = 45; i < 54; i++) inventory.setItem(i, glass);

        if (scrollOffset > 0) {
            inventory.setItem(45, createNavButton("§e▲ Monter", Material.ARROW));
        }

        boolean hasMore = plugin.getCraftManager().getAllCrafts().stream().anyMatch(c -> c.getGuiTier() > scrollOffset + 4);
        if (hasMore) {
            inventory.setItem(53, createNavButton("§e▼ Descendre", Material.ARROW));
        }
    }

    private ItemStack createNavButton(String name, Material mat) {
        ItemStack it = new ItemStack(mat);
        ItemMeta m = it.getItemMeta();
        m.setDisplayName(name);
        it.setItemMeta(m);
        return it;
    }

    @Override
    public Inventory getInventory() { return inventory; }
    public void open() { player.openInventory(this.inventory); }

    @Override
    public void onClick(InventoryClickEvent event) {
        int slot = event.getRawSlot();
        if (slot == 45 && scrollOffset > 0) {
            new WikiGui(plugin, player, scrollOffset - 1).open();
        } else if (slot == 53 && inventory.getItem(53).getType() == Material.ARROW) {
            new WikiGui(plugin, player, scrollOffset + 1).open();
        }

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;

        String craftId = plugin.getNbt().getString(clicked, "gui-craft-id");
        if (craftId == null) return;

        CraftDefinition craft = plugin.getCraftManager().getCraft(craftId);
        if (craft == null) return;

        if (playerData.hasUnlocked(craftId)) {
            player.sendMessage("§cVous avez déjà débloqué ce craft !");
            return;
        }

        if (!playerData.canBuyNormally(craft, plugin.getCraftManager())) {
            player.sendMessage("§cVous devez d'abord débloquer la lignée parente normalement.");
            return;
        }

        if (!plugin.getEconomyManager().canAfford(player, craft.getCosts())) {
            player.sendMessage("§cVous n'avez pas les ressources nécessaires !");
            return;
        }

        plugin.getEconomyManager().pay(player, craft.getCosts());

        if (craft.getConsoleCommand() != null && !craft.getConsoleCommand().isEmpty()) {
            String command = craft.getConsoleCommand()
                    .replace("%player%", player.getName())
                    .replace("%craft%", craft.getId());
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
        }

        playerData.unlockCraft(craftId, UnlockMethod.NORMAL);
        player.sendMessage("§aFélicitations, vous avez débloqué : §e" + craft.getName());

        setupItems();
    }

    @Override
    public void onClose(InventoryCloseEvent event) {}
}