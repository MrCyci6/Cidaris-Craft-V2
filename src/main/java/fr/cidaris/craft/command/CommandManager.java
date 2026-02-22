package fr.cidaris.craft.command;

import fr.cidaris.craft.CidarisCraftPlugin;
import fr.cidaris.craft.command.subcommands.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class CommandManager implements CommandExecutor {

    private final List<SubCommand> subcommands = new ArrayList<>();

    public CommandManager(CidarisCraftPlugin plugin) {
        subcommands.add(new WikiCommand(plugin));
        subcommands.add(new WorkbenchCommand(plugin));
        subcommands.add(new GiveCraftCommand(plugin));
        subcommands.add(new BlueprintCommand(plugin));
        subcommands.add(new BlueprintGuiCommand(plugin));
        subcommands.add(new ResetCommand(plugin));
        subcommands.add(new ReloadCommand(plugin));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cCette commande est réservée aux joueurs.");
            return true;
        }

        Player player = (Player) sender;

        if (args.length > 0) {
            for (SubCommand subCommand : subcommands) {
                if (args[0].equalsIgnoreCase(subCommand.getName())) {
                    if (subCommand.getPermission() != null && !player.hasPermission(subCommand.getPermission())) {
                        player.sendMessage("§cVous n'avez pas la permission.");
                        return true;
                    }
                    subCommand.perform(player, args);
                    return true;
                }
            }
        }

        player.sendMessage("§8§m--------------------------------");
        for (SubCommand subCommand : subcommands) {
            player.sendMessage("§e" + subCommand.getSyntax() + " §7- " + subCommand.getDescription());
        }
        player.sendMessage("§8§m--------------------------------");

        return true;
    }
}