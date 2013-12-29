package au.com.addstar.whatis;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import au.com.addstar.whatis.commands.CommandFinder;

public class WhatIs extends JavaPlugin
{
	@Override
	public void onEnable()
	{
		CommandFinder.init();
		
		CommandDispatcher whatis = new CommandDispatcher("whatis", "");
		whatis.registerCommand(new EventViewCommand());
		whatis.registerCommand(new CommandCommand());
		
		getCommand("whatis").setExecutor(whatis);
		getCommand("whatis").setTabCompleter(whatis);
		
		Bukkit.getScheduler().runTask(this, new Runnable()
		{
			@Override
			public void run()
			{
				getLogger().info("Building event name map");
				EventHelper.buildEventMap();
			}
		});
	}
}
