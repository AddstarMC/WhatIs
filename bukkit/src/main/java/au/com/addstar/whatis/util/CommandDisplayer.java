package au.com.addstar.whatis.util;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public interface CommandDisplayer
{
	boolean displayInfo(Command command, String label, CommandSender sender);
	
	String getSource(Command command);
}
