package au.com.addstar.whatis.commands;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import au.com.addstar.whatis.WhatIs;

public class VersionCommand implements ICommand
{
	@Override
	public String getName()
	{
		return "version";
	}

	@Override
	public String[] getAliases()
	{
		return null;
	}

	@Override
	public String getPermission()
	{
		return "whatis.version";
	}

	@Override
	public String getUsageString( String label, CommandSender sender )
	{
		return label + " [player]";
	}

	@Override
	public String getDescription()
	{
		return "Description shows what version (approximately) of minecraft players are using";
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
		if (args.length > 1)
			return false;
		
		Player player = null;
		
		if (args.length == 1)
		{
			player = Bukkit.getPlayer(args[0]);
			if (player == null)
			{
				sender.sendMessage(ChatColor.RED + "Unknown player " + args[0]);
				return true;
			}
		}
		
		if (player == null)
		{
			// Server version
			sender.sendMessage(ChatColor.WHITE + "Server version is " + ChatColor.GOLD + Bukkit.getVersion() + ChatColor.WHITE + " Implementing Bukkit " + ChatColor.GOLD + Bukkit.getBukkitVersion());
			Set<Integer> protocols = getSupportedProtocols();
			if (!protocols.isEmpty())
			{
				StringBuilder builder = new StringBuilder();
				for(Integer protocol : protocols)
				{
					if (builder.length() != 0)
						builder.append(", ");
					builder.append(getMCVersionString(protocol));
					builder.append(" (");
					builder.append(protocol);
					builder.append(")");
				}
				
				sender.sendMessage(ChatColor.GOLD + "Supported minecrafts: " + ChatColor.GRAY + builder);
			}
		}
		else
		{
			// MC version
			int protocol = getProtocolVersion(player);
			if (protocol < 0)
				sender.sendMessage(ChatColor.RED + "An internal exception occured while running this command");
			else
				sender.sendMessage(ChatColor.GOLD + player.getName() + ChatColor.WHITE + " is running Minecraft " + ChatColor.GOLD + getMCVersionString(protocol) + ChatColor.WHITE + " (protocol " + ChatColor.GOLD + protocol + ChatColor.WHITE + ")");
		}
		return true;
	}
	
	private String getMCVersionString(int protocol)
	{
		switch(protocol)
		{
		case 4:
			return "1.7.0-1.7.5";
		case 5:
			return "1.7.6-1.7.10";
		case 47:
			return "1.8.0+";
		default:
			return "Unknown";
		}
	}
	
	private MethodHandle mGetHandle;
	private Field mGetPlayerConnection;
	private Field mGetNetworkManager;
	private MethodHandle mGetVersion;
	
	public int getProtocolVersion(Player player)
	{
		if (mGetHandle == null)
		{
			try
			{
				mGetHandle = MethodHandles.lookup().unreflect(player.getClass().getMethod("getHandle"));
				mGetPlayerConnection = mGetHandle.type().returnType().getField("playerConnection");
				mGetNetworkManager = mGetPlayerConnection.getType().getField("networkManager");
			
				mGetVersion = null;
				try
				{
					Method getVersion = mGetNetworkManager.getType().getMethod("getVersion");
					mGetVersion = MethodHandles.lookup().unreflect(getVersion);
				}
				catch(NoSuchMethodException e)
				{
					// Pre 1.7.6
				}
			}
			catch(NoSuchFieldException e)
			{
				// Should not happen
				e.printStackTrace();
				return -1;
			}
			catch ( IllegalAccessException e )
			{
				// Should not happen
				e.printStackTrace();
				return -1;
			}
			catch(NoSuchMethodException e)
			{
				// Should not happen
				e.printStackTrace();
				return -1;
			}
		}
		
		try
		{
			Object handle = mGetHandle.invoke(player);
			Object connection = mGetPlayerConnection.get(handle);
			Object networkManager = mGetNetworkManager.get(connection);
			if (mGetVersion != null)
				return (Integer)mGetVersion.invoke(networkManager);
			else
				return 4;
		}
		catch(Throwable e)
		{
			e.printStackTrace();
			return -1;
		}
	}
	
	private Set<Integer> mProtocols;
	@SuppressWarnings( "unchecked" )
	public Set<Integer> getSupportedProtocols()
	{
		if (mProtocols == null)
		{
			Class<?> netManager = WhatIs.getVersionedClass("net.minecraft.server.NetworkManager");
			mProtocols = Collections.emptySet();
			if (netManager != null)
			{
				try
				{
					Field protocols = netManager.getField("SUPPORTED_VERSIONS");
					mProtocols = (Set<Integer>)protocols.get(null);
				}
				catch(NoSuchFieldException e)
				{
					// 1.7.5 or earlier
				}
				catch ( IllegalArgumentException | IllegalAccessException e )
				{
					// Should not happen
					e.printStackTrace();
				}
			}
		}
		
		return mProtocols;
	}

	@Override
	public List<String> onTabComplete( CommandSender sender, String label, String[] args )
	{
		return null;
	}

}
