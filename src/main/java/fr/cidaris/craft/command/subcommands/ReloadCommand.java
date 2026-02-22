package fr.cidaris.craft.command.subcommands;

import fr.cidaris.craft.CidarisCraftPlugin;
import fr.cidaris.craft.command.SubCommand;
import org.bukkit.entity.Player;

public class ReloadCommand extends SubCommand {

    private final CidarisCraftPlugin plugin;

    public ReloadCommand(CidarisCraftPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() { return "reload"; }

    @Override
    public String getDescription() { return "Recharge la configuration des crafts."; }

    @Override
    public String getSyntax() { return "/ccraft reload"; }

    @Override
    public String getPermission() { return "cidaris.admin.reload"; }

    @Override
    public void perform(Player player, String[] args) {
        plugin.reloadConfig();

        plugin.getCraftManager().loadCrafts();

        player.sendMessage("§a[CidarisCraft] Configuration et " + plugin.getCraftManager().getAllCrafts().size() + " crafts rechargés !");
        player.sendMessage("§7Note: Les joueurs doivent fermer et réouvrir le Wiki pour voir les changements.");
    }
}