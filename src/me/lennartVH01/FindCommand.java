package me.lennartVH01;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.block.ShulkerBox;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.DoubleChestInventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
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
	
	private static int countItems(ItemStack[] inv, Material mat, int data, boolean recursive){
		int count = 0;
		for(ItemStack stack:inv){
			if(stack != null && stack.getType() == mat && (data == -1 || data == stack.getData().getData()))
				count += stack.getAmount();
			
			// Search ShulkerBoxes recursively
			if(recursive && stack != null && stack.getItemMeta() instanceof BlockStateMeta){
				BlockStateMeta boxBlockMeta = (BlockStateMeta) stack.getItemMeta();
				if(boxBlockMeta.getBlockState() instanceof ShulkerBox){
					ShulkerBox box = (ShulkerBox) boxBlockMeta.getBlockState();
					
					count += countItems(box.getInventory().getContents(), mat, data, false);  // Tiny optimization since ShulkerBoxes can't contain ShulkerBoxes
				}
			}
		}
		
		return count;
	}
	
	@Override public boolean onCommand(CommandSender sender, Command cmd, String alias, String[] args){
		if(sender instanceof Player){
			Player player = (Player) sender;
			
			marker.removeMarkersFromPlayer(player);
			
			if(args.length == 0){
				return false;
			}
			
			Material mat = Material.matchMaterial(args[0]);
			if(mat == null){
				sender.sendMessage(ChatColor.RED + String.format(plugin.getConfig().getString("messages.error.unknown_material", "Material %s does not exist."), args[0]));
				return false;
			}
			
			//TEMP
			int data = -1;
			try{
				if(args.length >= 3)
					data = Integer.parseInt(args[2]);
			}catch(NumberFormatException e){return false;}
			
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
			
			//SuperMax radius even for Admins, just so they can't crash the server either :)
			if(radius > 1000)
				radius = 1000;
			
			{
				String msg = plugin.getConfig().getString("messages.info.search", "Searching for %s in a radius of %d blocks.");
				sender.sendMessage(String.format(msg, mat.toString(), radius));
			}
			
			markChests(player, radius, mat, data);
			
			return true;
		}else{
			sender.sendMessage(ChatColor.RED + plugin.getConfig().getString("messages.error.must_be_ingame", "You must be ingame to use this command"));
			return true;
		}
	}
	
	private void markChests(Player player, int radius, Material mat, int data){
		Location origin = player.getLocation();
		ArrayList<Location> foundChestLocations = new ArrayList<Location>();
		ArrayList<Entity> foundEntities = new ArrayList<Entity>();
		int containerItemCount = 0;
		boolean searchShulkerBox = plugin.getConfig().getBoolean("search_shulkerbox_recursively", true);
		
		for(int x = Math.floorDiv(origin.getBlockX() - radius, 16); x <= Math.floorDiv(origin.getBlockX() + radius, 16); x++){
			for(int z = Math.floorDiv(origin.getBlockZ() - radius, 16); z <= Math.floorDiv(origin.getBlockZ() + radius, 16); z++){
				//Containers
				for(BlockState b: player.getWorld().getChunkAt(x, z).getTileEntities()){
					if(b.getLocation().distanceSquared(origin) <= radius*radius
							&& (player.hasPermission(Permission.FIND_ADMIN) || permissionChecker.canAccess(player, b.getLocation()))
							&& b instanceof InventoryHolder){
						
						if(((InventoryHolder) b).getInventory() instanceof DoubleChestInventory){
							DoubleChestInventory doubleChestInv = (DoubleChestInventory) ((InventoryHolder) b).getInventory();
							if(doubleChestInv.getLeftSide().getHolder().equals(b)){
								int count = countItems(doubleChestInv.getContents(), mat, data, searchShulkerBox);
								if(count != 0){
									foundChestLocations.add(doubleChestInv.getLocation());
									containerItemCount += count;
								}
							}
						}else{
							int count = countItems(((InventoryHolder) b).getInventory().getContents(), mat, data, searchShulkerBox);
							if(count != 0){
								foundChestLocations.add(b.getLocation());
								containerItemCount += count;
							}
						}
					}
				}
				
				//Entities
				for(Entity e:player.getWorld().getChunkAt(x, z).getEntities()){
					if(e.getLocation().distanceSquared(origin) <= radius*radius
							&& (player.hasPermission(Permission.FIND_ADMIN) || permissionChecker.canAccess(player, e.getLocation()))
							&& e instanceof InventoryHolder
							&& !(e instanceof Player)){
						
						int count = countItems(((InventoryHolder) e).getInventory().getContents(), mat, data, searchShulkerBox);
						if(count != 0){
							foundEntities.add(e);
							containerItemCount += count;
						}
					}
				}
			}
		}
		int playerInventoryItemCount = countItems(player.getInventory().getContents(), mat, data, searchShulkerBox);
		int enderInventoryItemCount = countItems(player.getEnderChest().getContents(), mat, data, searchShulkerBox);
		
		{
			int total = containerItemCount + playerInventoryItemCount + enderInventoryItemCount;
			
			if(total > 0){
				player.sendMessage(String.format(plugin.getConfig().getString("messages.info.found.total", "Found %d items in total: "), total));
				
				
				player.sendMessage(String.format(plugin.getConfig().getString("messages.info.found.containers", "Chests: %d"), containerItemCount));
				
				if(playerInventoryItemCount > 0)
					player.sendMessage(String.format(plugin.getConfig().getString("messages.info.found.player", "Inventory: %d"), playerInventoryItemCount));
				
				if(enderInventoryItemCount > 0)
					player.sendMessage(String.format(plugin.getConfig().getString("messages.info.found.ender", "EnderChest: %d"), enderInventoryItemCount));
			}else{
				player.sendMessage(String.format(plugin.getConfig().getString("messages.info.found.nothingFound"), mat.toString() + ":" + data));
			}
		}
		
		marker.markObjects(player, foundChestLocations, foundEntities);
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
