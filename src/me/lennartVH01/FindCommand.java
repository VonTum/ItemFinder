package me.lennartVH01;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.DoubleChestInventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;



public class FindCommand implements CommandExecutor, TabCompleter{
	private final JavaPlugin plugin;
	private final BlockMarker marker;
	private final PermissionChecker permissionChecker;
	
	public FindCommand(JavaPlugin plugin, BlockMarker marker, PermissionChecker checker){
		this.plugin = plugin;
		this.marker = marker;
		this.permissionChecker = checker;
	}
	
	private static int countItems(ItemStack[] items, Material type){
		int runningTotal = 0;
		for(ItemStack i:items){
			if(i != null && type.equals(i.getType())){
				runningTotal += i.getAmount();
			}
		}
		return runningTotal;
	}
	@Override public boolean onCommand(CommandSender sender, Command cmd, String alias, String[] args){
		if(sender instanceof Player){
			Player player = (Player) sender;
			
			marker.removeMarkersFromPlayer(player);
			
			if(args.length == 0){
				sender.sendMessage(plugin.getConfig().getString("messages.info.markerClear", "Markers cleared."));
				return true;
			}
			Material mat = Material.matchMaterial(args[0]);
			if(mat == null){
				sender.sendMessage(ChatColor.RED + String.format(plugin.getConfig().getString("messages.error.unknown_material", "Material %s does not exist."), args[0]));
				return false;
			}
			
			Location origin = player.getLocation();
			int radius;
			if(args.length >= 2){
				try{
					radius = Integer.parseInt(args[1]);
				}catch(NumberFormatException e){return false;}
			}else{
				radius = plugin.getConfig().getInt("default_search_radius", 20);
			}
			
			//constrict radius for non-admins
			if(!player.hasPermission(Permission.FIND_ADMIN))
				radius = Math.min(radius, plugin.getConfig().getInt("max_search_radius", 50));
			
			{
				String msg = plugin.getConfig().getString("messages.info.search", "Searching for %s in a radius of %d blocks.");
				sender.sendMessage(String.format(msg, mat.toString(), radius));
			}
			
			
			ArrayList<Location> foundChestLocations = new ArrayList<Location>();
			int itemCount = 0;
			
			for(int x = Math.floorDiv(origin.getBlockX() - radius, 16); x <= Math.floorDiv(origin.getBlockX() + radius, 16); x++){
				for(int z = Math.floorDiv(origin.getBlockZ() - radius, 16); z <= Math.floorDiv(origin.getBlockZ() + radius, 16); z++){
					for(BlockState b: player.getWorld().getChunkAt(x, z).getTileEntities()){
						if(b.getLocation().distanceSquared(origin) <= radius*radius
								&& (player.hasPermission(Permission.FIND_ADMIN) || permissionChecker.canAccess(player, b.getLocation()))
								&& b instanceof InventoryHolder
								&& ((InventoryHolder) b).getInventory().contains(mat)){
							
							if(((InventoryHolder) b).getInventory() instanceof DoubleChestInventory){
								DoubleChestInventory doubleChestInv = (DoubleChestInventory) ((InventoryHolder) b).getInventory();
								if(doubleChestInv.getLeftSide().getHolder().equals(b)){
									foundChestLocations.add(doubleChestInv.getLocation());
									
									itemCount += countItems(doubleChestInv.getContents(), mat);
									
								}
							}else{
								foundChestLocations.add(b.getLocation());
								itemCount += countItems(((InventoryHolder) b).getInventory().getContents(), mat);
							}
						}
					}
				}
			}
			
			{
				String msg = plugin.getConfig().getString("messages.info.found", "Found %d items.");
				sender.sendMessage(String.format(msg, itemCount));
			}
			
			marker.markBlocks(player, foundChestLocations);
			
			return true;
		}else{
			sender.sendMessage(ChatColor.RED + plugin.getConfig().getString("messages.error.must_be_ingame", "You must be ingame to use this command"));
			return true;
		}
	}
	
	@Override public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
		if(args.length == 1){
			ArrayList<String> materialNames = new ArrayList<String>();
			for(Material m:Material.values()){
				if(m.name().startsWith(args[0].toUpperCase()))
					materialNames.add(m.name().toLowerCase());
			}
			return materialNames;
		}
		return null;
	}
}
