package au.com.addstar.whatis.util;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public interface CommandDisplayer
{
	public boolean displayInfo( Command command, String label, CommandSender sender);
	
	public String getSource( Command command );
}
