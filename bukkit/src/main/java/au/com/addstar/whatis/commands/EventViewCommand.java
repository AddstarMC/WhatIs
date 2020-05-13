package au.com.addstar.whatis.commands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TreeMap;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredListener;
import org.bukkit.util.ChatPaginator;
import org.bukkit.util.ChatPaginator.ChatPage;

import au.com.addstar.whatis.EventHelper;
import au.com.addstar.whatis.EventHelper.EventCallback;

public class EventViewCommand implements ICommand
{
	@Override
	public String getName()
	{
		return "event";
	}

	@Override
	public String[] getAliases()
	{
		return null;
	}

	@Override
	public String getPermission()
	{
		return "whatis.event";
	}

	@Override
	public String getUsageString( String label, CommandSender sender )
	{
		return label + " (plugin <plugin>|count <eventName>|handlers <eventName>) [page]";
	}

	@Override
	public String getDescription()
	{
		return "Views the events the specified plugin is listening on";
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
		if(args.length != 2 && args.length != 3)
			return false;
		
		int page = 1;
		if(args.length == 3)
		{
			try
			{
				page = Integer.parseInt(args[2]);
			}
			catch(NumberFormatException e)
			{
				sender.sendMessage(ChatColor.RED + args[2] + " is not a page number");
				return true;
			}
			
			if(page <= 0)
			{
				sender.sendMessage(ChatColor.RED + "Page number must be 1 or higher");
				return true;
			}
		}
		
		StringBuilder raw = new StringBuilder();
		
		if(args[0].equalsIgnoreCase("count"))
		{
			Class<? extends Event> eventClass = EventHelper.parseEvent(args[1]);
			
			if(eventClass == null)
			{
				sender.sendMessage(args[1] + " is not an event type");
				return true;
			}
			
			HandlerList handlers = EventHelper.getHandlers(eventClass.asSubclass(Event.class));
			
			sender.sendMessage(ChatColor.translateAlternateColorCodes('&', String.format("&eHandler count for &c%s&e: &f%d",
				eventClass.getSimpleName(), handlers.getRegisteredListeners().length)));
			TreeMap<Plugin, Integer> count = new TreeMap<>((o1, o2) -> o1.getName().compareToIgnoreCase(o2.getName()));
			
			for(RegisteredListener listener : handlers.getRegisteredListeners())
			{
				Integer num = count.get(listener.getPlugin());
				if(num == null)
					num = 1;
				else
					++num;
				
				count.put(listener.getPlugin(), num);
			}
			
			ArrayList<String> lines = new ArrayList<>();
			
			for(Entry<Plugin, Integer> entry : count.entrySet())
				lines.add(ChatColor.translateAlternateColorCodes('&', String.format("&7- &6%s&f %d", entry.getKey().getName(), entry.getValue())));
			
			for(String line : lines)
			{
				if(raw.length() > 0)
					raw.append("\n");
				
				raw.append(line);
			}
		}
		else if(args[0].equalsIgnoreCase("handlers"))
		{
			Class<? extends Event> eventClass = EventHelper.parseEvent(args[1]);
			
			if(eventClass == null)
			{
				sender.sendMessage(args[1] + " is not an event type");
				return true;
			}
			
			HandlerList handlers = EventHelper.getHandlers(eventClass.asSubclass(Event.class));
			
			sender.sendMessage(ChatColor.translateAlternateColorCodes('&', String.format("&eHandlers for &c%s&e: &f%d", eventClass.getSimpleName(), handlers.getRegisteredListeners().length)));
			
			ArrayList<String> lines = new ArrayList<>();
			for(RegisteredListener listener : handlers.getRegisteredListeners())
				lines.add(ChatColor.GRAY + "- " + ChatColor.GOLD + listener.getPlugin().getName() + ChatColor.GRAY + " " + listener.getPriority() + (listener.isIgnoringCancelled() ? " IgnoreCancel" : ""));
			
			for(String line : lines)
			{
				if(raw.length() > 0)
					raw.append("\n");
				
				raw.append(line);
			}
		}
		else if(args[0].equalsIgnoreCase("plugin"))
		{
			Plugin plugin = Bukkit.getPluginManager().getPlugin(args[1]);
			
			if(plugin == null)
			{
				sender.sendMessage(ChatColor.RED + "Unknown plugin: " + args[1]);
				return true;
			}
			
			List<EventCallback> callbacks = EventHelper.getEventCallbacks(plugin);
			ArrayList<String> lines = new ArrayList<>();
			
			sender.sendMessage(ChatColor.translateAlternateColorCodes('&', String.format("&eHandlers for plugin &c%s&e: &f%d", plugin.getName(), callbacks.size())));
			
			for(EventCallback callback : callbacks)
				lines.add(ChatColor.translateAlternateColorCodes('&', String.format("&6%s &f%s %s", callback.eventClass.getSimpleName(), callback.priority.toString(), callback.ignoreCancelled ? "IgnoreCancel" : "")));
			
			Collections.sort(lines);
			
			for(String line : lines)
			{
				if(raw.length() > 0)
					raw.append("\n");
				
				raw.append(line);
			}
		}
		else
			return false;
		
		ChatPage p = ChatPaginator.paginate(raw.toString(), page);
		if(p.getPageNumber() < p.getTotalPages())
			sender.sendMessage(ChatColor.translateAlternateColorCodes('&', String.format("&7Page &e%d&7 of &e%d &7(use &e/whatis %s %s %s %d&7 to get the next page)", p.getPageNumber(), p.getTotalPages(), label, args[0], args[1], p.getPageNumber() + 1)));
		else
			sender.sendMessage(ChatColor.translateAlternateColorCodes('&', String.format("&7Page &e%d&7 of &e%d", p.getPageNumber(), p.getTotalPages())));
		sender.sendMessage(p.getLines());
		
		return true;
	}

	@Override
	public List<String> onTabComplete( CommandSender sender, String label, String[] args )
	{
		if(args.length == 1)
		{
			ArrayList<String> matching = new ArrayList<>();
			String toMatch = args[0].toLowerCase();

			if("plugin".startsWith(toMatch))
				matching.add("plugin");
			if("count".startsWith(toMatch))
				matching.add("count");
			if("handlers".startsWith(toMatch))
				matching.add("handlers");
			
			return matching;
		}
		else if(args.length == 2)
		{
			ArrayList<String> matching = new ArrayList<>();
			String toMatch = args[1].toLowerCase();
			for(String name : EventHelper.getEventNames())
			{
				if(name.startsWith(toMatch))
					matching.add(name);
			}
			
			return matching;
		}
		
		return null;
	}

}
