package main;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Dispenser;
import org.bukkit.block.Sign;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import net.md_5.bungee.api.chat.hover.content.Item;
import net.minecraft.world.entity.animal.EntityFox.i;
import net.minecraft.world.inventory.Container;

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
		if(e.getItem().getItemMeta().getDisplayName().equals(ChatColor.GREEN+"MiningHead") ||
			e.getItem().getItemMeta().getDisplayName().equals(ChatColor.GREEN+"RangeModifier"))
			e.setCancelled(true);
		
		
		//Item extracted from StorageSystem
		Location invloc = e.getInitiator().getLocation();
		Location destloc = e.getDestination().getLocation();
		if(invloc==null) return;
		if(destloc==null) return;
		Block destinationBlock = destloc.getBlock();
		Block initiatiorBlock = invloc.getBlock();
		if(initiatiorBlock.getType() == Material.DROPPER) {
			int storageSystemID = InventoryManager.validateBaseBlock(initiatiorBlock.getRelative(0, 1, 0));
			if(storageSystemID == -1) return;
			e.setCancelled(true);
			if(e.getSource().getItem(4) == null) return;
			ItemStack result = InventoryManager.consumeItemStackFromStorageCell(storageSystemID, e.getSource().getItem(4), 1);
			if(result==null) {
				invloc.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, invloc.clone().add(0.5, 0.5, 0.5), 10);
				return;
			}
			e.getDestination().addItem(result);
		}
		
		
		if(destinationBlock.getType() == Material.DISPENSER) {
			//Item sorted into StorageSystem
			ItemStack centerItem = e.getDestination().getItem(4);
			if(centerItem != null) {
				if(centerItem.getType() == Material.MUSIC_DISC_PIGSTEP) {
					if(!centerItem.getItemMeta().getDisplayName().startsWith(ChatColor.DARK_PURPLE+"STORAGECELL")) {
						int nextID = InventoryManager.storageCells.size();
						System.out.println("Creating Storage Space "+nextID);
						ItemMeta diskMeta = centerItem.getItemMeta();
						diskMeta.setDisplayName(ChatColor.DARK_PURPLE+"STORAGECELL"+nextID);
						InventoryManager.storageCells.put(nextID, new ArrayList<>());
						centerItem.setItemMeta(diskMeta);
					}
					
					int id = Integer.valueOf(centerItem.getItemMeta().getDisplayName().replace(ChatColor.DARK_PURPLE+"STORAGECELL", ""));
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
	
	
	@EventHandler
    public void onDispenseItem(BlockDispenseEvent e) {//WARNING CRAPPY CODE!
		Block initiatiorBlock = e.getBlock();
		if(initiatiorBlock==null) return;
	    if(initiatiorBlock.getType() == Material.DISPENSER) {
	    	Inventory dispenserInv = ((InventoryHolder)(initiatiorBlock.getState())).getInventory();
	    	ItemStack baseItem = dispenserInv.getItem(4);
	    	ItemStack modifier = dispenserInv.getItem(7);
	    	if(e.getItem().getItemMeta().getDisplayName().equals(ChatColor.GREEN+"MiningHead") || baseItem.getItemMeta().getDisplayName().equals(ChatColor.GREEN+"MiningHead")) {
				e.setCancelled(true);
				int rad = 0;
				
				if(modifier==null) {
					if(e.getItem().getItemMeta().getDisplayName().equals(ChatColor.GREEN+"RangeModifier"))
						rad+=e.getItem().getAmount();
				}else if(modifier.getItemMeta().getDisplayName().equals(ChatColor.GREEN+"RangeModifier"))
					rad+=modifier.getAmount();
					
				if(rad>5)
					rad=5;
				
				if (initiatiorBlock.getBlockData() instanceof Directional) {
					Block toBreak = null;
				    BlockFace facing = ((Directional) initiatiorBlock.getBlockData()).getFacing();
				    int dist = 0;
				    mainloop: while(dist++<=30) {
				    	for(int xof = -rad; xof<=rad; xof++) {
				    		for(int yof = -rad; yof<=rad; yof++) {
				    			for(int zof = -rad; zof<=rad; zof++) {
						    		int xofs = xof;
						    		int yofs = yof;
						    		int zofs = zof;
						    		if(facing.getModX()!=0) {xof=rad+999; xofs = 0;}
						    		if(facing.getModY()!=0) {yof=rad+999; yofs = 0;}
						    		if(facing.getModZ()!=0) {zof=rad+999; zofs = 0;}
						    		
						    		Block currentCheck = initiatiorBlock.getRelative(facing, dist).getRelative(xofs, yofs, zofs);
							    	//System.out.println(locationToString(currentCheck.getLocation())+" --> "+currentCheck.getType());
					    			if(currentCheck.getType() == Material.REDSTONE_WIRE ||
					    			currentCheck.getType() == Material.BEDROCK ||
					    			currentCheck.getType() == Material.BARRIER ||
			    					currentCheck.getType() == Material.TORCH ||
			    					currentCheck.getType() == Material.CHEST ||
			    					currentCheck.getType() == Material.BARREL ||
			    					currentCheck.getType() == Material.GLASS ||
									currentCheck.getType() == Material.AIR ||
									currentCheck.isLiquid()) {
					    				continue;	//Invalid block --> Ignore
					    			}else {
					    				toBreak = currentCheck;
					    				break mainloop;
					    			}
						    		
						    	}
					    	}
				    	}
				    	
		    			
				    	
				    }
				    	
				    
				    Block container = initiatiorBlock.getRelative(facing.getOppositeFace());
				    if(toBreak == null) return;	//No Block in Range
				    if(container == null) return; //No container behind
				    
			    	
				    Collection<ItemStack> drops = toBreak.getDrops();
				    toBreak.setType(Material.AIR);
				    
				    toBreak.getWorld().spawnParticle(Particle.EXPLOSION_NORMAL, toBreak.getLocation().clone().add(0.5, 0.5, 0.5), 1, 0, 0, 0, 0);
				    for(int i = 0; i<3; i++) {
				    	Random rand = new Random();
				    	Location inBLoc = toBreak.getLocation().clone().add(Float.valueOf(rand.nextInt(100))/100, Float.valueOf(rand.nextInt(100))/100, Float.valueOf(rand.nextInt(100))/100);
				    	if(rand.nextInt(3)==0)
				    		toBreak.getWorld().spawnParticle(Particle.DRIP_WATER, inBLoc, 10);
				    	else
				    		toBreak.getWorld().spawnParticle(Particle.DRIP_LAVA, inBLoc, 10);
				    }
				    
				    if(container.getState() instanceof InventoryHolder) {
				    	Inventory blockinv = ((InventoryHolder)(container.getState())).getInventory();
					    for(ItemStack cis : drops)
					    	blockinv.addItem(cis);
				    }
				    
				    
				    
				}
				
			}
	    }
	}
		
	
	
	@EventHandler
	public void onChatSend(AsyncPlayerChatEvent e) {
		if(e.getMessage().contains("@sevra relexe")) {
			Bukkit.getScheduler().runTask(Main.plugin, () -> {
				e.getPlayer().sendMessage(ChatColor.GREEN+"sevra traves relexa...");
				Bukkit.reload();
			});
			e.setCancelled(true);
		}
	}
	
}
