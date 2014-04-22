package au.com.addstar.whatis;

import java.util.List;
import java.util.WeakHashMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import au.com.addstar.whatis.entities.EntityCategory;
import au.com.addstar.whatis.entities.EntityConcentrationMap;
import au.com.addstar.whatis.entities.EntityGroup;
import au.com.addstar.whatis.util.Callback;

public class EntityCommand implements ICommand
{
	private WeakHashMap<CommandSender, EntityConcentrationMap> mStoredResults = new WeakHashMap<CommandSender, EntityConcentrationMap>();
	
	public static final int resultsPerPage = 4;
	public static final int resutlsPerPageConsole = 10;
	
	@Override
	public String getName()
	{
		return "entities";
	}

	@Override
	public String[] getAliases()
	{
		return null;
	}

	@Override
	public String getPermission()
	{
		return "whatis.entity";
	}

	@Override
	public String getUsageString( String label, CommandSender sender )
	{
		return label + " [show [page]|[world]]";
	}

	@Override
	public String getDescription()
	{
		return "Finds concentrations of entities that may be causing issues";
	}

	@Override
	public boolean canBeConsole()
	{
		return true;
	}

	@Override
	public boolean canBeCommandBlock()
	{
		return false;
	}
	
	private void displayResults(CommandSender sender, EntityConcentrationMap map, int page)
	{
		int perPage = (sender instanceof Player ? resultsPerPage : resutlsPerPageConsole);
		
		List<EntityGroup> groups = map.getAllGroups();
		
		if(groups.isEmpty())
		{
			sender.sendMessage(ChatColor.GOLD + "There are no results to display.");
			return;
		}
		
		int pages = (int)Math.ceil(groups.size() / (float)perPage);
		
		if(page >= pages)
			page = pages-1;
		
		if(page < 0)
			page = 0;
		
		
		int start = page * perPage;
		
		sender.sendMessage(ChatColor.YELLOW + "Entity concentrations: ");
		for(int i = start; i < start + perPage; ++i)
		{
			EntityGroup group = groups.get(i);
			
			String densityColor = null;
			float density = group.getDensity();
			if(density > 1)
				densityColor = ChatColor.RED + ChatColor.BOLD.toString();
			else if(density > 0.6)
				densityColor = ChatColor.RED.toString();
			else if(density > 0.3)
				densityColor = ChatColor.YELLOW.toString();
			else
				densityColor = ChatColor.GREEN.toString();
			
			String spacingColor = null;
			float spacing = group.getSpacing();
			if(spacing < 0.5)
				spacingColor = ChatColor.RED + ChatColor.BOLD.toString();
			else if(spacing < 1)
				spacingColor = ChatColor.RED.toString();
			else if(spacing < 3)
				spacingColor = ChatColor.YELLOW.toString();
			else
				spacingColor = ChatColor.GREEN.toString();
			
			sender.sendMessage(ChatColor.translateAlternateColorCodes('&', String.format("&e%d,%d,%d&7 (&f%s&7) r: &c%d&7 density: %s%.2f &7spacing: %s%.2f", group.getLocation().getBlockX(), group.getLocation().getBlockY(), group.getLocation().getBlockZ(), group.getLocation().getWorld().getName(), (int)group.getRadius(), densityColor, density, spacingColor, spacing)));
			sender.sendMessage(ChatColor.WHITE + "  Total: " + ChatColor.YELLOW + group.getTotalCount());
			
			for(EntityCategory cat : EntityCategory.values())
			{
				int count = group.getCount(cat);
				if(count != 0)
					sender.sendMessage(ChatColor.GRAY + "  " + cat.name() + ": " + ChatColor.YELLOW + count);
			}
			
			String playerString = "";
			boolean odd = true;
			
			int count = 0;
			int maxPlayers = 2;
			for(String cause : group.getCauses())
			{
				String part = null;
				if(cause.startsWith("player:"))
				{
					if(count <= maxPlayers)
					{
						part = cause.substring(7);
						++count;
					}
					else
						continue;
				}
				else if(cause.equals("spawn"))
					part = "Spawn Region";
				else if(cause.equals("unknown:players"))
					part = "Unknown Players";
				else if(cause.equals("unknown"))
					part = " Unknown (probably plugins)";
				
				if(!playerString.isEmpty())
					playerString += ChatColor.GRAY + ", ";

				if(odd)
					playerString += ChatColor.GRAY;
				else
					playerString += ChatColor.WHITE;
				
				playerString += part; 
				odd = !odd;
			}
			
			if(count > maxPlayers)
				playerString += ChatColor.GRAY + " and " + (count - maxPlayers) + " more";
			
			sender.sendMessage(ChatColor.YELLOW + "Cause: " + playerString);

			sender.sendMessage("");
		}
		
		if(page < pages)
			sender.sendMessage(ChatColor.translateAlternateColorCodes('&', String.format("&7Page &e%d&7 of &e%d &7(use &e/whatis entities show %d&7 to get the next page)", page+1, pages, page+2)));
		else
			sender.sendMessage(ChatColor.translateAlternateColorCodes('&', String.format("&7Page &e%d&7 of &e%d", page+1, pages)));
	}

	@Override
	public boolean onCommand( final CommandSender sender, String label, String[] args )
	{
		if(args.length != 0 && args.length != 1 && args.length != 2)
			return false;
		
		if(args.length >= 1 && args[0].equalsIgnoreCase("show"))
		{
			int page = 0;
			
			if(args.length == 2)
			{
				try
				{
					page = Integer.parseInt(args[1]);
					if(page <= 0)
					{
						sender.sendMessage(ChatColor.RED + "Page number must be 1 or greater");
						return true;
					}
					
					--page;
				}
				catch(NumberFormatException e)
				{
					sender.sendMessage(ChatColor.RED + "Page number must be a number 1 or greater");
					return true;
				}
			}
			
			EntityConcentrationMap map = mStoredResults.get(sender);
			if(map == null)
			{
				sender.sendMessage(ChatColor.RED + "You have no stored results");
				return true;
			}
			
			displayResults(sender, map, page);
			return true;
		}
		
		World world = null;
		EntityConcentrationMap map = new EntityConcentrationMap(WhatIs.instance);
		
		if(args.length == 1)
		{
			world = Bukkit.getWorld(args[0]);
			if(world == null)
			{
				sender.sendMessage(ChatColor.RED + "Unknown world " + args[0]);
				return true;
			}
			
			map.queueWorld(world);
		}
		else
			map.queueAll();
		
		mStoredResults.remove(sender);
		
		map.build(new Callback<EntityConcentrationMap>()
		{
			@Override
			public void onCompleted( EntityConcentrationMap data )
			{
				mStoredResults.put(sender, data);
				sender.sendMessage(ChatColor.GOLD + "Generation complete");
				sender.sendMessage(ChatColor.GRAY + "You can see these results at any time with /whatis entities show [page]");
				
				displayResults(sender, data, 0);
			}
		});
		
		sender.sendMessage(ChatColor.GOLD + "Started generating the entity concentration map. Please wait...");
		
		return true;
	}

	@Override
	public List<String> onTabComplete( CommandSender sender, String label, String[] args )
	{
		return null;
	}

}
