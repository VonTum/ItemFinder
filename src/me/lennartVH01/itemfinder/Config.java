package me.lennartVH01.itemfinder;

import org.bukkit.configuration.Configuration;

public class Config {
	public static int DEFAULT_SEARCH_RADIUS;
	public static int MAX_SEARCH_RADIUS;
	
	public static int MARKER_TIMEOUT;
	
	public static boolean SEARCH_SHULKERS_RECURSIVELY;
	
	public static void reload(Configuration config){
		Messages.reload(config);
		
		DEFAULT_SEARCH_RADIUS = config.getInt("default_search_radius", 20);
		MAX_SEARCH_RADIUS = config.getInt("max_search_radius", 50);
		
		MARKER_TIMEOUT = config.getInt("marker_timeout", 500);
		
		SEARCH_SHULKERS_RECURSIVELY = config.getBoolean("search_shulkerbox_recursively", true);
	}
	
	public static class Messages {
		public static String ERROR_UNKNOWN_MATERIAL;
		public static String ERROR_MUST_BE_INGAME;
		public static String INFO_SEARCH;
		public static String INFO_FOUND_TOTAL;
		public static String INFO_FOUND_CONTAINER;
		public static String INFO_FOUND_FLOOR;
		public static String INFO_FOUND_PLAYER;
		public static String INFO_FOUND_ENDER;
		public static String INFO_FOUND_NOTHINGFOUND;
		
		public static void reload(Configuration config){
			ERROR_UNKNOWN_MATERIAL = config.getString("messages.error.unknown_material", 	"Material %s does not exist.");
			ERROR_MUST_BE_INGAME = config.getString("messages.error.must_be_ingame", 		"You must be ingame to use this command");
			
			INFO_SEARCH = config.getString("messages.info.search", 							"Searching for %s in a radius of %d blocks.");
			INFO_FOUND_TOTAL = config.getString("messages.info.found.total", 				"Found %d items in total: ");
			INFO_FOUND_CONTAINER = config.getString("messages.info.found.container", 		"Chests: %d");
			INFO_FOUND_PLAYER = config.getString("messages.info.found.player", 				"Inventory: %d");
			INFO_FOUND_ENDER = config.getString("messages.info.found.ender", 				"EnderChest: %d");
			
			INFO_FOUND_NOTHINGFOUND = config.getString("messages.info.found.nothingFound", 	"%s could not be found.");
		}
	}
}