package me.lennartVH01.itemfinder;

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
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.DoubleChestInventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;

import me.lennartVH01.itemfinder.Config.Messages;

public class FindCommand implements CommandExecutor, TabCompleter{
	private final BlockMarker marker;
	private final PermissionChecker permissionChecker;
	
	public FindCommand(BlockMarker marker, PermissionChecker checker){
		this.marker = marker;
		this.permissionChecker = checker;
	}
	
	private static int countItems(ItemStack[] inv, SearchCriteria criteria, boolean recursive){
		int count = 0;
		for(ItemStack stack:inv){
			if(criteria.matches(stack))
				count += stack.getAmount();
			
			// Search ShulkerBoxes recursively
			if(recursive && stack != null && stack.getItemMeta() instanceof BlockStateMeta){
				BlockStateMeta boxBlockMeta = (BlockStateMeta) stack.getItemMeta();
				
				// Quick and dirty fix to fix a (presumably) spigot bug
				// TODO have another look
				try{
					if(boxBlockMeta != null && boxBlockMeta.getBlockState() instanceof ShulkerBox){
						ShulkerBox box = (ShulkerBox) boxBlockMeta.getBlockState();
						
						count += countItems(box.getInventory().getContents(), criteria, false);  // Tiny optimization since ShulkerBoxes can't contain ShulkerBoxes
					}
				}catch(IllegalStateException ex){}
			}
		}
		
		return count;
	}
	
	@Override public boolean onCommand(CommandSender sender, Command cmd, String alias, String[] args){
		if(sender instanceof Player){
			Player player = (Player) sender;
			
			marker.removeMarkersFromPlayer(player);
			
			String searchName;
			SearchCriteria searchCriteria;
			
			if(args.length == 0){
				final ItemStack handStack = player.getInventory().getItemInMainHand();
				
				if(handStack != null && handStack.getType() != Material.AIR){
					searchName = handStack.getType().toString();
					searchCriteria = new SearchCriteria(){@Override public boolean matches(ItemStack stack) {return handStack.isSimilar(stack);}};
				}else{
					sender.sendMessage(Messages.ERROR_MUST_HOLD_ITEM);
					return false;
				}
			}else{
				searchName = args[0];
				
				String[] itemParts = args[0].split(":", 2);
				
				final Material mat = Material.matchMaterial(itemParts[0]);
				
				if(mat == null){
					sender.sendMessage(ChatColor.RED + String.format(Messages.ERROR_UNKNOWN_MATERIAL, itemParts[0]));
					return false;
				}
				
				if(itemParts.length == 1){
					searchName = mat.toString();
					searchCriteria = new SearchCriteria(){@Override public boolean matches(ItemStack stack) {return stack != null && stack.getType() == mat;}};
				}else{
					try{
						final int data = Integer.parseInt(itemParts[1]);
						searchName = mat.toString() + ":" + data;
						searchCriteria = new SearchCriteria(){@Override public boolean matches(ItemStack stack) {return stack != null && stack.getType() == mat && data == stack.getData().getData();}};
					}catch(NumberFormatException ex){return false;}
				}
			}
			
			int radius;
			if(args.length >= 2){
				try{
					radius = Integer.parseInt(args[1]);
				}catch(NumberFormatException e){return false;}
			}else{radius = Config.DEFAULT_SEARCH_RADIUS;}
			
			//constrict radius for non-admins
			if(!player.hasPermission(Permission.FIND_LONGRANGE) && radius > Config.MAX_SEARCH_RADIUS)
				radius = Config.MAX_SEARCH_RADIUS;
			
			//SuperMax radius even for Admins, just so they can't crash the server either :)
			if(radius > 1000)
				radius = 1000;
			
			String msg = Messages.INFO_SEARCH;
			
			sender.sendMessage(String.format(Messages.INFO_SEARCH, searchName, radius));
			
			markChests(player, searchCriteria, searchName, radius);
			
			return true;
		}else{
			sender.sendMessage(Messages.ERROR_MUST_BE_INGAME);
			return true;
		}
	}
	
	private void markChests(Player player, SearchCriteria searchCriteria, String queryName, int radius){
		Location origin = player.getLocation();
		ArrayList<Location> foundChestLocations = new ArrayList<Location>();
		ArrayList<Entity> foundEntities = new ArrayList<Entity>();
		int containerItemCount = 0;
		int floorItemCount = 0;
		
		for(int x = (int) Math.floor((origin.getBlockX() - radius)/16.0); x <= (int) Math.floor((origin.getBlockX() + radius)/16.0); x++){
			for(int z = (int) Math.floor((origin.getBlockZ() - radius)/16.0); z <= (int) Math.floor((origin.getBlockZ() + radius)/16.0); z++){
				//Containers
				for(BlockState b: player.getWorld().getChunkAt(x, z).getTileEntities()){
					if(b.getLocation().distanceSquared(origin) <= radius*radius
							&& (player.hasPermission(Permission.FIND_IGNOREPERMS) || permissionChecker.canAccess(player, b.getLocation()))
							&& b instanceof InventoryHolder){
						
						if(((InventoryHolder) b).getInventory() instanceof DoubleChestInventory){
							DoubleChestInventory doubleChestInv = (DoubleChestInventory) ((InventoryHolder) b).getInventory();
							if(doubleChestInv.getLeftSide().getHolder().equals(b)){
								int count = countItems(doubleChestInv.getContents(), searchCriteria, Config.SEARCH_SHULKERS_RECURSIVELY);
								if(count != 0){
									foundChestLocations.add(doubleChestInv.getLocation());
									containerItemCount += count;
								}
							}
						}else{
							int count = countItems(((InventoryHolder) b).getInventory().getContents(), searchCriteria, Config.SEARCH_SHULKERS_RECURSIVELY);
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
							&& (player.hasPermission(Permission.FIND_IGNOREPERMS) || permissionChecker.canAccess(player, e.getLocation()))
							&& !(e instanceof Player)){
						
						if(e instanceof InventoryHolder){
							int count = countItems(((InventoryHolder) e).getInventory().getContents(), searchCriteria, Config.SEARCH_SHULKERS_RECURSIVELY);
							if(count != 0){
								foundEntities.add(e);
								containerItemCount += count;
							}
						}else if(e instanceof Item){
							ItemStack stack = (ItemStack) ((Item) e).getItemStack();
							if(searchCriteria.matches(stack)){
								foundEntities.add(e);
								floorItemCount += stack.getAmount();
							}
						}
					}
				}
			}
		}
		int playerInventoryItemCount = countItems(player.getInventory().getContents(), searchCriteria, Config.SEARCH_SHULKERS_RECURSIVELY);
		int enderInventoryItemCount = countItems(player.getEnderChest().getContents(), searchCriteria, Config.SEARCH_SHULKERS_RECURSIVELY);
		
		int total = containerItemCount + floorItemCount + playerInventoryItemCount + enderInventoryItemCount;
		
		if(total > 0){
			player.sendMessage(String.format(Messages.INFO_FOUND_TOTAL, total));
			
			player.sendMessage(String.format(Messages.INFO_FOUND_CONTAINER, containerItemCount));
			
			if(floorItemCount > 0)
				player.sendMessage(String.format(Messages.INFO_FOUND_FLOOR, floorItemCount));
			
			if(playerInventoryItemCount > 0)
				player.sendMessage(String.format(Messages.INFO_FOUND_PLAYER, playerInventoryItemCount));
			
			if(enderInventoryItemCount > 0)
				player.sendMessage(String.format(Messages.INFO_FOUND_ENDER, enderInventoryItemCount));
		}else{
			player.sendMessage(String.format(Messages.INFO_FOUND_NOTHINGFOUND, queryName));
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
	
	private static interface SearchCriteria {
		public boolean matches(ItemStack stack);
	}
}
