package me.lennartVH01.itemfinder;

import java.util.List;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class CompatibilityBlockMarker implements BlockMarker {

	@Override
	public void markObjects(Player p, List<Location> blocks, List<Entity> entities) {
		p.sendMessage("§cItemFinder Compatibility Mode! Please notify an admin to update!");
		
		for(Location loc:blocks)
			p.sendMessage("<" + loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ() + ">");
		
		for(Entity e:entities){
			Location loc = e.getLocation();
			p.sendMessage("<" + loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ() + ">");
		}
	}
	
	@Override
	public void removeMarkersFromPlayer(Player player) {}

	@Override
	public void onPlayerLeave(UUID uniqueId) {}

	@Override
	public void onDisable() {}

}
