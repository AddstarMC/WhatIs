package au.com.addstar.whatis.commands;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class CommandFinder
{
	private static ArrayList<CommandDisplayer> mDisplayers = new ArrayList<CommandDisplayer>();
	
	public static void init()
	{
		if(Bukkit.getPluginManager().isPluginEnabled("WorldEdit"))
			registerCommandInfo(new WorldEditCommandDisplayer());
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
		
		if(command.getDescription() != null && !command.getDescription().isEmpty())
			sender.sendMessage(ChatColor.GRAY + command.getDescription());

		sender.sendMessage(ChatColor.RED + "Unknown Source");
		sender.sendMessage(ChatColor.translateAlternateColorCodes('&', String.format("&6Perm: &7%s", command.getPermission())));
		
		if(!command.getAliases().isEmpty())
		{
			String aliases = "";
			boolean first = true;
			for(String alias : command.getAliases())
			{
				if(!aliases.isEmpty())
					aliases += ChatColor.GRAY + ", ";
				
				if(first)
					aliases += ChatColor.GRAY + alias;
				else
					aliases += ChatColor.WHITE + alias;
				
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
	
	public static void registerCommandInfo( CommandDisplayer info )
	{
		mDisplayers.add(info);
	}
	
	static
	{
		registerCommandInfo(new StandardCommandDisplayer());
	}
}
