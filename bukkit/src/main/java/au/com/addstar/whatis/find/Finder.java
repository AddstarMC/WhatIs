package au.com.addstar.whatis.find;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.event.Event;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.Plugin;

import com.google.common.collect.HashMultimap;

import au.com.addstar.whatis.EventHelper;
import au.com.addstar.whatis.util.CommandFinder;

public class Finder
{
	public static List<? extends IObject> find(String string, Class<? extends IObject> findClass)
	{
		if(findClass == PermissionObject.class)
			return findPermissions(string);
		else if(findClass == EventObject.class)
			return findEvents(string);
		else if(findClass == PluginObject.class)
			return findPlugins(string);
		else if(findClass == CommandObject.class)
			return findCommands(string);
		
		return null;
	}
	
	public static List<PermissionObject> findPermissions(String name)
	{
		boolean fullMatch = true;
		boolean invert = false;
		
		name = name.trim();
		if(name.startsWith("!"))
		{
			invert = true;
			name = name.substring(1);
		}
		
		if(name.startsWith(":"))
		{
			fullMatch = false;
			name = name.substring(1);
		}
		
		String[] parts = name.split("\\.");
		
		StringBuilder b = new StringBuilder();
		for(String part : parts)
		{
			if(b.length() != 0)
				b.append("\\.");
			
			if (part.equals("**")) // match all segments
				b.append("[^\\.]+(?:\\.[^\\.]+)*");
			else if(part.equals("*")) // match segment
				b.append("[^\\.]+");
			else // normal
			{
				part = part.replaceAll("\\?", "[^\\.]?");
				part = part.replaceAll("\\*", "[^\\.]*");
				b.append(part);
			}
		}
		
		if(fullMatch)
		{
			b.insert(0, "^");
			b.append("$");
		}
		
		Pattern pattern = Pattern.compile(b.toString());
		
		ArrayList<PermissionObject> matches = new ArrayList<>();
		for(Permission perm : Bukkit.getPluginManager().getPermissions())
		{
			Matcher m = pattern.matcher(perm.getName());
			
			boolean matched = m.find();
			
			if((matched && !invert) || (!matched && invert))
				matches.add(new PermissionObject(perm));
		}
		
		Collections.sort(matches);
		return matches;
	}
	
	public static List<CommandObject> findCommands(String name)
	{
		SimpleCommandMap map = (SimpleCommandMap)CommandFinder.getCommandMap();
		
		HashSet<Command> unique = new HashSet<>();
		for(Command command : map.getCommands())
			unique.add(command);
		
		HashMultimap<String, Command> commands = HashMultimap.create();
		for(Command command : unique)
		{
			commands.put(command.getName(), command);
			if(command.getAliases() != null)
			{
				for(String alias : command.getAliases())
					commands.put(alias, command);
			}
		}
		
		boolean fullMatch = true;
		boolean invert = false;
		
		name = name.trim();
		if(name.startsWith("!"))
		{
			invert = true;
			name = name.substring(1);
		}
		
		if(name.startsWith(":"))
		{
			fullMatch = false;
			name = name.substring(1);
		}
		
		name = name.replaceAll("\\?", ".?");
		name = name.replaceAll("\\*", ".*");
		
		if(fullMatch)
			name = "^" + name + "$";
		
		Pattern pattern = Pattern.compile(name);
		
		ArrayList<CommandObject> matches = new ArrayList<>();
		
		for(String cmd : commands.keys())
		{
			Matcher m = pattern.matcher(cmd);
			boolean match = m.find();
			
			if((match && !invert) || (!match && invert))
			{
				for(Command command : commands.get(cmd))
				{
					System.out.println("Adding: " + cmd + "-" + command);
					matches.add(new CommandObject(cmd, command));
				}
			}
		}
		
		return matches;
	}
	
	public static List<PluginObject> findPlugins(String name)
	{
		boolean fullMatch = true;
		boolean invert = false;
		
		name = name.trim();
		if(name.startsWith("!"))
		{
			invert = true;
			name = name.substring(1);
		}
		
		if(name.startsWith(":"))
		{
			fullMatch = false;
			name = name.substring(1);
		}
		
		name = name.replaceAll("\\?", ".?");
		name = name.replaceAll("\\*", ".*");
		
		if(fullMatch)
			name = "^" + name + "$";
		
		Pattern pattern = Pattern.compile(name);
		
		ArrayList<PluginObject> matches = new ArrayList<>();
		for(Plugin plugin : Bukkit.getPluginManager().getPlugins())
		{
			Matcher m = pattern.matcher(plugin.getName());
			boolean match = m.find();
			
			if((match && !invert) || (!match && invert))
				matches.add(new PluginObject(plugin));
			
		}
		
		return matches;
	}
	
	public static List<EventObject> findEvents(String name)
	{
		boolean fullMatch = true;
		boolean invert = false;
		
		name = name.trim();
		if(name.startsWith("!"))
		{
			invert = true;
			name = name.substring(1);
		}
		
		if(name.startsWith(":"))
		{
			fullMatch = false;
			name = name.substring(1);
		}
		
		name = name.replaceAll("\\?", ".?");
		name = name.replaceAll("\\*", ".*");
		
		if(fullMatch)
			name = "^" + name + "$";
		
		Pattern pattern = Pattern.compile(name);
		
		ArrayList<EventObject> matches = new ArrayList<>();
		for(Class<? extends Event> clazz : EventHelper.getEvents())
		{
			Matcher m = pattern.matcher(clazz.getSimpleName());
			boolean match = m.find();
			
			if((match && !invert) || (!match && invert))
				matches.add(new EventObject(clazz));
		}
		
		return matches;
	}
}
