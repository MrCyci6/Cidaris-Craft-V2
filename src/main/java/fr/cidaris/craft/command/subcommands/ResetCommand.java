package fr.cidaris.craft.command.subcommands;

import fr.cidaris.craft.CidarisCraftPlugin;
import fr.cidaris.craft.command.SubCommand;
import fr.cidaris.craft.model.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class ResetCommand extends SubCommand {

    private final CidarisCraftPlugin plugin;

    public ResetCommand(CidarisCraftPlugin plugin) {
        this.plugin = plugin;
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
    public void perform(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("§cUsage: " + getSyntax());
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            player.sendMessage("§cJoueur introuvable.");
            return;
        }

        PlayerData data = plugin.getPlayerDataManager().getPlayerData(target.getUniqueId());
        data.getUnlockedCrafts().clear();

        plugin.getPlayerDataManager().saveAndRemoveFromCache(target.getUniqueId());
        plugin.getPlayerDataManager().loadIntoCache(target.getUniqueId());

        player.sendMessage("§aLes crafts de §e" + target.getName() + " §aont été réinitialisés.");
        target.sendMessage("§6[Cidaris] Vos crafts ont été réinitialisés par un administrateur.");
    }
}