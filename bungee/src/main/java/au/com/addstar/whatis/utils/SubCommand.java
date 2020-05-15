package au.com.addstar.whatis.utils;

import net.md_5.bungee.api.CommandSender;


public interface SubCommand {
	String getName();
	String getDescription();
	String[] getAliases();
	
	String getUsage();
	String getPermission();
	
	boolean onCommand(CommandSender sender, String[] args) throws BadArgumentException;
	Iterable<String> onTabComplete(CommandSender sender, String[] args);
}
