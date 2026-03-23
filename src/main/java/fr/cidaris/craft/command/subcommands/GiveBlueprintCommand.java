package fr.cidaris.craft.command.subcommands;

import fr.cidaris.craft.CidarisRecipePlugin;
import fr.cidaris.craft.command.SubCommand;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class GiveBlueprintCommand extends SubCommand {


    public GiveBlueprintCommand(CidarisRecipePlugin plugin) {
        super(plugin);
    }

    @Override
    public String getName() { return "givebp"; }

    @Override
    public String getDescription() { return "Vous donne un Blueprint scellé."; }

    @Override
    public String getSyntax() { return "/ccraft givebp <joueur> <random|specific> [id]"; }

    @Override
    public String getPermission() { return "cidaris.admin.give"; }

    @Override
    public boolean isPlayerOnly() {
        return false;
    }

    @Override
    public void perform(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("§cSyntaxe: " + getSyntax());
            return;
        }

        String type = args[2].toLowerCase();
        Player target = Bukkit.getPlayer(args[1]);

        if (target == null) {
            sender.sendMessage(msgConfig.getMessage("player_not_found"));
            return;
        }

        ItemStack blueprintItem = null;

        if (type.equals("random")) {
            blueprintItem = plugin.getBlueprintManager().createSealedRandom();

        } else if (type.equals("specific")) {
            if (args.length < 4) {
                sender.sendMessage("§cSyntaxe: " + getSyntax());
                return;
            }

            String craftId = args[3];
            if (plugin.getCraftManager().getCraft(craftId) == null) {
                sender.sendMessage(msgConfig.getMessage("craft_not_found").replace("%craft%", craftId));
                return;
            }

            blueprintItem = plugin.getBlueprintManager().createSealedSpecific(craftId);

        } else {
            sender.sendMessage("§cSyntaxe: " + getSyntax());
            return;
        }

        if (blueprintItem != null) {
            target.getInventory().addItem(blueprintItem);
            target.sendMessage(msgConfig.getMessage("blueprint_received"));

        }
    }
}