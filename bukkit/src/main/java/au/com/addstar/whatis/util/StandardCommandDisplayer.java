package au.com.addstar.whatis.util;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.MultipleCommandAlias;
import org.bukkit.command.PluginIdentifiableCommand;
import org.bukkit.command.defaults.BukkitCommand;

public class StandardCommandDisplayer implements CommandDisplayer
{
	@Override
	public boolean displayInfo( Command command, String label, CommandSender sender )
	{
		if(command instanceof MultipleCommandAlias)
		{
			displayMultiCommand((MultipleCommandAlias)command, label, sender);
			return true;
		}
		
		if(command instanceof PluginIdentifiableCommand || command instanceof BukkitCommand )
		{
			if(command.getAliases().contains(label))
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&', String.format("&eCommand: &f/%s (alias of /%s)", label, command.getName())));
			else
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&', String.format("&eCommand: &f/%s", label)));
			
			if(command.getDescription() != null && !command.getDescription().isEmpty())
				sender.sendMessage(ChatColor.GRAY + command.getDescription());

			sender.sendMessage(ChatColor.translateAlternateColorCodes('&', String.format("&6Plugin: &7%s", getSource(command))));
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
			return true;
		}
		return false;
	}
	
	private void displayMultiCommand(MultipleCommandAlias command, String label, CommandSender sender)
	{
		if(command.getAliases().contains(label))
			sender.sendMessage(ChatColor.translateAlternateColorCodes('&', String.format("&eMultiCommand: &f/%s (alias of /%s)", label, command.getName())));
		else
			sender.sendMessage(ChatColor.translateAlternateColorCodes('&', String.format("&eMultiCommand: &f/%s", label)));
		
		for(Command fallback : command.getCommands())
			sender.sendMessage(ChatColor.translateAlternateColorCodes('&', String.format("&e/%s &7from &e%s", fallback.getName(), CommandFinder.getSource(fallback))));
		
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
	
	@Override
	public String getSource( Command command )
	{
		if(command instanceof PluginIdentifiableCommand)
			return ((PluginIdentifiableCommand)command).getPlugin().getName();
		else if(command instanceof BukkitCommand)
			return "CraftBukkit";
		return null;
	}

}
