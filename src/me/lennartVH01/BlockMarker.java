package me.lennartVH01;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import net.minecraft.server.v1_11_R1.EntityMagmaCube;
import net.minecraft.server.v1_11_R1.PacketPlayOutEntityDestroy;
import net.minecraft.server.v1_11_R1.PacketPlayOutSpawnEntityLiving;

import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_11_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_11_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class BlockMarker {
	private static HashMap<UUID, MarkedTask> playerMap = new HashMap<UUID, MarkedTask>();
	public static void markBlocks(Plugin plugin, Player p, List<Location> blocks){
		int[] cubeIds = new int[blocks.size()];
		for(int i = 0; i < blocks.size(); i++){
			EntityMagmaCube cube = new EntityMagmaCube(((CraftWorld) p.getWorld()).getHandle());
			cube.setLocation(blocks.get(i).getX()+0.5, blocks.get(i).getY()+0.25, blocks.get(i).getZ()+0.5, 0, 0);
			
			cube.setFlag(5, true);//invisible
			cube.setFlag(6, true);//glowing
			
			((CraftPlayer) p).getHandle().playerConnection.sendPacket(new PacketPlayOutSpawnEntityLiving(cube));
			cubeIds[i] = cube.getId();
		}
		int taskId = plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
			@Override public void run() {
				playerMap.remove(p.getUniqueId());
				if(p != null && p.isOnline()){
					removeEntitys(cubeIds, p);
				}
			}
		}, plugin.getConfig().getLong("marker_timeout", 500));
		playerMap.put(p.getUniqueId(), new MarkedTask(taskId, cubeIds));
	}
	public static void cleanUpPlayer(UUID playerId){
		playerMap.remove(playerId);
	}
	public static void removeMarkersFromPlayer(Plugin plugin, Player p){
		if(playerMap.containsKey(p.getUniqueId())){
			MarkedTask t = playerMap.get(p.getUniqueId());
			plugin.getServer().getScheduler().cancelTask(t.taskId);
			removeEntitys(t.ids, p);
		}
	}
	private static void removeEntitys(int[] ids, Player p){
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
