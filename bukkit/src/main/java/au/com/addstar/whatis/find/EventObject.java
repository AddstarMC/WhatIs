package au.com.addstar.whatis.find;

import org.bukkit.event.Event;
import org.bukkit.plugin.Plugin;

public class EventObject implements IObject
{
	private Class<? extends Event> mClass;
	
	public EventObject(Class<? extends Event> clazz)
	{
		mClass = clazz;
	}
	
	@Override
	public String getType()
	{
		return "Event";
	}

	@Override
	public String getName()
	{
		return mClass.getSimpleName();
	}

	@Override
	public String getDescription()
	{
		return mClass.getName();
	}

	@Override
	public Plugin getOwner()
	{
		return null;
	}

	@Override
	public int compareTo( IObject o )
	{
		return getName().compareTo(o.getName());
	}
}
