package fr.cidaris.craft.listener;

import fr.cidaris.craft.gui.CidarisGui;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

public class GuiListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getClickedInventory() == null) return;
        if (event.getInventory().getHolder() instanceof CidarisGui) {
            event.setCancelled(true);
            CidarisGui gui = (CidarisGui) event.getInventory().getHolder();
            gui.onClick(event);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getInventory().getHolder() instanceof CidarisGui) {
            CidarisGui gui = (CidarisGui) event.getInventory().getHolder();
            gui.onClose(event);
        }
    }
}