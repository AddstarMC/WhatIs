package au.com.addstar.whatis.commands;

import java.lang.invoke.MethodHandle;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Event;

import au.com.addstar.whatis.EventHelper;
import au.com.addstar.whatis.util.FilterHelper;
import au.com.addstar.whatis.util.FilterHelper.ClassConnector;

public class FiltersCommand implements ICommand
{
	@Override
	public String getName()
	{
		return "filters";
	}

	@Override
	public String[] getAliases()
	{
		return null;
	}

	@Override
	public String getPermission()
	{
		return "whatis.filters";
	}

	@Override
	public String getUsageString( String label, CommandSender sender )
	{
		return label + "[<event> [<path>]]";
	}

	@Override
	public String getDescription()
	{
		return "Explains how to use filters, or lists all filters for a class. <filterpart> can be things like 'block' which will show all sub filters for block, or 'block.location' which will show all sub filters for the location";
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
		if(args.length == 0)
		{
			sender.sendMessage(ChatColor.GOLD + "Filters: " + ChatColor.GRAY + "Filters allow you to select matching data only. The filter string format is:");
			sender.sendMessage(ChatColor.YELLOW + "[<filter1>,<filter2>,...,<filterN>]");
			sender.sendMessage(ChatColor.GRAY + "You can have as many filter components as you need to get the desired result all separated by a comma (,)");
			sender.sendMessage(ChatColor.GRAY + "Filter components are always in the format of " + ChatColor.YELLOW + "<path><operator><value>");
			sender.sendMessage(ChatColor.GRAY + " - <path> is the object you wish to filter on. This can be things like 'block' or 'location'.");
			sender.sendMessage(ChatColor.GRAY + "    You can also go into those objects using a period (.). For example: block.location.x");
			sender.sendMessage(ChatColor.GRAY + " - <operator> is the operation you are doing. Available operators are:");
			sender.sendMessage(ChatColor.GRAY + "     * = Equals, value of <path> must match <value>");
			sender.sendMessage(ChatColor.GRAY + "     * != Not equals, value of <path> must not match <value>");
			sender.sendMessage(ChatColor.GRAY + "     * : Contains, value of <path> must contain <value>");
			sender.sendMessage(ChatColor.GRAY + "     * !: Not contains, value of <path> must not contain <value>");
			sender.sendMessage(ChatColor.GRAY + "     * < Less than, value of <path> must be less than <value>");
			sender.sendMessage(ChatColor.GRAY + "     * <= Less than equal, value of <path> must be less than equal to <value>");
			sender.sendMessage(ChatColor.GRAY + "     * > Greater than, value of <path> must be greater than <value>");
			sender.sendMessage(ChatColor.GRAY + "     * >= Greather than equal, value of <path> must be greater than or equal to <value>");
			sender.sendMessage(ChatColor.GRAY + " - <value> is the value you are matching with. The format of this depends on what object <path> resolves to");
			sender.sendMessage("");
			sender.sendMessage(ChatColor.GRAY + "Example filter:");
			sender.sendMessage(ChatColor.WHITE + "[block.location.vector=0 0 0,block.type!=Air]");
			sender.sendMessage(ChatColor.GRAY + "This filter will match any block at the origin of any world that is not air");
		}
		else
		{
			Class<? extends Event> eventClass = EventHelper.parseEvent(args[0]);
			
			if(eventClass == null)
			{
				sender.sendMessage(args[0] + " is not an event type");
				return true;
			}
			
			String filterPart = null;
			if(args.length == 2)
				filterPart = args[1];
			else if(args.length > 2)
				return false;
			
			try
			{
				ClassConnector connector = getConnector(eventClass, filterPart);
				String name = eventClass.getSimpleName();
				if(filterPart != null)
					name += "." + filterPart;
				
				sender.sendMessage(ChatColor.GOLD + "Filters available in " + name);
				if(connector.getNames().isEmpty())
					sender.sendMessage(" - *None*");
				else
				{
					for(String method : connector.getNames())
						sender.sendMessage(" - " + method + ": " + connector.getHandle(method).type().returnType().getSimpleName());
				}
			}
			catch(IllegalArgumentException e)
			{
				sender.sendMessage(ChatColor.RED + "Error in path: " + e.getMessage());
			}
			
		}
		return true;
	}
	
	private ClassConnector getConnector(Class<?> clazz, String path)
	{
		ClassConnector connector = FilterHelper.getConnector(clazz);
		if(path == null)
			return connector;
		
		String[] parts = path.split("\\.");
		for(String part : parts)
		{
			MethodHandle handle = connector.getHandle(part);
			if(handle == null)
				throw new IllegalArgumentException(connector.getConnectedClass().getSimpleName() + " does not contain " + part);
			
			connector = FilterHelper.getConnector(handle.type().returnType());
		}
		
		return connector;
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
			
			return matching;
		}
		return null;
	}

}
