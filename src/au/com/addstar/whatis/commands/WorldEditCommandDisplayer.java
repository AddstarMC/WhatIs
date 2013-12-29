package au.com.addstar.whatis.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import com.sk89q.bukkit.util.DynamicPluginCommand;

public class WorldEditCommandDisplayer implements CommandDisplayer
{
	@Override
	public boolean displayInfo( Command command, String label, CommandSender sender )
	{
		if(command instanceof DynamicPluginCommand)
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

	@Override
	public String getSource( Command command )
	{
		if(command instanceof DynamicPluginCommand)
			return ((DynamicPluginCommand)command).getPlugin().getName();
		
		return null;
	}

}
