package main;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.Dispenser;
import org.bukkit.block.Sign;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class MainEventListener implements Listener {

	static String locationToString(Location input) {
		return "["+input.getX()+"|"+input.getY()+"|"+input.getZ()+"]";
	}
	
	@EventHandler
	public void onPlayerOpenStoragePanel(PlayerInteractEvent e) {
		if(e.getClickedBlock() == null)
			return;
		//try 
		{
		 if(e.getAction() == Action.RIGHT_CLICK_BLOCK){
			 if(String.valueOf(e.getClickedBlock().getType()).contains("SIGN")){
                Block block = e.getClickedBlock();
                if (block != null && block.getState() instanceof Sign)
                {
                    BlockData data = block.getBlockData();
                    if (data instanceof Directional)
                    {
                        Directional directional = (Directional)data;
                        Block blockBehind = block.getRelative(directional.getFacing().getOppositeFace());
                        Location attachloc = blockBehind.getLocation();
                        
                        
                        	Block after = attachloc.getWorld().getBlockAt(attachloc);
                        	int id = InventoryManager.validateBaseBlock(after);
                        	if(id!=-1) {
                        		Player p = e.getPlayer();
                        		p.playSound(p.getLocation(), Sound.BLOCK_IRON_DOOR_OPEN, 1, 1);
                				p.openInventory(InventoryManager.openExtractionInventory(id, InventoryManager.getPlayerScroll(p), InventoryManager.getPlayerReversion(p)));
                				e.setCancelled(true);
                        	}
                        
                    }
                }
            }
			if(String.valueOf(e.getClickedBlock().getType()).contains("TRIPWIRE_HOOK")){
                Block block = e.getClickedBlock();
                    BlockData data = block.getBlockData();
                    if (data instanceof Directional)
                    {
                        Directional directional = (Directional)data;
                        Block blockBehind = block.getRelative(directional.getFacing().getOppositeFace());
                        Location attachloc = blockBehind.getLocation();
                        
                    	Block after = attachloc.getWorld().getBlockAt(attachloc);
                    	int id = InventoryManager.validateBaseBlock(after);
                    	if(id!=-1) {
                    		Player p = e.getPlayer();
            				if(p.getInventory().getItemInMainHand().getType() == Material.MUSIC_DISC_PIGSTEP) {
            					p.playSound(p.getLocation(), Sound.BLOCK_ANVIL_LAND, 0.1f, 1);
            					//p.sendMessage(ChatColor.GREEN+"StorageDisk successfully overwritten!");
            					p.setItemInHand(new ItemBuilder(p.getInventory().getItemInMainHand()).setDisplayname(ChatColor.DARK_PURPLE+"STORAGECELL"+id).build());
            				}else
            					//p.sendMessage(ChatColor.RED+"You have to hold a storage Disk!");
            				e.setCancelled(true);
                    	}
            }
            }
			 else if(e.getClickedBlock().getType()==Material.IRON_TRAPDOOR){
				 {
                 	Block after = e.getClickedBlock().getWorld().getBlockAt(e.getClickedBlock().getLocation().add(0, -1, 0));
     				int id = InventoryManager.validateBaseBlock(after);
                	if(id!=-1) {
                		Player p = e.getPlayer();
                		p.playSound(p.getLocation(), Sound.BLOCK_IRON_DOOR_OPEN, 1, 1);
         				p.openInventory(InventoryManager.openInsertionInventory(id));
         				e.setCancelled(true);
                	}
     				
                 
             }
	               
	          }
        }
	}//catch (Exception exc) { Bukkit.broadcastMessage(exc.getLocalizedMessage()); }
	}
	
	
	@EventHandler
	public void onHopperMoveItemEvent(InventoryMoveItemEvent e) {
		//trying to check if inventory is owned by a dispenser but dropper has the same amount of slots
		if(e.getDestination().getSize()!=9)
			return;
		ItemStack storageDisk = e.getDestination().getItem(4);
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
				
				Bukkit.getScheduler().scheduleSyncDelayedTask(Main.plugin, ()->{
					e.getDestination().remove(e.getItem());
					InventoryManager.addItemStackToStorageCell(id, e.getItem().clone());
				});
				
				
				
				
			}
		}
	}
	
}
