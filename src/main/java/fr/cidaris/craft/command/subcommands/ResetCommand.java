package fr.cidaris.craft.command.subcommands;

import fr.cidaris.craft.CidarisRecipePlugin;
import fr.cidaris.craft.command.SubCommand;
import fr.cidaris.craft.model.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ResetCommand extends SubCommand {


    public ResetCommand(CidarisRecipePlugin plugin) {
        super(plugin);
    }

    @Override
    public String getName() { return "reset"; }

    @Override
    public String getDescription() { return "Réinitialise tous les crafts d'un joueur."; }

    @Override
    public String getSyntax() { return "/ccraft reset <joueur>"; }

    @Override
    public String getPermission() { return "cidaris.admin.reset"; }

    @Override
    public boolean isPlayerOnly() {
        return false;
    }

    @Override
    public void perform(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("§cUsage: " + getSyntax());
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(msgConfig.getMessage("player_not_found"));
            return;
        }

        PlayerData data = plugin.getPlayerDataManager().getPlayerData(target.getUniqueId());
        data.getUnlockedCrafts().clear();

        plugin.getPlayerDataManager().saveAndRemoveFromCache(target.getUniqueId());
        plugin.getPlayerDataManager().loadIntoCache(target.getUniqueId());

        sender.sendMessage(msgConfig.getMessage("admin_reset_success").replace("%player%", target.getName()));
        target.sendMessage(msgConfig.getMessage("admin_reset_notify"));
    }
}