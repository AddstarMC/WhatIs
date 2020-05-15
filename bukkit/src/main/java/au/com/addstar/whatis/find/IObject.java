package au.com.addstar.whatis.find;

import org.bukkit.plugin.Plugin;

public interface IObject extends Comparable<IObject>
{
	String getType();
	
	String getName();
	
	String getDescription();
	
	Plugin getOwner();
}
