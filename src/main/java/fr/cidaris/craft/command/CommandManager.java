package fr.cidaris.craft.command;

import fr.cidaris.craft.CidarisRecipePlugin;
import fr.cidaris.craft.command.subcommands.*;
import fr.cidaris.craft.config.files.MessagesConfig;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class CommandManager implements CommandExecutor {

    private final CidarisRecipePlugin plugin;
    private final List<SubCommand> subcommands = new ArrayList<>();

    public CommandManager(CidarisRecipePlugin plugin) {
        this.plugin = plugin;
        subcommands.add(new WikiCommand(plugin));
        subcommands.add(new GiveCraftCommand(plugin));
        subcommands.add(new GiveBlueprintCommand(plugin));
        subcommands.add(new BlueprintGuiCommand(plugin));
        subcommands.add(new ResetCommand(plugin));
        subcommands.add(new ReloadCommand(plugin));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            sendHelpMessage(sender);
            return true;
        }

        for (SubCommand subCommand : subcommands) {
            if (args[0].equalsIgnoreCase(subCommand.getName())) {

                if (subCommand.getPermission() != null && !sender.hasPermission(subCommand.getPermission())) {
                    sender.sendMessage(plugin.getConfigManager().getConfig(MessagesConfig.class).getMessage("no_permission"));
                    return true;
                }

                if (subCommand.isPlayerOnly() && !(sender instanceof Player)) {
                    sender.sendMessage("§cErreur : Cette commande ne peut être exécutée que par un joueur en jeu.");
                    return true;
                }

                subCommand.perform(sender, args);
                return true;
            }
        }

        sender.sendMessage("§cCommande inconnue. Tapez /ccraft help pour voir la liste.");
        return true;
    }

    private void sendHelpMessage(CommandSender sender) {
        sender.sendMessage(" ");
        sender.sendMessage("§8§m----------------------------------------");
        sender.sendMessage("             §b§lCidaris Craft");
        sender.sendMessage(" ");

        boolean hasAnyCommand = false;

        for (SubCommand subCommand : subcommands) {
            if (subCommand.getPermission() == null || subCommand.getPermission().isEmpty() || sender.hasPermission(subCommand.getPermission())) {

                if (subCommand.isPlayerOnly() && !(sender instanceof Player)) {
                    sender.sendMessage(" §8» §c" + subCommand.getSyntax() + " §8- §7(Joueur Uniquement)");
                } else {
                    sender.sendMessage(" §8» §e" + subCommand.getSyntax() + " §8- §7" + subCommand.getDescription());
                }
                hasAnyCommand = true;
            }
        }

        if (!hasAnyCommand) {
            sender.sendMessage(" §cVous n'avez accès à aucune commande.");
        }

        sender.sendMessage("§8§m----------------------------------------");
        sender.sendMessage(" ");
    }
}