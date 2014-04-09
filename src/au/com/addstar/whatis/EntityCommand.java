package au.com.addstar.whatis;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.CommandSender;

import au.com.addstar.whatis.entities.EntityConcentrationMap;
import au.com.addstar.whatis.util.Callback;

public class EntityCommand implements ICommand
{
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
		return label + " [world]";
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

	@Override
	public boolean onCommand( final CommandSender sender, String label, String[] args )
	{
		if(args.length != 0 && args.length != 1)
			return false;
		
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
		
		map.build(new Callback<EntityConcentrationMap>()
		{
			@Override
			public void onCompleted( EntityConcentrationMap data )
			{
				sender.sendMessage(ChatColor.GOLD + "Generation complete");
				System.out.println(data.getAllGroups());
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
