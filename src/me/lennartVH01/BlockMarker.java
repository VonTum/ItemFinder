package me.lennartVH01;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import net.minecraft.server.v1_11_R1.EntityMagmaCube;
import net.minecraft.server.v1_11_R1.PacketPlayOutEntityDestroy;
import net.minecraft.server.v1_11_R1.PacketPlayOutSpawnEntityLiving;

import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_11_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_11_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

public class BlockMarker{
	private final ItemFinder plugin;
	private final HashMap<UUID, MarkedTask> playerMap = new HashMap<UUID, MarkedTask>();
	
	public BlockMarker(ItemFinder plugin){
		this.plugin = plugin;
	}
	public void markObjects(final org.bukkit.entity.Player p, List<Location> blocks, List<org.bukkit.entity.Entity> entities){
		final int[] cubeIds = new int[blocks.size()+entities.size()];
		
		for(int i = 0; i < blocks.size(); i++){
			cubeIds[i] = createMarkerAt((CraftPlayer) p, blocks.get(i).getX() + 0.5, blocks.get(i).getY() + 0.25, blocks.get(i).getZ() + 0.5).getId();
		}
		
		//TODO Entities
		/*for(int i = 0; i < entities.size(); i++){
			org.bukkit.entity.Entity e = entities.get(i);
			EntityMagmaCube cube = createMarkerAt((CraftPlayer) p, e.getLocation().getX(), e.getLocation().getY(), e.getLocation().getZ());
			cubeIds[i+blocks.size()] = cube.getId();
			new PacketPlayOutEntity
			
		}*/
		
		int taskId = plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
			@Override public void run() {
				playerMap.remove(p.getUniqueId());
				if(p != null && p.isOnline()){
					removeEntitys(p, cubeIds);
				}
			}
		}, plugin.getConfig().getLong("marker_timeout", 500));
		playerMap.put(p.getUniqueId(), new MarkedTask(taskId, cubeIds));
	}
	private static EntityMagmaCube createMarkerAt(CraftPlayer player, double x, double y, double z){
		EntityMagmaCube cube = new EntityMagmaCube(((CraftWorld) player.getWorld()).getHandle());
		
		cube.setLocation(x, y, z, 0, 0);
		
		cube.setFlag(5, true);//invisible
		cube.setFlag(6, true);//glowing
		
		player.getHandle().playerConnection.sendPacket(new PacketPlayOutSpawnEntityLiving(cube));
		
		return cube;
	}
	
	
	public void removeMarkersFromPlayer(Player p){
		if(playerMap.containsKey(p.getUniqueId())){
			MarkedTask t = playerMap.get(p.getUniqueId());
			plugin.getServer().getScheduler().cancelTask(t.taskId);
			removeEntitys(p, t.ids);
		}
	}
	public void onDisable(){
		for(Map.Entry<UUID, MarkedTask> playerEntry:playerMap.entrySet()){
			plugin.getServer().getScheduler().cancelTask(playerEntry.getValue().taskId);
			removeEntitys(plugin.getServer().getPlayer(playerEntry.getKey()), playerEntry.getValue().ids);
		}
	}
	public void onPlayerLeave(UUID player){
		plugin.getServer().getScheduler().cancelTask(playerMap.remove(player).taskId);
	}
	private void removeEntitys(Player p, int[] ids){
		((CraftPlayer) p).getHandle().playerConnection.sendPacket(new PacketPlayOutEntityDestroy(ids));
	}
	private static class MarkedTask{
		public int taskId;
		public int[] ids;
		public MarkedTask(int taskId, int[] ids){
			this.taskId = taskId;
			this.ids = ids;
		}
	}
}
