package fr.cidaris.craft.command.subcommands;

import fr.cidaris.craft.CidarisRecipePlugin;
import fr.cidaris.craft.command.SubCommand;
import fr.cidaris.craft.gui.menus.WikiGui;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class WikiCommand extends SubCommand {


    public WikiCommand(CidarisRecipePlugin plugin) {
        super(plugin);
    }

    @Override
    public String getName() { return "wiki"; }

    @Override
    public String getDescription() { return "Ouvre l'arbre technologique."; }

    @Override
    public String getSyntax() { return "/ccraft wiki"; }

    @Override
    public String getPermission() { return null; }

    @Override
    public boolean isPlayerOnly() {
        return true;
    }

    @Override
    public void perform(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        WikiGui wiki = new WikiGui(plugin, player, 0, 0);
        player.openInventory(wiki.getInventory());
    }
}