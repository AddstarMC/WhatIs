package au.com.addstar.whatis.find;

import org.bukkit.plugin.Plugin;

public interface IObject extends Comparable<IObject>
{
	public String getType();
	
	public String getName();
	
	public String getDescription();
	
	public Plugin getOwner();
}
