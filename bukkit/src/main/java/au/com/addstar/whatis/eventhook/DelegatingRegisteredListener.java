package au.com.addstar.whatis.eventhook;

import org.bukkit.event.Event;
import org.bukkit.event.EventException;
import org.bukkit.plugin.RegisteredListener;

public class DelegatingRegisteredListener extends RegisteredListener
{
	private RegisteredListener mExisting;
	
	public DelegatingRegisteredListener(RegisteredListener existing)
	{
		super(existing.getListener(), null, existing.getPriority(), existing.getPlugin(), existing.isIgnoringCancelled());
		mExisting = existing;
	}

	@Override
	public void callEvent( Event event ) throws EventException
	{
		mExisting.callEvent(event);
	}
	
	public RegisteredListener getOriginal()
	{
		return mExisting;
	}
}
