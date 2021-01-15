package de.jeff_media.AngelChestPlus.listeners;

import de.jeff_media.AngelChestPlus.Main;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;

public class HologramListener implements Listener {
	
	final Main main;
	
	public HologramListener(Main main) {
		this.main = main;
	}
	
	@EventHandler
	public void manipulate(PlayerArmorStandManipulateEvent e)
	{
		if(e.getRightClicked().isVisible()) return;
		if(!main.isAngelChestHologram(e.getRightClicked())) return;
	    e.setCancelled(true);
	}

}