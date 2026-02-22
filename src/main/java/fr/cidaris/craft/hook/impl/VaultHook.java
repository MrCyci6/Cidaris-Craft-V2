package fr.cidaris.craft.hook.impl;

import fr.cidaris.craft.hook.PluginHook;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;

public class VaultHook implements PluginHook {

    private Economy economy = null;
    private boolean hooked = false;

    @Override
    public String getPluginName() {
        return "Vault";
    }

    @Override
    public boolean setup(Plugin plugin) {
        if (Bukkit.getServer().getPluginManager().getPlugin(getPluginName()) == null) {
            return false;
        }

        RegisteredServiceProvider<Economy> rsp = Bukkit.getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }

        economy = rsp.getProvider();
        hooked = (economy != null);
        return hooked;
    }

    @Override
    public boolean isHooked() {
        return hooked;
    }

    // Méthode spécifique à ce hook
    public Economy getEconomy() {
        return economy;
    }
}