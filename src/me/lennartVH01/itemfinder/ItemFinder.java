package me.lennartVH01.itemfinder;

import java.util.logging.Level;

import org.bukkit.plugin.java.JavaPlugin;


public class ItemFinder extends JavaPlugin{
	private BlockMarker blockMarker;
	private PermissionChecker checker;
	private Metrics metrics;
	
	private static final String MC_VERSION = "1.12";
	
	@Override public void onEnable(){
		saveDefaultConfig();
		
		Config.reload(getConfig());
		
		try{
			blockMarker = new BuiltinBlockMarker(this);
		}catch(NoClassDefFoundError ex){
			getLogger().log(Level.SEVERE, "Incompatible Minecraft version for ItemFinder-"+MC_VERSION+", entering Compatibility Mode!");
			getLogger().log(Level.SEVERE, "Download the latest version at https://github.com/lennartVH01/ItemFinder/releases");
			blockMarker = new CompatibilityBlockMarker();
		}
		
		checker = new PermissionChecker(this);
		
		FindCommand findcmd = new FindCommand(blockMarker, checker);
		
		getCommand("find").setExecutor(findcmd);
		getCommand("find").setTabCompleter(findcmd);
		
		//Enable metrics
		metrics = new Metrics(this);
		
		metrics.addCustomChart(new Metrics.SingleLineChart("find_freq", () -> {
			int curFreq = findcmd.currentFindCount;
			findcmd.currentFindCount -= curFreq;		// subtract from itself, should set it to 0, 
			return curFreq;								// but I don't know threads so this should be the most safe
		}));
	}
	
	@Override public void onDisable(){
		blockMarker.onDisable();
	}
}
