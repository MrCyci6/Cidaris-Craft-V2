package fr.cidaris.craft.command.subcommands;

import fr.cidaris.craft.CidarisCraftPlugin;
import fr.cidaris.craft.command.SubCommand;
import fr.cidaris.craft.model.PlayerData;
import fr.cidaris.craft.model.enums.UnlockMethod;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class GiveCraftCommand extends SubCommand {

    private final CidarisCraftPlugin plugin;

    public GiveCraftCommand(CidarisCraftPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() { return "givecraft"; }

    @Override
    public String getDescription() { return "Débloque un craft spécifique pour un joueur."; }

    @Override
    public String getSyntax() { return "/ccraft givecraft <joueur> <id>"; }

    @Override
    public String getPermission() { return "cidaris.admin.givecraft"; }

    @Override
    public void perform(Player player, String[] args) {
        if (args.length < 3) {
            player.sendMessage("§cUsage: " + getSyntax());
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        String craftId = args[2];

        if (target == null) {
            player.sendMessage("§cJoueur introuvable.");
            return;
        }

        if (plugin.getCraftManager().getCraft(craftId) == null) {
            player.sendMessage("§cLe craft §7" + craftId + " §cn'existe pas.");
            return;
        }

        PlayerData data = plugin.getPlayerDataManager().getPlayerData(target.getUniqueId());

        data.unlockCraft(craftId, UnlockMethod.ADMIN);

        player.sendMessage("§aLe craft §e" + craftId + " §aa été débloqué pour §e" + target.getName());
        target.sendMessage("§a[Cidaris] Vous avez débloqué le craft : §e" + craftId);
    }
}