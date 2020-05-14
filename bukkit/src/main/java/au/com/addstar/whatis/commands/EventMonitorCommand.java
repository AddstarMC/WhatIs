package au.com.addstar.whatis.commands;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import au.com.addstar.whatis.eventhook.EventOutput;
import au.com.addstar.whatis.eventhook.FileEventOutput;
import au.com.addstar.whatis.eventhook.PasteEventOutput;
import javafx.event.EventHandler;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Event;

import au.com.addstar.whatis.EventHelper;
import au.com.addstar.whatis.EventReporter;
import au.com.addstar.whatis.WhatIs;
import au.com.addstar.whatis.util.filters.FilterCompiler;
import au.com.addstar.whatis.util.filters.FilterSet;

public class EventMonitorCommand implements ICommand
{
	@Override
	public String getName()
	{
		return "eventmonitor";
	}

	@Override
	public String[] getAliases()
	{
		return new String[] { "monitorevent" };
	}

	@Override
	public String getPermission()
	{
		return "whatis.eventmonitor";
	}

	@Override
	public String getUsageString( String label, CommandSender sender )
	{
		return label + " <eventname> <ticks> ['paste'] <filterString...>";
	}

	@Override
	public String getDescription()
	{
		return "Monitors an event for <ticks> number of ticks and outputs it to a file";
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
		if(args.length < 2)
			return false;
		if(args[0].toLowerCase().equals("rebuildeventmap")){
			sender.sendMessage("Event Map current: Events ->" + EventHelper.getEventNames().size());
			EventHelper.buildEventMap();
			sender.sendMessage("Event Map rebuilt: Events ->" + EventHelper.getEventNames().size());
			return true;
		}
		Class<? extends Event> eventClass = EventHelper.parseEvent(args[0]);
		
		if(eventClass == null)
		{
			sender.sendMessage(args[0] + " is not an event type");
			return true;
		}
		
		int count;
		
		try
		{
			count = Integer.parseInt(args[1]);
		}
		catch(NumberFormatException e)
		{
			sender.sendMessage(args[1] + " is not an integer");
			return true;
		}
		
		FilterSet filter = null;
		EventOutput output ;
		if(args.length > 2) {
			int startIndex = 2;
			if (args[2].toLowerCase().equals("paste")) {
				output = new PasteEventOutput();
				startIndex = 3;
			} else {
				output = createFileOutput();
			}
			if (args.length > startIndex) {
				String argString = StringUtils.join(args, ' ', startIndex, args.length);

				try {
					filter = FilterCompiler.compile(eventClass, argString);
				} catch (IllegalArgumentException e) {
					sender.sendMessage(ChatColor.RED + e.getMessage());
					return true;
				}
			}
		} else {
			output = createFileOutput();
		}

		try
		{
			EventReporter.monitorEvent(eventClass, count, sender, output, filter);
			sender.sendMessage(ChatColor.GREEN + eventClass.getSimpleName() + " is now being monitored for " + count + " ticks");
		}
		catch(IllegalArgumentException | IllegalStateException e)
		{
			sender.sendMessage(ChatColor.RED + e.getMessage());
		}

		return true;
	}
	private FileEventOutput createFileOutput() {
		File parent = new File(WhatIs.instance.getDataFolder(), "eventreports");
		parent.mkdirs();
		return new FileEventOutput(parent, new SimpleDateFormat("yyyy-MM-dd-hh-mm-ss").format(new Date()) + ".txt");
	}
	@Override
	public List<String> onTabComplete( CommandSender sender, String label, String[] args )
	{
		if(args.length == 1)
		{
			List<String> matching = new ArrayList<>();
			String toMatch = args[0].toLowerCase();
			for(String name : EventHelper.getEventNames())
			{
				if(name.startsWith(toMatch))
					matching.add(name);
			}
			
			if("plugin".startsWith(toMatch))
				matching.add("plugin");
			
			return matching;
		}
		
		return null;
	}

}
