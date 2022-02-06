package main;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Random;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;

public class Main extends JavaPlugin{

	
	
	public static JavaPlugin plugin;
	
	  public static ItemStack createCustomTextureSkull(String url) {
	        ItemStack head = new ItemStack(Material.PLAYER_HEAD, 1);
	        if (url.isEmpty())
	            return head;

	        SkullMeta headMeta = (SkullMeta) head.getItemMeta();
	        GameProfile profile = new GameProfile(UUID.randomUUID(), null);

	        profile.getProperties().put("textures", new Property("textures", url));

	        try {
	            Field profileField = headMeta.getClass().getDeclaredField("profile");
	            profileField.setAccessible(true);
	            profileField.set(headMeta, profile);

	        } catch (IllegalArgumentException | NoSuchFieldException | SecurityException | IllegalAccessException error) {
	            error.printStackTrace();
	        }
	        head.setItemMeta(headMeta);
	        return head;
	    }
	
	@Override
	public void onEnable() {
		System.out.println("Advanced Storage enabled!");
		plugin = this;
		
		
		NamespacedKey mhkey = new NamespacedKey(this, "mining_head");
		ShapedRecipe miningHeadRecipe = new ShapedRecipe(mhkey, new ItemBuilder(Material.GOLDEN_PICKAXE, 1).setDisplayname(ChatColor.GREEN+"MiningHead").addEnchantment(Enchantment.THORNS, 1).build());
		miningHeadRecipe.shape("EEE", "OSO", "OSO");
		miningHeadRecipe.setIngredient('S', Material.BLAZE_ROD);
		miningHeadRecipe.setIngredient('E', Material.EMERALD);
		
		Bukkit.addRecipe(miningHeadRecipe);
		
		
		NamespacedKey rmkey = new NamespacedKey(this, "range_modifier");
		ShapedRecipe rangeModRecipe = new ShapedRecipe(rmkey, new ItemBuilder(Material.GLASS_BOTTLE, 1).setDisplayname(ChatColor.GREEN+"RangeModifier").addEnchantment(Enchantment.THORNS, 1).build());
		rangeModRecipe.shape("CCC", "YPB", "CCC");
		rangeModRecipe.setIngredient('Y', Material.YELLOW_STAINED_GLASS_PANE);
		rangeModRecipe.setIngredient('B', Material.BLUE_STAINED_GLASS_PANE);
		rangeModRecipe.setIngredient('C', Material.COPPER_BLOCK);
		rangeModRecipe.setIngredient('P', Material.PHANTOM_MEMBRANE);
		Bukkit.addRecipe(rangeModRecipe);
		
		
		NamespacedKey dkey = new NamespacedKey(this, "storage_disc");
		ShapedRecipe discRecipe = new ShapedRecipe(dkey, new ItemBuilder(Material.MUSIC_DISC_PIGSTEP, 1).setDisplayname("EmptyStorageDisk").build());
		discRecipe.shape("RER", "GBG", "RER");
		discRecipe.setIngredient('E', Material.IRON_BLOCK);
		discRecipe.setIngredient('R', Material.REDSTONE_BLOCK);
		discRecipe.setIngredient('G', Material.GOLD_BLOCK);
		discRecipe.setIngredient('B', Material.LEGACY_BOOK_AND_QUILL);
		Bukkit.addRecipe(discRecipe);
		
		
		
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
