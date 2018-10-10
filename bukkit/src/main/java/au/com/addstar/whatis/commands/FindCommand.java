package au.com.addstar.whatis.commands;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import au.com.addstar.whatis.find.CommandObject;
import au.com.addstar.whatis.find.EventObject;
import au.com.addstar.whatis.find.Finder;
import au.com.addstar.whatis.find.IObject;
import au.com.addstar.whatis.find.PermissionObject;
import au.com.addstar.whatis.find.PluginObject;


public class FindCommand implements ICommand
{

	@Override
	public String getName()
	{
		return "find";
	}

	@Override
	public String[] getAliases()
	{
		return null;
	}

	@Override
	public String getPermission()
	{
		return "whatis.find";
	}

	@Override
	public String getUsageString( String label, CommandSender sender )
	{
		return label + " <permission|command|plugin|event> <searchstring>";
	}

	@Override
	public String getDescription()
	{
		return "Searches for things on the server";
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
		
		Class<? extends IObject> findType = null;
		if(args[0].equalsIgnoreCase("permission") || args[0].equalsIgnoreCase("perm"))
			findType = PermissionObject.class;
		else if(args[0].equalsIgnoreCase("plugin"))
			findType = PluginObject.class;
		else if(args[0].equalsIgnoreCase("event"))
			findType = EventObject.class;
		else if(args[0].equalsIgnoreCase("command") || args[0].equalsIgnoreCase("cmd"))
			findType = CommandObject.class;
		else
		{
			sender.sendMessage(ChatColor.RED + "Unknown type: " + args[0]);
			return true;
		}
		
		StringBuilder searchString = new StringBuilder();
		for(int i = 1; i < args.length; ++i)
		{
			if(searchString.length() != 0)
				searchString.append(" ");
			searchString.append(args[i]);
		}
		
		List<? extends IObject> results = Finder.find(searchString.toString(), findType);
		
		if(results.isEmpty())
			sender.sendMessage(ChatColor.GOLD + "No results for '" + searchString + "'");
		else
		{
			sender.sendMessage(ChatColor.GOLD.toString() + results.size() + " results for '" + searchString + "'");
			for(IObject obj : results)
			{
				sender.sendMessage(obj.getName());
			}
		}
		
		return true;
	}

	@Override
	public List<String> onTabComplete( CommandSender sender, String label, String[] args )
	{
		return null;
	}

}
