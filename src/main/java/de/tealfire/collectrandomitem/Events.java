package de.tealfire.collectrandomitem;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityPickupItemEvent;

public class Events {
    @EventHandler
    public void onEntityPickupItemEvent(EntityPickupItemEvent event){
        Bukkit.getLogger().info(event.getItem().toString()); // Debug message
    }
}
