package main;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.Dispenser;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryInteractEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;


public class InventoryManager implements Listener {

	static HashMap<Integer, ArrayList<ItemStack>> storageCells = new HashMap<>();
	static HashMap<UUID, Integer> playerScrolls = new HashMap<>();
	static HashMap<UUID, Boolean> playerReversions = new HashMap<>();
	
	public static int getPlayerScroll(Player p) {
		if(playerScrolls.containsKey(p.getUniqueId()))
			return playerScrolls.get(p.getUniqueId());
		playerScrolls.put(p.getUniqueId(), 0);
		return 0;
	}
	public static boolean getPlayerReversion(Player p) {
		if(playerReversions.containsKey(p.getUniqueId()))
			return playerReversions.get(p.getUniqueId());
		playerReversions.put(p.getUniqueId(), false);
		return false;
	}
	
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
				if(check.getAmount()<removeAmount)
					removeAmount = check.getAmount();
				int nextAmount = check.getAmount()-removeAmount;
				before.get(entryID).setAmount(nextAmount);
				removedStack = new ItemBuilder(check).setAmount(removeAmount).build();
				if(nextAmount<=0)
					before.remove(entryID);
					
				
			}
		}
		storageCells.put(cellID, before);
		return removedStack;
	}
	
	public static Inventory openExtractionInventory(int storageCellID, int scrolloffset, boolean reversion) {
		
		Inventory inv = Bukkit.createInventory(null, 6*9, ChatColor.DARK_PURPLE+"Extraction ("+storageCellID+")");
		//inv.setMaxStackSize(99999);
		
		ArrayList<ItemStack> items = (ArrayList<ItemStack>) storageCells.get(storageCellID).clone();
		if(!reversion)
			items.sort((o1, o2) -> {return o2.getAmount()-o1.getAmount();});
		else
			items.sort((o1, o2) -> {return o1.getAmount()-o2.getAmount();});
		
		for(int i = 0; i<scrolloffset*9; i++) {
			if(items.size()>0)
				items.remove(0);
		}
		
		for(ItemStack ciso : items) {
			ItemStack cis = ciso.clone();
			inv.addItem(new ItemBuilder(cis).setLore("Amount: "+cis.getAmount()).setAmount(1).build());
		}
		
		for(int i = 45; i<53; i++)
			inv.setItem(i, new ItemBuilder(Material.WHITE_STAINED_GLASS_PANE, 1).setDisplayname(ChatColor.RED+"x").build());
		
		//inv.setItem(44, new ItemBuilder(Main.createCustomTextureSkull("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNmNjYmY5ODgzZGQzNTlmZGYyMzg1YzkwYTQ1OWQ3Mzc3NjUzODJlYzQxMTdiMDQ4OTVhYzRkYzRiNjBmYyJ9fX0=")).setDisplayname(ChatColor.DARK_PURPLE+"Up  ("+scrolloffset+")").build());
		inv.setItem(52, new ItemBuilder(Main.createCustomTextureSkull("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzI0MzE5MTFmNDE3OGI0ZDJiNDEzYWE3ZjVjNzhhZTQ0NDdmZTkyNDY5NDNjMzFkZjMxMTYzYzBlMDQzZTBkNiJ9fX0=")).setDisplayname(ChatColor.DARK_PURPLE+"Down ("+scrolloffset+")").build());
		inv.setItem(53, new ItemBuilder(Main.createCustomTextureSkull("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNmNjYmY5ODgzZGQzNTlmZGYyMzg1YzkwYTQ1OWQ3Mzc3NjUzODJlYzQxMTdiMDQ4OTVhYzRkYzRiNjBmYyJ9fX0=")).setDisplayname(ChatColor.DARK_PURPLE+"Up  ("+scrolloffset+")").build());
		inv.setItem(45, new ItemBuilder(Main.createCustomTextureSkull("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNDZiYTYzMzQ0ZjQ5ZGQxYzRmNTQ4OGU5MjZiZjNkOWUyYjI5OTE2YTZjNTBkNjEwYmI0MGE1MjczZGM4YzgyIn19fQ==")).setDisplayname(ChatColor.DARK_PURPLE+"Search").build());
		inv.setItem(49, new ItemBuilder(Main.createCustomTextureSkull("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYTZjYzQ4NmMyYmUxY2I5ZGZjYjJlNTNkZDlhM2U5YTg4M2JmYWRiMjdjYjk1NmYxODk2ZDYwMmI0MDY3In19fQ==")).setDisplayname(ChatColor.DARK_PURPLE+"Enderchest").build());
		inv.setItem(50, new ItemBuilder(Main.createCustomTextureSkull("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMmNkYzBmZWI3MDAxZTJjMTBmZDUwNjZlNTAxYjg3ZTNkNjQ3OTMwOTJiODVhNTBjODU2ZDk2MmY4YmU5MmM3OCJ9fX0=")).setDisplayname(ChatColor.DARK_PURPLE+"Crafting Table").build());
		
		
		if(!reversion)
			inv.setItem(47, new ItemBuilder(Main.createCustomTextureSkull("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNjgyYWQxYjljYjRkZDIxMjU5YzBkNzVhYTMxNWZmMzg5YzNjZWY3NTJiZTM5NDkzMzgxNjRiYWM4NGE5NmUifX19")).setDisplayname(ChatColor.DARK_PURPLE+"Sort left").build());
		else
			inv.setItem(47, new ItemBuilder(Main.createCustomTextureSkull("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMzdhZWU5YTc1YmYwZGY3ODk3MTgzMDE1Y2NhMGIyYTdkNzU1YzYzMzg4ZmYwMTc1MmQ1ZjQ0MTlmYzY0NSJ9fX0=")).setDisplayname(ChatColor.DARK_PURPLE+"Sort right").build());
		
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
			
			if(e.getCurrentItem()==null) return;
			
			if(e.getCurrentItem().getItemMeta().getDisplayName().startsWith(ChatColor.DARK_PURPLE+"Up")) {
				p.playSound(p.getLocation(), Sound.BLOCK_LEVER_CLICK, 1, 1);
				if(e.getClick() == ClickType.LEFT) 
					if(playerScrolls.get(p.getUniqueId())>0)
						playerScrolls.put(p.getUniqueId(), playerScrolls.get(p.getUniqueId())-1);
				if(e.getClick() == ClickType.SHIFT_LEFT) 
					for(int i = 0; i<5;i++)
						if(playerScrolls.get(p.getUniqueId())>0)
							playerScrolls.put(p.getUniqueId(), playerScrolls.get(p.getUniqueId())-1);
				if(e.getClick() == ClickType.RIGHT)
					for(int i = 0; i<50;i++)
						if(playerScrolls.get(p.getUniqueId())>0)
							playerScrolls.put(p.getUniqueId(), playerScrolls.get(p.getUniqueId())-1);
				
				p.openInventory(openExtractionInventory(id, getPlayerScroll(p), getPlayerReversion(p)));
				return;
			}
			if(e.getCurrentItem().getItemMeta().getDisplayName().startsWith(ChatColor.DARK_PURPLE+"Down")) {
				p.playSound(p.getLocation(), Sound.BLOCK_LEVER_CLICK, 1, 1);
				if(e.getClick() == ClickType.LEFT)
					if(storageCells.get(id).size()>(playerScrolls.get(p.getUniqueId())+3)*9)
						playerScrolls.put(p.getUniqueId(), playerScrolls.get(p.getUniqueId())+1);
				if(e.getClick() == ClickType.SHIFT_LEFT)
					for(int i = 0; i<5;i++)
						if(storageCells.get(id).size()>(playerScrolls.get(p.getUniqueId())+3)*9)
							playerScrolls.put(p.getUniqueId(), playerScrolls.get(p.getUniqueId())+1);
				if(e.getClick() == ClickType.RIGHT)
					for(int i = 0; i<50;i++)
						if(storageCells.get(id).size()>(playerScrolls.get(p.getUniqueId())+3)*9)
							playerScrolls.put(p.getUniqueId(), playerScrolls.get(p.getUniqueId())+1);
				
				p.openInventory(openExtractionInventory(id, getPlayerScroll(p), getPlayerReversion(p)));
				return;
			}
			if(e.getCurrentItem().getItemMeta().getDisplayName().startsWith(ChatColor.DARK_PURPLE+"Sort")) {
				p.playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_CHAIN, 1, 1);
				playerReversions.put(p.getUniqueId(), !playerReversions.get(p.getUniqueId()));
				p.openInventory(openExtractionInventory(id, getPlayerScroll(p), getPlayerReversion(p)));
			}
			
			
			if(e.getCurrentItem().getItemMeta().getDisplayName().startsWith(ChatColor.DARK_PURPLE+"Search")) {
				p.openInventory(openExtractionInventory(id, getPlayerScroll(p), getPlayerReversion(p)));
				p.playSound(p.getLocation(), Sound.BLOCK_LEVER_CLICK, 1, 1);
				p.sendMessage(ChatColor.RED+"Search function under development... ");
			}
			
			if(e.getCurrentItem().getItemMeta().getDisplayName().startsWith(ChatColor.DARK_PURPLE+"Crafting Table")) {
				p.playSound(p.getLocation(), Sound.BLOCK_LEVER_CLICK, 1, 1);
				p.openWorkbench(null, true);
			}
			
			if(e.getCurrentItem().getItemMeta().getDisplayName().startsWith(ChatColor.DARK_PURPLE+"Enderchest")) {
				p.playSound(p.getLocation(), Sound.BLOCK_LEVER_CLICK, 1, 1);
				p.openInventory(p.getEnderChest());
			}
			
			
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
			ItemStack result = consumeItemStackFromStorageCell(id, e.getCurrentItem(), takeamount);
			//System.out.println("Result: "+result);
			if(result != null) {
				p.playSound(p.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_OFF, 1, 1);
				p.getInventory().addItem(result);
				p.openInventory(openExtractionInventory(id, getPlayerScroll(p), getPlayerReversion(p)));
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
