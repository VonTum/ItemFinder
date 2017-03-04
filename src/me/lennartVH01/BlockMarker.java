package me.lennartVH01;

import java.util.List;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public interface BlockMarker {
	public void markObjects(Player p, List<Location> blocks, List<Entity> entities);
	
	public void removeMarkersFromPlayer(Player player);
	public void onPlayerLeave(UUID uniqueId);
	public void onDisable();
	
}
