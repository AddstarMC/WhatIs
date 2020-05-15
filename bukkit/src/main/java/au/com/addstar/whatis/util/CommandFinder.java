package au.com.addstar.whatis.util;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.plugin.Plugin;

public class CommandFinder
{
	private static final Collection<CommandDisplayer> mDisplayers = new ArrayList<>();
	
	public static void init()
	{
	}
	
	public static void displayCommand( Command command, String label, CommandSender sender )
	{
		for(CommandDisplayer info : mDisplayers)
		{
			if(info.displayInfo(command, label, sender))
				return;
		}
		
		fallbackDisplay(command, label, sender);
	}
	
	private static void fallbackDisplay( Command command, String label, CommandSender sender )
	{
		if(command.getAliases().contains(label))
			sender.sendMessage(ChatColor.translateAlternateColorCodes('&', String.format("&eCommand: &f/%s (alias of /%s)", label, command.getName())));
		else
			sender.sendMessage(ChatColor.translateAlternateColorCodes('&', String.format("&eCommand: &f/%s", label)));
		
		if(!command.getDescription().isEmpty())
			sender.sendMessage(ChatColor.GRAY + command.getDescription());

		sender.sendMessage(ChatColor.RED + "Unknown Source");
		sender.sendMessage(ChatColor.translateAlternateColorCodes('&', String.format("&6Perm: &7%s", command.getPermission())));
		
		if(!command.getAliases().isEmpty())
		{
			StringBuilder aliases = new StringBuilder();
			boolean first = true;
			for(String alias : command.getAliases())
			{
				if(aliases.length() > 0)
					aliases.append(ChatColor.GRAY).append(", ");
				
				if(first)
					aliases.append(ChatColor.GRAY).append(alias);
				else
					aliases.append(ChatColor.WHITE).append(alias);
				
				first = !first;
			}
			
			sender.sendMessage(ChatColor.GOLD + "Aliases: " + aliases);
		}
	}
	
	public static String getSource( Command command )
	{
		for(CommandDisplayer info : mDisplayers)
		{
			String source = info.getSource(command);
			if(source != null)
				return source;
		}
		
		return "Unknown";
	}
	
	public static Plugin getPluginSource( Command command )
	{
		for(CommandDisplayer info : mDisplayers)
		{
			String source = info.getSource(command);
			if(source != null)
				return Bukkit.getPluginManager().getPlugin(source);
		}
		
		return null;
	}
	
	private static CommandMap mCommandMap = null;
	
	public static CommandMap getCommandMap()
	{
		if(mCommandMap != null)
			return mCommandMap;
		
		try
		{
			Method method = Bukkit.getServer().getClass().getMethod("getCommandMap");
			mCommandMap = (CommandMap)method.invoke(Bukkit.getServer());
		}
		catch(Exception e)
		{
			throw new RuntimeException(e);
		}
		return mCommandMap;
	}
	
	public static List<Command> getCommands(Plugin plugin)
	{
		SimpleCommandMap commands = (SimpleCommandMap)getCommandMap();
		HashSet<Command> matching = new HashSet<>();
		
		for(Command command : commands.getCommands())
		{
			Plugin source = getPluginSource(command);
			if(source == plugin)
				matching.add(command);
		}
		
		return new ArrayList<>(matching);
	}
	
	public static void registerCommandInfo( CommandDisplayer info )
	{
		mDisplayers.add(info);
	}
	
	static
	{
		registerCommandInfo(new StandardCommandDisplayer());
	}
}
