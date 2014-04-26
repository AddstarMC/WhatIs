package au.com.addstar.whatis.eventhook;

import org.bukkit.event.Event;
import org.bukkit.plugin.RegisteredListener;

public abstract class EventHookSession
{
	public synchronized void recordInitialStep(Event event)
	{
	}
	
	public synchronized void recordStep(Event event, RegisteredListener listener, boolean initallyCancelled)
	{
	}
}
