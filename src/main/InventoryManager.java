package main;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.block.Block;
import org.bukkit.block.Dispenser;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.tags.CustomItemTagContainer;
import org.bukkit.persistence.PersistentDataContainer;

import com.google.common.collect.Multimap;

import net.minecraft.world.item.Items;


public class InventoryManager implements Listener {

	static HashMap<Integer, ArrayList<ItemStack>> storageCells = new HashMap<>();
	
	public static int validateBaseBlock(Block toValidate) {
		if(toValidate.getType() == Material.DISPENSER) {
    		Dispenser disp = (Dispenser) toValidate.getState();
    		ItemStack storageDisk = disp.getInventory().getItem(4);
    		if(storageDisk != null) {
    			if(storageDisk.getType() == Material.MUSIC_DISC_PIGSTEP) {
    				if(!storageDisk.getItemMeta().getDisplayName().startsWith(ChatColor.DARK_PURPLE+"STORAGECELL")) {
    					int nextID = InventoryManager.storageCells.size();
    					System.out.println("Creating Storage Space "+nextID);
    					ItemMeta diskMeta = storageDisk.getItemMeta();
    					diskMeta.setDisplayName(ChatColor.DARK_PURPLE+"STORAGECELL"+nextID);
    					InventoryManager.storageCells.put(nextID, new ArrayList<>());
    					storageDisk.setItemMeta(diskMeta);
    				}
    				
    				int id = Integer.valueOf(storageDisk.getItemMeta().getDisplayName().replace(ChatColor.DARK_PURPLE+"STORAGECELL", ""));
    				if(InventoryManager.storageCells.get(id) == null)
    					InventoryManager.storageCells.put(id, new ArrayList<>());
    				return id;
    				
    			}
    		}
    			
    	}
		return -1;
	}
	
	static void addItemStackToStorageCell(int cellID, ItemStack toAdd) {
		//System.out.println("Adding "+toAdd+" to Storage Cell Numaber "+cellID);
		ArrayList<ItemStack> before = storageCells.get(cellID);
		//if(!toAdd.hasItemMeta())
		boolean hasBeenAdded = false;
		for(ItemStack check : before) {
			if(check.isSimilar(toAdd)) {
				check.setAmount(check.getAmount()+toAdd.getAmount());
				hasBeenAdded = true;
			}
		}
		if(!hasBeenAdded)
			before.add(toAdd);
		storageCells.put(cellID, before);
	}
	static ItemStack consumeItemStackFromStorageCell(int cellID, ItemStack toRemove, int removeAmount) {
		toRemove = new ItemBuilder(toRemove).setLore().build();
		//System.out.println("Removing "+toRemove+" from Storage Cell Numaber "+cellID);
		ArrayList<ItemStack> before = storageCells.get(cellID);
		ItemStack removedStack = null;
		for(int entryID = 0; entryID<before.size();entryID++) {
			ItemStack check = new ItemBuilder(before.get(entryID)).build();
			//System.out.println("checking "+check+" ...");
			if(check.isSimilar(toRemove)) {
				//System.out.println("found! --> Amountchecks: "+check.getAmount()+"|"+removeAmount);
				if(check.getAmount()>=removeAmount) {
					int nextAmount = check.getAmount()-removeAmount;
					before.get(entryID).setAmount(nextAmount);
					removedStack = new ItemBuilder(check).setAmount(removeAmount).build();
					if(nextAmount<=0)
						before.remove(entryID);
					
				}
				
			}
		}
		storageCells.put(cellID, before);
		return removedStack;
	}
	
	public static Inventory openExtractionInventory(int storageCellID) {
		
		Inventory inv = Bukkit.createInventory(null, 6*9, ChatColor.DARK_PURPLE+"Extraction ("+storageCellID+")");
		//inv.setMaxStackSize(99999);
		
		ArrayList<ItemStack> items = storageCells.get(storageCellID);
		items.sort((o1, o2) -> {return o2.getAmount()-o1.getAmount();});
		
		for(ItemStack ciso : items) {
			ItemStack cis = ciso.clone();
			inv.addItem(new ItemBuilder(cis).setLore("Amount: "+cis.getAmount()).setAmount(1).build());
		}
		
		return inv;
		
	}
	
	public static Inventory openInsertionInventory(int storageCellID) {
		Inventory inv = Bukkit.createInventory(null, 6*9, ChatColor.DARK_PURPLE+"Insertion ("+storageCellID+")");
		return inv;
	}
	
	
	
	@EventHandler
	public void onPlayerInventoryInteract(InventoryInteractEvent e) {
		
	}
	
	@EventHandler
	public void onPlayerInventoryClick(InventoryClickEvent e) {
		Player p = (Player) e.getWhoClicked();
		if(e.getView().getTitle().startsWith(ChatColor.DARK_PURPLE+"Extraction")) {
			int id = Integer.valueOf(e.getView().getTitle().split(" ")[1].replace("(", "").replace(")", ""));
			e.setCancelled(true);
			
			
			int takeamount = 0;
			if(e.getClick() == ClickType.LEFT) {
				takeamount = 1;
			}else if(e.getClick() == ClickType.SHIFT_LEFT) {
				takeamount = 64;
			}else if(e.getClick() == ClickType.RIGHT) {
				takeamount = 32;
			}else if(e.getClick() == ClickType.SHIFT_RIGHT) {
				takeamount = 16;
			}
			if(e.getCurrentItem()!=null) {
				ItemStack result = consumeItemStackFromStorageCell(id, e.getCurrentItem(), takeamount);
				//System.out.println("Result: "+result);
				if(result != null) {
					p.getInventory().addItem(result);
					p.openInventory(openExtractionInventory(id));
				}
			}
			
			
		}
		
	}
	
	@EventHandler
	public void onInventoryClose(InventoryCloseEvent e) {
		//System.out.println(e.getView().getTitle());
		if(e.getView().getTitle().split(" ")[0].equalsIgnoreCase(ChatColor.DARK_PURPLE+"Insertion")) {
			int id = Integer.valueOf(e.getView().getTitle().split(" ")[1].replace("(", "").replace(")", ""));
			for(ItemStack cis : e.getInventory().getContents()) {
				if(cis!=null)
					addItemStackToStorageCell(id, cis);
			}
			e.getInventory().clear();
		}
		
	}
	
	
}
