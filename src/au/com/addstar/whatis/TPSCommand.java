package au.com.addstar.whatis;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import au.com.addstar.whatis.util.Graph;

public class TPSCommand implements ICommand
{
	private Graph mTickGraph = new Graph();
	private Graph mTPSGraph = new Graph();
	
	public TPSCommand()
	{
		mTickGraph.addColourStop(0, ChatColor.GREEN);
		mTickGraph.addColourStop(55000000, ChatColor.YELLOW);
		mTickGraph.addColourStop(66000000, ChatColor.RED);
		mTPSGraph.addColourStop(18, ChatColor.GREEN);
		mTPSGraph.addColourStop(15, ChatColor.YELLOW);
		mTPSGraph.addColourStop(0, ChatColor.RED);
	}
	
	@Override
	public String getName()
	{
		return "tps";
	}

	@Override
	public String[] getAliases()
	{
		return null;
	}

	@Override
	public String getPermission()
	{
		return "whatis.tps";
	}

	@Override
	public String getUsageString( String label, CommandSender sender )
	{
		return label;
	}

	@Override
	public String getDescription()
	{
		return "Shows the current Ticks per second of the server.";
	}

	@Override
	public boolean canBeConsole()
	{
		return true;
	}

	@Override
	public boolean canBeCommandBlock()
	{
		return true;
	}

	@Override
	public boolean onCommand( CommandSender sender, String label, String[] args )
	{
		if(args.length != 0)
			return false;
		
		TickMonitor monitor = WhatIs.instance.getTickMonitor();
		
		double tps = monitor.getCurrentTPS();
		
		if(tps >= 18)
			sender.sendMessage(ChatColor.translateAlternateColorCodes('&', String.format("&6Current TPS&7: &a%.2f&7tps &a%.3f &7ms/t", tps, monitor.getAverageTickTime() / 1000000D)));
		else if(tps >= 15)
			sender.sendMessage(ChatColor.translateAlternateColorCodes('&', String.format("&6Current TPS&7: &e%.2f&7tps &e%.3f &7ms/t", tps, monitor.getAverageTickTime() / 1000000D)));
		else
			sender.sendMessage(ChatColor.translateAlternateColorCodes('&', String.format("&6Current TPS&7: &c%.2f&7tps &c%.3f &7ms/t", tps, monitor.getAverageTickTime() / 1000000D)));
		
		long max = monitor.getMaxTickTime();
		long min = monitor.getMinTickTime();
		long[] tickHistory = monitor.getHistory();
		
		sender.sendMessage(ChatColor.translateAlternateColorCodes('&', String.format("&6Last %d Ticks&7: \u2206 &f%.4f&7 ms", tickHistory.length, (max - min) / 1000000D)));
		sender.sendMessage(ChatColor.translateAlternateColorCodes('&', String.format(" &6Min&7: &f%.2f &7ms", min / 1000000D)));
		sender.sendMessage(ChatColor.translateAlternateColorCodes('&', String.format(" &6Max&7: &f%.2f &7ms", max / 1000000D)));
		
		mTickGraph.setData(tickHistory, 50000000, max);
		mTickGraph.draw(sender);
		
		double maxTps = monitor.getMaxTPS();
		double minTps = monitor.getMinTPS();
		double[] tpsHistory = monitor.getTPSHistory();
		
		sender.sendMessage(ChatColor.translateAlternateColorCodes('&', String.format("&6TPS over last %d seconds&7: \u2206 &f%.2f&7tps", tpsHistory.length, maxTps - minTps)));
		sender.sendMessage(ChatColor.translateAlternateColorCodes('&', String.format(" &6Min&7: &f%.2f&7tps", minTps)));
		sender.sendMessage(ChatColor.translateAlternateColorCodes('&', String.format(" &6Max&7: &f%.2f&7tps", maxTps)));
		
		mTPSGraph.setData(tpsHistory, minTps, 20);
		mTPSGraph.draw(sender);
		
		return true;
	}

	@Override
	public List<String> onTabComplete( CommandSender sender, String label,
			String[] args )
	{
		return null;
	}

}
