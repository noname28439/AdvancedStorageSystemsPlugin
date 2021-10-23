package main;

import java.util.ArrayList;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin{

	public static JavaPlugin plugin;
	
	@Override
	public void onEnable() {
		System.out.println("Advanced Storage enabled!");
		plugin = this;
		
		if(plugin.getConfig().isSet("STORAGE_CONTAINERS"))
		for(String cid : plugin.getConfig().getConfigurationSection("STORAGE_CONTAINERS").getKeys(false)) {
			ArrayList<ItemStack> loaded = loadInv("STORAGE_CONTAINERS."+cid);
			int id = Integer.valueOf(cid);
			InventoryManager.storageCells.put(id, loaded);
		}
		
		
		PluginManager pm = Bukkit.getPluginManager();
		pm.registerEvents(new MainEventListener(), this);
		pm.registerEvents(new InventoryManager(), this);
		
	}
	
	@Override
	public void onDisable() {
		plugin.getConfig().set("RANDID", new Random().nextInt(100));
		plugin.saveConfig();
		System.out.println("InventoryManager found!");
		for(int cid : InventoryManager.storageCells.keySet())
			saveInv("STORAGE_CONTAINERS."+cid, InventoryManager.storageCells.get(cid));
	}
	
	
	
	public static void saveInv(String path, ArrayList<ItemStack> itemStacks) {
		plugin.getConfig().set(path, itemStacks);
		plugin.saveConfig();
	}
	
	public static ArrayList<ItemStack> loadInv(String configurationPath) {
		if(plugin.getConfig().isSet(configurationPath)) {
			return (ArrayList<ItemStack>) plugin.getConfig().getList(configurationPath);
		}
		System.out.println("Error: Could not load Inventory from "+configurationPath);
		return null;
	}
	
}