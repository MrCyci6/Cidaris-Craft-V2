package fr.cidaris.craft.command.subcommands;

import fr.cidaris.craft.CidarisRecipePlugin;
import fr.cidaris.craft.command.SubCommand;
import fr.cidaris.craft.gui.menus.BlueprintGui;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BlueprintGuiCommand extends SubCommand {

    public BlueprintGuiCommand(CidarisRecipePlugin plugin) {
        super(plugin);
    }

    @Override
    public String getName() { return "bpgui"; }

    @Override
    public String getDescription() { return "Ouvre la recherche de blueprint."; }

    @Override
    public String getSyntax() { return "/ccraft bpgui"; }

    @Override
    public String getPermission() { return null; }

    @Override
    public boolean isPlayerOnly() {
        return true;
    }

    @Override
    public void perform(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        BlueprintGui gui = new BlueprintGui(plugin, player);
        player.openInventory(gui.getInventory());
    }
}