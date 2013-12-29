package au.com.addstar.whatis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredListener;

import au.com.addstar.whatis.EventHelper.EventCallback;

public class EventViewCommand implements ICommand
{
	@Override
	public String getName()
	{
		return "events";
	}

	@Override
	public String[] getAliases()
	{
		return null;
	}

	@Override
	public String getPermission()
	{
		return null;
	}

	@Override
	public String getUsageString( String label, CommandSender sender )
	{
		return label + " (plugin <plugin>|<eventName>)";
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
		if(args.length != 1 && args.length != 2)
			return false;
		
		if(args.length == 1)
		{
			Class<? extends Event> eventClass = EventHelper.parseEvent(args[0]);
			
			if(eventClass == null)
			{
				sender.sendMessage(args[0] + " is not an event type");
				return true;
			}
			
			HandlerList handlers = EventHelper.getHandlers(eventClass.asSubclass(Event.class));
			
			sender.sendMessage(String.format("Handlers for %s: %d", eventClass.getSimpleName(), handlers.getRegisteredListeners().length));
			HashMap<Plugin, Integer> count = new HashMap<Plugin, Integer>();
			
			for(RegisteredListener listener : handlers.getRegisteredListeners())
			{
				Integer num = count.get(listener.getPlugin());
				if(num == null)
					num = 1;
				else
					++num;
				
				count.put(listener.getPlugin(), num);
			}
			
			for(Entry<Plugin, Integer> entry : count.entrySet())
				sender.sendMessage(String.format("* %s: %d", entry.getKey().getName(), entry.getValue()));
		}
		else
		{
			if(!args[0].equals("plugin"))
				return false;
			
			Plugin plugin = Bukkit.getPluginManager().getPlugin(args[1]);
			
			if(plugin == null)
			{
				sender.sendMessage(ChatColor.RED + "Unknown plugin: " + args[1]);
				return true;
			}
			
			List<RegisteredListener> all = HandlerList.getRegisteredListeners(plugin);
			HashSet<Listener> unique = new HashSet<Listener>();
			
			for(RegisteredListener listener : all)
				unique.add(listener.getListener());
			
			for(Listener listener : unique)
			{
				List<EventCallback> callbacks = EventHelper.getEventCallbacks(listener);
				
				for(EventCallback callback : callbacks)
					sender.sendMessage(String.format("%s - %s", callback.eventClass.getSimpleName(), callback.priority.toString()));
			}
		}
		return true;
	}

	@Override
	public List<String> onTabComplete( CommandSender sender, String label, String[] args )
	{
		if(args.length == 1)
		{
			ArrayList<String> matching = new ArrayList<String>();
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
