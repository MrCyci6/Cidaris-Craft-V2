package fr.cidaris.craft.command.subcommands;

import fr.cidaris.craft.CidarisCraftPlugin;
import fr.cidaris.craft.command.SubCommand;
import fr.cidaris.craft.gui.menus.WikiGui;
import org.bukkit.entity.Player;

public class WikiCommand extends SubCommand {

    private final CidarisCraftPlugin plugin;

    public WikiCommand(CidarisCraftPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() { return "wiki"; }

    @Override
    public String getDescription() { return "Ouvre l'arbre technologique."; }

    @Override
    public String getSyntax() { return "/ccraft wiki"; }

    @Override
    public String getPermission() { return "cidaris.use"; }

    @Override
    public void perform(Player player, String[] args) {
        WikiGui wiki = new WikiGui(plugin, player, 0);
        player.openInventory(wiki.getInventory());
    }
}