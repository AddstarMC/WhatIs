package au.com.addstar.whatis.commands;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;

import au.com.addstar.whatis.EventGroups;
import au.com.addstar.whatis.EventHelper;
import au.com.addstar.whatis.QuickMonitor;
import au.com.addstar.whatis.QuickMonitor.CancelReport;
import au.com.addstar.whatis.eventhook.CancelHook;
import au.com.addstar.whatis.eventhook.DurationTarget;
import au.com.addstar.whatis.util.Callback;
import au.com.addstar.whatis.util.filters.FilterCompiler;
import au.com.addstar.whatis.util.filters.FilterSet;

public class WhatCancelledCommand implements ICommand
{
	@Override
	public String getName()
	{
		return "whatcancelled";
	}

	@Override
	public String[] getAliases()
	{
		return new String[] {"whatcanceled"};
	}

	@Override
	public String getPermission()
	{
		return "whatis.whatcancelled";
	}

	@Override
	public String getUsageString( String label, CommandSender sender )
	{
		return label + " (action <block|item|move|chat|entity|player> [ticks] [player])|(event <eventname> [ticks] [filter])";
	}

	@Override
	public String getDescription()
	{
		return "Monitors relevant events checking what prevented them from completing. If ticks is not specified, the monitor will last for 5 seconds (100 ticks)";
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

	private boolean onAction(final CommandSender sender, String[] args)
	{
		if(args.length == 0 || args.length > 3)
			return false;
		
		EventGroups action = null;
		
		for(EventGroups group : EventGroups.values())
		{
			if(args[0].equalsIgnoreCase(group.name()))
			{
				action = group;
				break;
			}
		}
		
		if(action == null)
		{
			sender.sendMessage(ChatColor.RED + "Unknown action " + args[0]);
			return true;
		}
		
		int ticks = 100;
		Player player = (sender instanceof Player ? (Player)sender : null);
		boolean hasTicks = false;
		
		// TODO: If the player name is just numbers, this will be interpreted as ticks
		if(args.length >= 2)
		{
			try
			{
				ticks = Integer.parseInt(args[1]);
				if(ticks <= 0)
				{
					sender.sendMessage("Ticks must be a number greater than 0");
					return true;
				}
				hasTicks = true;
			}
			catch(NumberFormatException e)
			{
			}
		}
		
		if((args.length == 2 && !hasTicks) || (args.length == 3 && hasTicks))
		{
			player = Bukkit.getPlayer(args[args.length-1]);
			if(player == null)
			{
				sender.sendMessage(ChatColor.RED + "Unknown player " + args[args.length-1]);
				return true;
			}
		}
		
		if(player == null)
		{
			sender.sendMessage(ChatColor.RED + "You need to specify a player");
			return true;
		}
		
		try
		{
			final int fTicks = ticks;
			QuickMonitor.checkForCancel(data -> {
				Collection<CancelReport> reports = data.getReports();

				if(reports.isEmpty())
					sender.sendMessage(ChatColor.GOLD + "[WhatIs] " + ChatColor.WHITE + "Nothing cancelled that action or nothing called a relevant event within " + fTicks + " ticks.");
				else if(reports.size() == 1)
				{
					CancelReport report = reports.iterator().next();
					sender.sendMessage(ChatColor.GOLD + "[WhatIs] " + ChatColor.WHITE + "That action was cancelled by " + ChatColor.YELLOW + report.plugin.getName());
					sender.sendMessage(ChatColor.YELLOW + "Event: " + ChatColor.GRAY + report.event.getSimpleName());
					sender.sendMessage(ChatColor.YELLOW + "Handler: " + ChatColor.GRAY + report.handler);
				}
				else
				{
					sender.sendMessage(ChatColor.GOLD + "[WhatIs] " + ChatColor.WHITE + "The event was called and cancelled " + reports.size() + " times within " + fTicks + " ticks:");
					for(CancelReport report : reports)
					{
						sender.sendMessage(ChatColor.GOLD + DateFormat.getTimeInstance().format(report.time) + ChatColor.WHITE + " " + report.plugin.getName() + ":");
						sender.sendMessage(ChatColor.YELLOW + "Event: " + ChatColor.GRAY + report.event.getSimpleName());
						sender.sendMessage(ChatColor.YELLOW + "Handler: " + ChatColor.GRAY + report.handler);
					}
				}
			}, DurationTarget.playerForTicksOrCancel(ticks, player), action.events);
			sender.sendMessage(ChatColor.GREEN + "The action " + action.name() + " is now being monitored for " + ticks + " ticks");
		}
		catch(IllegalArgumentException | IllegalStateException e)
		{
			sender.sendMessage(ChatColor.RED + e.getMessage());
		}

		return true;
	}
	
	@SuppressWarnings( "unchecked" )
	private boolean onEvent(final CommandSender sender, String[] args)
	{
		if(args.length == 0)
			return false;
		
		Class<? extends Event> eventClass = EventHelper.parseEvent(args[0]);
		
		if(eventClass == null)
		{
			sender.sendMessage(args[0] + " is not an event type");
			return true;
		}
		
		int ticks = 100;
		boolean hasTicks = false;
		if(args.length >= 2)
		{
			try
			{
				ticks = Integer.parseInt(args[1]);
				if(ticks <= 0)
				{
					sender.sendMessage("Ticks must be a number greater than 0");
					return true;
				}
				hasTicks = true;
			}
			catch(NumberFormatException e)
			{
			}
		}

		FilterSet filters = null; 
		if((args.length >= 2 && !hasTicks) || (args.length >= 3 && hasTicks))
		{
			StringBuilder argString = new StringBuilder();
			for(int i = (hasTicks ? 2 : 1); i < args.length; i++)
				argString.append(args[i]).append(" ");
			
			argString = new StringBuilder(argString.toString().trim());
		
			try
			{
				filters = FilterCompiler.compile(eventClass, argString.toString());
			}
			catch(IllegalArgumentException e)
			{
				sender.sendMessage(ChatColor.RED + e.getMessage());
				return true;
			}
		}
		
		try
		{
			final int fTicks = ticks;
			QuickMonitor.checkForCancel(data -> {
				Collection<CancelReport> reports = data.getReports();

				if(reports.isEmpty())
					sender.sendMessage(ChatColor.GOLD + "[WhatIs] " + ChatColor.WHITE + "Nothing cancelled that event or nothing called that event within " + fTicks + " ticks.");
				else if(reports.size() == 1)
				{
					CancelReport report = reports.iterator().next();
					sender.sendMessage(ChatColor.GOLD + "[WhatIs] " + ChatColor.WHITE + "The event was cancelled by " + ChatColor.YELLOW + report.plugin.getName());
					sender.sendMessage(ChatColor.YELLOW + "Handler: " + ChatColor.GRAY + report.handler);
				}
				else
				{
					sender.sendMessage(ChatColor.GOLD + "[WhatIs] " + ChatColor.WHITE + "The event was called and cancelled " + reports.size() + " times within " + fTicks + " ticks:");
					for(CancelReport report : reports)
					{
						sender.sendMessage(ChatColor.GOLD + DateFormat.getTimeInstance().format(report.time) + ChatColor.WHITE + " " + report.plugin.getName() + ":");
						sender.sendMessage(ChatColor.YELLOW + "Handler: " + ChatColor.GRAY + report.handler);
					}
				}
			}, DurationTarget.forTicksOrCancel(ticks), filters, eventClass);
			sender.sendMessage(ChatColor.GREEN + eventClass.getSimpleName() + " is now being monitored for " + ticks + " ticks");
		}
		catch(IllegalArgumentException | IllegalStateException e)
		{
			sender.sendMessage(ChatColor.RED + e.getMessage());
		}

		return true;
	}
	
	@Override
	public boolean onCommand( CommandSender sender, String label, String[] args )
	{
		if(args.length < 1)
			return false;
		
		if(args[0].equalsIgnoreCase("action"))
			return onAction(sender, Arrays.copyOfRange(args, 1, args.length));
		else if(args[0].equalsIgnoreCase("event"))
			return onEvent(sender, Arrays.copyOfRange(args, 1, args.length));
		else
			return false;
	}

	@Override
	public List<String> onTabComplete( CommandSender sender, String label, String[] args )
	{
		if(args.length == 1)
		{
			List<String> matching = new ArrayList<>();
			if("action".startsWith(args[0].toLowerCase()))
				matching.add("action");
			if("event".startsWith(args[0].toLowerCase()))
				matching.add("event");
			return matching;
		}
		else if(args.length == 2 && args[0].equalsIgnoreCase("event"))
		{
			List<String> matching = new ArrayList<>();
			String toMatch = args[1].toLowerCase();
			for(String name : EventHelper.getEventNames())
			{
				if(name.startsWith(toMatch))
					matching.add(name);
			}
			
			return matching;
		}
		else if(args.length == 2 && args[0].equalsIgnoreCase("action"))
		{
			List<String> matching = new ArrayList<>();
			String toMatch = args[1].toLowerCase();
			for(EventGroups group : EventGroups.values())
			{
				if(group.name().toLowerCase().startsWith(toMatch))
					matching.add(group.name());
			}
			
			return matching;
		}
		
		return null;
	}
}
