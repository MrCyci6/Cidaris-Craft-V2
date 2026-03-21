package fr.cidaris.craft.command;

import fr.cidaris.craft.CidarisRecipePlugin;
import fr.cidaris.craft.config.files.MessagesConfig;
import org.bukkit.command.CommandSender;

public abstract class SubCommand {

    protected final CidarisRecipePlugin plugin;
    protected final MessagesConfig msgConfig;

    public abstract String getName();
    public abstract String getDescription();
    public abstract String getSyntax();
    public abstract String getPermission();
    public abstract boolean isPlayerOnly();
    public abstract void perform(CommandSender sender, String[] args);

    public SubCommand(CidarisRecipePlugin plugin) {
        this.plugin = plugin;
        this.msgConfig = plugin.getConfigManager().getConfig(MessagesConfig.class);
    }
}