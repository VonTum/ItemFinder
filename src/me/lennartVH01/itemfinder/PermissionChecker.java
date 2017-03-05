package me.lennartVH01.itemfinder;

import java.util.ArrayList;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.palmergames.bukkit.towny.Towny;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.TownyUniverse;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;

public class PermissionChecker {
	
	private final ArrayList<PluginChecker> checkerList = new ArrayList<PluginChecker>();
	
	public PermissionChecker(ItemFinder plugin){
		
		Plugin townyPlugin = plugin.getServer().getPluginManager().getPlugin("Towny");
		if(townyPlugin instanceof Towny){
			checkerList.add(new PluginChecker() {
				
				@Override public boolean canAccess(Player p, Location loc) {
					String townName = TownyUniverse.getTownName(loc);
					if(townName == null)
						//If no town found then check if they can build in the wild
						//return p.hasPermission(com.palmergames.bukkit.towny.permissions.PermissionNodes.TOWNY_WILD_BLOCK_SWITCH.getNode());
						return true;
					else{
						try {
							return TownyUniverse.getDataSource().getTown(townName).hasResident(p.getName());
						} catch (NotRegisteredException e) {
							return true;
						}
					}
				}
			});
		}
		
		Plugin wgPlugin = plugin.getServer().getPluginManager().getPlugin("WorldGuard");
		if(wgPlugin instanceof WorldGuardPlugin){
			final WorldGuardPlugin worldGuard = (WorldGuardPlugin) wgPlugin;
			
			checkerList.add(new PluginChecker() {
				@Override public boolean canAccess(Player p, Location loc) {
					return worldGuard.canBuild(p, loc);
				}
			});
		}
	}
	
	public boolean canAccess(Player p, Location loc){
		if(p.hasPermission(Permission.FIND_ADMIN)) return true;
		
		for(PluginChecker checker:checkerList)
			if(!checker.canAccess(p, loc))
				return false;
		
		return true;
	}
	
	private static interface PluginChecker{
		public boolean canAccess(Player p, Location loc);
	}
}
