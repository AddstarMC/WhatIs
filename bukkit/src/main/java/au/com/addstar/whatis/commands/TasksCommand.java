package au.com.addstar.whatis.commands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import au.com.addstar.whatis.WhatIs;

public class TasksCommand implements ICommand
{

	@Override
	public String getName()
	{
		return "tasks";
	}

	@Override
	public String[] getAliases()
	{
		return null;
	}

	@Override
	public String getPermission()
	{
		return "whatis.tasks";
	}

	@Override
	public String getUsageString( String label, CommandSender sender )
	{
		return label + " [plugin]";
	}

	@Override
	public String getDescription()
	{
		return "View all running / scheduled tasks.";
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
	public boolean onCommand( CommandSender sender, String label, String[] args )
	{
		if(args.length != 0 && args.length != 1)
			return false;
		
		Plugin plugin = null;
		
		if(args.length == 1)
		{
			plugin = Bukkit.getPluginManager().getPlugin(args[0]);
			if(plugin == null)
			{
				sender.sendMessage(ChatColor.RED + "Unknown plugin " + args[0]);
				return true;
			}
		}
		
		if(plugin != null)
			sender.sendMessage(ChatColor.GOLD + "Tasks for " + plugin.getName() + ":");
		else
			sender.sendMessage(ChatColor.GOLD + "All tasks:");
		
		ArrayList<BukkitTask> tasks = new ArrayList<>();
		
		for(BukkitTask pending : Bukkit.getScheduler().getPendingTasks())
		{
			if(plugin == null || pending.getOwner() == plugin)
				tasks.add(pending);
		}
		
		Collections.sort(tasks, new Comparator<BukkitTask>()
		{
			@Override
			public int compare( BukkitTask o1, BukkitTask o2 )
			{
				return Integer.compare(o1.getTaskId(), o2.getTaskId());
			}
		});
		
		if(tasks.isEmpty())
			sender.sendMessage(" No tasks found");
		else
		{
			for(BukkitTask task : tasks)
			{
				StringBuilder builder = new StringBuilder();
				
				if(plugin == null)
				{
					builder.append(ChatColor.YELLOW);
					builder.append(task.getOwner().getName());
					builder.append(ChatColor.GRAY);
					builder.append(": ");
				}
				
				builder.append(ChatColor.GRAY);
				builder.append("Task ");
				builder.append(ChatColor.YELLOW);
				builder.append(task.getTaskId());
				builder.append(ChatColor.GRAY);
				builder.append(" ");
				builder.append(WhatIs.getTaskSource(task));
				builder.append("\n");
				
				sender.sendMessage(builder.toString());
			}
		}
		
		return true;
	}

	@Override
	public List<String> onTabComplete( CommandSender sender, String label, String[] args )
	{
		return null;
	}

}
