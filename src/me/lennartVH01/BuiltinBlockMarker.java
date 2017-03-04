package me.lennartVH01;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

import net.minecraft.server.v1_11_R1.EntityMagmaCube;
import net.minecraft.server.v1_11_R1.PacketPlayOutEntityDestroy;
import net.minecraft.server.v1_11_R1.PacketPlayOutMount;
import net.minecraft.server.v1_11_R1.PacketPlayOutSpawnEntityLiving;

import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_11_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_11_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_11_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

public class BuiltinBlockMarker implements BlockMarker {
	private final ItemFinder plugin;
	private final HashMap<UUID, MarkedTask> playerMap = new HashMap<UUID, MarkedTask>();
	
	public BuiltinBlockMarker(ItemFinder plugin){
		this.plugin = plugin;
	}
	public void markObjects(final org.bukkit.entity.Player p, final List<Location> blocks, final List<org.bukkit.entity.Entity> entities){
		final int[] cubeIds = new int[blocks.size()+entities.size()];
		
		//Mark Blocks
		for(int i = 0; i < blocks.size(); i++){
			EntityMagmaCube cube = createMarkerAt((CraftPlayer) p, blocks.get(i).getX() + 0.5, blocks.get(i).getY() + 0.25, blocks.get(i).getZ() + 0.5);
			cubeIds[i] = cube.getId();
			sendSpawnEntityPacket(p, cube);
		}
		
		//Mark Entities
		if(entities.size() > 0){
			try{
				/*for(Field f:PacketPlayOutMount.class.getDeclaredFields())
					System.out.println(f.getName());
				
				System.out.println("HAI FIELDS OF PACKETPLAYOUTMOUNT:\n" + PacketPlayOutMount.class.getDeclaredFields() + " of size " + PacketPlayOutMount.class.getDeclaredFields().length);
				*/
				
				Field vehicleField = PacketPlayOutMount.class.getDeclaredField("a");
				vehicleField.setAccessible(true);
				
				Field passengerField = PacketPlayOutMount.class.getDeclaredField("b");
				passengerField.setAccessible(true);
				
				
				for(int i = 0; i < entities.size(); i++){
					org.bukkit.entity.Entity e = entities.get(i);
					
					EntityMagmaCube cube = createMarkerAt((CraftPlayer) p, e.getLocation().getX(), e.getLocation().getY(), e.getLocation().getZ());
					
					// Could you believe I spent ages screwing around with tp timers and sending fake riding packets only to finally solve it with just 1 line?
					// Although, in my defence, setPassenger *is* deprectated
					//e.setPassenger(cube.getBukkitEntity());
					
					
					cubeIds[i+blocks.size()] = cube.getId();
					PacketPlayOutMount mountPacket = new PacketPlayOutMount();
					
					
					
					// Keep current passengers
					int[] passengers = new int[e.getPassengers().size() + 1];
					for(int j = 0; j < e.getPassengers().size(); j++)
						passengers[j] = e.getPassengers().get(j).getEntityId();
					
					// Add cube to passenger list
					passengers[e.getPassengers().size()] = cube.getId();
					
					// bottom entity, in this case, whatever minecart/armorstand is wanted for marking
					vehicleField.setInt(mountPacket, cube.getId());
					
					// Add passenger list
					passengerField.set(mountPacket, passengers);
					
					sendSpawnEntityPacket(p, cube);
					
					//((CraftPlayer) p).getHandle().playerConnection.sendPacket(mountPacket);
				}
				
				
				vehicleField.setAccessible(false);
				passengerField.setAccessible(false);
				
			} catch (Exception e1) {
				plugin.getLogger().log(Level.WARNING, "ERROR: Cannot edit field in PacketPlayOutMount. Please contact the developer");
				e1.printStackTrace();
			}
		}
		
		
		
		
		//Regiser Kill everything task
		int taskId = plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
			@Override public void run() {
				playerMap.remove(p.getUniqueId());
				if(p != null && p.isOnline()){
					removeEntitys(p, cubeIds, entities);
				}
			}
		}, plugin.getConfig().getLong("marker_timeout", 500));
		playerMap.put(p.getUniqueId(), new MarkedTask(taskId, cubeIds, entities));
	}
	private static EntityMagmaCube createMarkerAt(CraftPlayer player, double x, double y, double z){
		EntityMagmaCube cube = new EntityMagmaCube(((CraftWorld) player.getWorld()).getHandle());
		
		cube.setLocation(x, y, z, 0, 0);
		
		cube.setFlag(5, true);//invisible
		cube.setFlag(6, true);//glowing
		
		return cube;
	}
	private static void sendSpawnEntityPacket(org.bukkit.entity.Player player, EntityMagmaCube cube){
		((CraftPlayer) player).getHandle().playerConnection.sendPacket(new PacketPlayOutSpawnEntityLiving(cube));
	}
	
	public void removeMarkersFromPlayer(Player p){
		if(playerMap.containsKey(p.getUniqueId())){
			MarkedTask t = playerMap.get(p.getUniqueId());
			t.cancelTask();
			
			removeEntitys(p, t.ids, t.entities);
		}
	}
	public void onDisable(){
		for(Map.Entry<UUID, MarkedTask> playerEntry:playerMap.entrySet()){
			playerEntry.getValue().cancelTask();
			
			removeEntitys(plugin.getServer().getPlayer(playerEntry.getKey()), playerEntry.getValue().ids, playerEntry.getValue().entities);
		}
	}
	public void onPlayerLeave(UUID player){
		playerMap.remove(player).cancelTask();
	}
	private void removeEntitys(Player p, int[] ids, List<org.bukkit.entity.Entity> entities){
		//unmount everything
		for(org.bukkit.entity.Entity e:entities)
			((CraftPlayer) p).getHandle().playerConnection.sendPacket(new PacketPlayOutMount(((CraftEntity) e).getHandle()));
		//remove cubes
		((CraftPlayer) p).getHandle().playerConnection.sendPacket(new PacketPlayOutEntityDestroy(ids));
	}
	private class MarkedTask{
		public int taskId;
		public int[] ids;
		public List<org.bukkit.entity.Entity> entities;
		public MarkedTask(int taskId, int[] ids, List<org.bukkit.entity.Entity> entities){
			this.taskId = taskId;
			this.ids = ids;
			this.entities = entities;
		}
		public void cancelTask(){
			plugin.getServer().getScheduler().cancelTask(taskId);
		}
	}
}
