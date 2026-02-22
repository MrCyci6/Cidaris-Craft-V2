package fr.cidaris.craft.gui;

import org.bukkit.inventory.InventoryHolder;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

public interface CidarisGui extends InventoryHolder {
    void onClick(InventoryClickEvent event);
    void onClose(InventoryCloseEvent event);
}