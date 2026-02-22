package fr.cidaris.craft.command.subcommands;

import fr.cidaris.craft.CidarisCraftPlugin;
import fr.cidaris.craft.command.SubCommand;
import fr.cidaris.craft.gui.menus.WorkbenchGui;
import org.bukkit.entity.Player;

public class WorkbenchCommand extends SubCommand {

    private final CidarisCraftPlugin plugin;

    public WorkbenchCommand(CidarisCraftPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() { return "workbench"; }

    @Override
    public String getDescription() { return "Ouvre la table de craft."; }

    @Override
    public String getSyntax() { return "/ccraft workbench"; }

    @Override
    public String getPermission() { return "cidaris.use"; }

    @Override
    public void perform(Player player, String[] args) {
        WorkbenchGui gui = new WorkbenchGui(plugin, player);
        player.openInventory(gui.getInventory());
    }
}