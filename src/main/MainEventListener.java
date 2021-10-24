package main;

import java.util.ArrayList;

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
	
}
