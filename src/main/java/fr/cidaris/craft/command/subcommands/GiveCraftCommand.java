package fr.cidaris.craft.command.subcommands;

import fr.cidaris.craft.CidarisCraftPlugin;
import fr.cidaris.craft.command.SubCommand;
import fr.cidaris.craft.model.CraftDefinition;
import fr.cidaris.craft.model.PlayerData;
import fr.cidaris.craft.model.enums.UnlockMethod;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class GiveCraftCommand extends SubCommand {


    public GiveCraftCommand(CidarisCraftPlugin plugin) {
        super(plugin);
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
    public boolean isPlayerOnly() {
        return false;
    }

    @Override
    public void perform(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage("§cUsage: " + getSyntax());
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        String craftId = args[2];

        if (target == null) {
            sender.sendMessage(msgConfig.getMessage("player_not_found"));
            return;
        }

        CraftDefinition craft = plugin.getCraftManager().getCraft(craftId);
        if (craft == null) {
            sender.sendMessage(msgConfig.getMessage("craft_not_found").replace("%craft%", craftId));
            return;
        }

        PlayerData data = plugin.getPlayerDataManager().getPlayerData(target.getUniqueId());

        if (craft.getCommands() != null) {
            for (String cmd : craft.getCommands()) {
                String formattedCmd = cmd.replace("%player%", sender.getName()).replace("%craft%", craft.getId());
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), formattedCmd);
            }
        }

        data.unlockCraft(craftId, UnlockMethod.ADMIN);

        sender.sendMessage(msgConfig.getMessage("admin_givecraft_success").replace("%craft%", craftId).replace("%player%", target.getName()));
        target.sendMessage(msgConfig.getMessage("unlock_success_wiki").replace("%craft%", craftId));

        plugin.getWebhookManager().sendUnlockLog(target, craft.getName(), craft.getCommands());
    }
}