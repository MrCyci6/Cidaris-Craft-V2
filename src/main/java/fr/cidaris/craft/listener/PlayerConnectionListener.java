package fr.cidaris.craft.listener;

import fr.cidaris.craft.CidarisRecipePlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerConnectionListener implements Listener {

    private final CidarisRecipePlugin plugin;

    public PlayerConnectionListener(CidarisRecipePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        plugin.getPlayerDataManager().loadIntoCache(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        plugin.getPlayerDataManager().saveAndRemoveFromCache(event.getPlayer().getUniqueId());
    }
}