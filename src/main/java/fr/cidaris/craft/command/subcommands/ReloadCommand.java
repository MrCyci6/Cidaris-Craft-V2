package fr.cidaris.craft.command.subcommands;

import fr.cidaris.craft.CidarisRecipePlugin;
import fr.cidaris.craft.command.SubCommand;
import fr.cidaris.craft.config.files.MessagesConfig;
import org.bukkit.command.CommandSender;

public class ReloadCommand extends SubCommand {

    public ReloadCommand(CidarisRecipePlugin plugin) {
        super(plugin);
    }

    @Override
    public String getName() { return "reload"; }

    @Override
    public String getDescription() { return "Recharge TOUTES les configurations du plugin."; }

    @Override
    public String getSyntax() { return "/ccraft reload"; }

    @Override
    public String getPermission() { return "cidaris.admin.reload"; }

    @Override
    public boolean isPlayerOnly() {
        return false;
    }

    @Override
    public void perform(CommandSender sender, String[] args) {

        long startTime = System.currentTimeMillis();

        plugin.reloadConfig();
        plugin.getConfigManager().reloadAll();
        plugin.getCraftManager().loadCrafts();

        long timeTaken = System.currentTimeMillis() - startTime;

        MessagesConfig msgConfig = plugin.getConfigManager().getConfig(MessagesConfig.class);
        sender.sendMessage(msgConfig.getMessage("reload_success") + " §7(" + timeTaken + "ms)");

        int craftCount = plugin.getCraftManager().getAllCrafts().size();
        sender.sendMessage("§8» §e" + craftCount + " §7crafts chargés en mémoire.");
    }
}