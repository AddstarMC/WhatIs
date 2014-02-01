package au.com.addstar.whatis;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.EventException;
import org.bukkit.plugin.RegisteredListener;

public class ReportingRegisteredListener extends RegisteredListener
{
	private RegisteredListener mExisting;
	
	public ReportingRegisteredListener(RegisteredListener existing)
	{
		super(existing.getListener(), null, existing.getPriority(), existing.getPlugin(), existing.isIgnoringCancelled());
		mExisting = existing;
	}

	@Override
	public void callEvent( Event event ) throws EventException
	{
		boolean canceled = false;
		if(event instanceof Cancellable)
			canceled = ((Cancellable)event).isCancelled();
		
		EventReporter.recordEventInitialState(event, canceled);
		
		mExisting.callEvent(event);
		EventReporter.recordEventState(event, this, canceled);
	}
	
	public RegisteredListener getOriginal()
	{
		return mExisting;
	}
}
