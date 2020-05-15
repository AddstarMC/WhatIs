package au.com.addstar.whatis.eventhook;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.EventException;
import org.bukkit.plugin.RegisteredListener;

public class ReportingRegisteredListener extends DelegatingRegisteredListener
{
	private final EventHookSession mSession;
	public ReportingRegisteredListener(EventHookSession session, RegisteredListener existing)
	{
		super(existing);
		mSession = session;
	}

	@Override
	public void callEvent( Event event ) throws EventException
	{
		boolean canceled = false;
		if(event instanceof Cancellable)
			canceled = ((Cancellable)event).isCancelled();
		
		mSession.recordInitialStep(event);
		
		super.callEvent(event);
		mSession.recordStep(event, getOriginal(), canceled);
	}
}
