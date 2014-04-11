package au.com.addstar.whatis;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ChunkCommand implements ICommand
{

	@Override
	public String getName()
	{
		return "chunk";
	}

	@Override
	public String[] getAliases()
	{
		return null;
	}

	@Override
	public String getPermission()
	{
		return "whatis.chunk";
	}

	@Override
	public String getUsageString( String label, CommandSender sender )
	{
		return label + " [<x> <z> [world]]";
	}

	@Override
	public String getDescription()
	{
		return "See what is keeping a chunk loaded.";
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
		if(args.length != 0 && args.length != 2 && args.length != 3)
			return false;
		
		int x = 0;
		int z = 0;
		World world = null;
		
		if(sender instanceof Player)
		{
			Location loc = ((Player) sender).getLocation();
			x = loc.getBlockX() >> 4;
			z = loc.getBlockZ() >> 4;
			world = ((Player) sender).getWorld();
		}
		else if(args.length != 3)
			return false;
		
		if(args.length >= 2)
		{
			try
			{
				x = Integer.parseInt(args[0]);
			}
			catch(NumberFormatException e)
			{
				sender.sendMessage(ChatColor.RED + "x must be an integer");
				return true;
			}
			
			try
			{
				z = Integer.parseInt(args[1]);
			}
			catch(NumberFormatException e)
			{
				sender.sendMessage(ChatColor.RED + "z must be an integer");
				return true;
			}
		}
		
		if(args.length == 3)
		{
			world = Bukkit.getWorld(args[2]);
			if(world == null)
			{
				sender.sendMessage(ChatColor.RED + "Unknown world " + args[2]);
				return true;
			}
		}
		
		List<String> causes = ChunkUtil.getChunkAnchors(world, x, z);
		
		if(causes.isEmpty())
			sender.sendMessage(ChatColor.GOLD + String.format("The chunk %d,%d (%s) is not loaded", x, z, world.getName()));
		else
		{
			sender.sendMessage(ChatColor.GOLD + String.format("The chunk %d,%d (%s) is loaded by:", x, z, world.getName()));
			String playerString = "";
			boolean odd = true;
			
			for(String cause : causes)
			{
				if(cause.startsWith("player:"))
				{
					if(!playerString.isEmpty())
						playerString += ChatColor.GRAY + ", ";

					if(odd)
						playerString += ChatColor.GRAY;
					else
						playerString += ChatColor.WHITE;
					
					playerString += cause.substring(7);
					odd = !odd;
				}
				else if(cause.equals("spawn"))
					sender.sendMessage(ChatColor.GRAY + " Spawn Region");
				else if(cause.equals("unknown:players"))
					playerString = "Unknown Players";
				else if(cause.equals("unknown"))
					sender.sendMessage(ChatColor.GRAY + " Unknown (probably plugins)");
			}
			
			if(!playerString.isEmpty())
				sender.sendMessage(ChatColor.GRAY + " " + playerString);
		}
		
		return true;
	}

	@Override
	public List<String> onTabComplete( CommandSender sender, String label, String[] args )
	{
		return null;
	}

}
