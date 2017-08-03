package me.lennartVH01.itemfinder;

import me.lennartVH01.itemfinder.nms.BlockMarker;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListener implements Listener {
	private final BlockMarker marker;
	
	public PlayerListener(BlockMarker marker){
		this.marker = marker;
	}
	@EventHandler
	public void onPlayerLeave(PlayerQuitEvent e){
		marker.onPlayerLeave(e.getPlayer().getUniqueId());
	}
}
