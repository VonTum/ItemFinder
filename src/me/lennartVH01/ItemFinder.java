package me.lennartVH01;

import org.bukkit.plugin.java.JavaPlugin;


public class ItemFinder extends JavaPlugin{
	private BlockMarker blockMarker;
	@Override public void onEnable(){
		saveDefaultConfig();
		
		blockMarker = new BlockMarker(this);
		
		FindCommand findcmd = new FindCommand(this, blockMarker);
		
		getCommand("find").setExecutor(findcmd);
		getCommand("find").setTabCompleter(findcmd);
	}
	
	@Override public void onDisable(){
		blockMarker.onDisable();
	}
}
