package au.com.addstar.whatis.utils;

import net.md_5.bungee.api.CommandSender;


public interface SubCommand {
	public String getName();
	public String getDescription();
	public String[] getAliases();
	
	public String getUsage();
	public String getPermission();
	
	public boolean onCommand(CommandSender sender, String[] args) throws BadArgumentException;
	public Iterable<String> onTabComplete(CommandSender sender, String[] args);
}
