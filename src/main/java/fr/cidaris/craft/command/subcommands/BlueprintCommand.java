package fr.cidaris.craft.command.subcommands;

import fr.cidaris.craft.CidarisCraftPlugin;
import fr.cidaris.craft.command.SubCommand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class BlueprintCommand extends SubCommand {

    private final CidarisCraftPlugin plugin;

    public BlueprintCommand(CidarisCraftPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() { return "bp"; }

    @Override
    public String getDescription() { return "Vous donne un Blueprint scellé."; }

    @Override
    public String getSyntax() { return "/ccraft bp <random|specific> [id]"; }

    @Override
    public String getPermission() { return "cidaris.admin.give"; }

    @Override
    public void perform(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("§cSyntaxe: " + getSyntax());
            return;
        }

        String type = args[1].toLowerCase();
        ItemStack blueprintItem = null;

        if (type.equals("random")) {
            blueprintItem = plugin.getBlueprintManager().createSealedRandom();
            player.sendMessage("§a[Cidaris] Vous avez reçu un Blueprint Aléatoire !");

        } else if (type.equals("specific")) {
            if (args.length < 3) {
                player.sendMessage("§cVous devez préciser l'ID du craft. Ex: /ccraft bp specific opale_sword");
                return;
            }

            String craftId = args[2];
            if (plugin.getCraftManager().getCraft(craftId) == null) {
                player.sendMessage("§cErreur : Le craft '" + craftId + "' n'existe pas dans crafts.yml !");
                return;
            }

            blueprintItem = plugin.getBlueprintManager().createSealedSpecific(craftId);
            player.sendMessage("§a[Cidaris] Vous avez reçu un Blueprint Spécifique pour : §e" + craftId);

        } else {
            player.sendMessage("§cType inconnu. Utilisez 'random' ou 'specific'.");
            return;
        }

        if (blueprintItem != null) {
            player.getInventory().addItem(blueprintItem);
        }
    }
}