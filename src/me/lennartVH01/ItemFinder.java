package me.lennartVH01;

import org.bukkit.plugin.java.JavaPlugin;


public class ItemFinder extends JavaPlugin{
	@Override public void onEnable(){
		saveDefaultConfig();
		
		FindCommand findcmd = new FindCommand(this);
		
		getCommand("find").setExecutor(findcmd);
		getCommand("find").setTabCompleter(findcmd);
	}
	
	@Override public void onDisable(){
		
	}
}
