package au.com.addstar.whatis;

import java.util.List;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class HeatCommand implements ICommand
{
	@Override
	public String getName()
	{
		return "heat";
	}

	@Override
	public String[] getAliases()
	{
		return new String[] {"chunkheat", "worldheat"};
	}

	@Override
	public String getPermission()
	{
		return "whatis.heat";
	}

	@Override
	public String getUsageString( String label, CommandSender sender )
	{
		return label + " [results <count>]";
	}

	@Override
	public String getDescription()
	{
		return "Either toggles the chunk heat map finding bad TPS causing areas, or shows the results";
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
		if(args.length != 0 && args.length != 2)
			return false;
		
		WorldHeatMap map = WorldHeatMap.getHeatMap();
		
		if(args.length == 0)
		{
			if(map.isRunning())
			{
				map.finish();
				sender.sendMessage(ChatColor.GOLD + "Chunk Heat Map was stopped");
			}
			else
			{
				map.start();
				sender.sendMessage(ChatColor.GOLD + "Chunk Heat Map is now running");
			}
		}
		else
		{
			if(args[0].equals("results"))
			{
				int count = 0;
				
				try
				{
					count = Integer.parseInt(args[1]);
					if(count <= 0)
					{
						sender.sendMessage(ChatColor.RED + "Count must be greater than 0");
						return true;
					}
				}
				catch(NumberFormatException e)
				{
					sender.sendMessage(ChatColor.RED + "Count must be a number greater than 0");
					return true;
				}
				
				List<Entry<Integer, WorldChunkCoord>> chunks = map.getHottestChunks(count);
				
				if(chunks.isEmpty())
				{
					sender.sendMessage(ChatColor.GOLD + "There is no data available yet.");
					return true;
				}
				
				sender.sendMessage(ChatColor.GOLD.toString() + chunks.size() + " Hottest Chunks:");
				
				for(Entry<Integer, WorldChunkCoord> entry : chunks)
					sender.sendMessage(ChatColor.translateAlternateColorCodes('&', String.format("  &e%d&7,&e%d %s &7(%d,??,%d) Heat: &c%d", entry.getValue().x, entry.getValue().z, Bukkit.getWorld(entry.getValue().world).getName(), entry.getValue().x*16+8, entry.getValue().z*16+8, entry.getKey())));
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
