package au.com.addstar.whatis.eventhook;

import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.plugin.RegisteredListener;

import au.com.addstar.whatis.QuickMonitor.CancelReport;

public class CancelHook extends EventHookSession
{
	private IdentityHashMap<Event, CancelReport> mCancelled;
	
	public CancelHook()
	{
		mCancelled = new IdentityHashMap<Event, CancelReport>();
	}
	
	@Override
	public synchronized void recordStep( Event event, RegisteredListener listener, boolean initallyCancelled )
	{
		if(!shouldInclude(event))
			return;
		
		synchronized(mCancelled)
		{
			boolean cancelled = (event instanceof Cancellable ? ((Cancellable)event).isCancelled() : false);
			
			if(!initallyCancelled && cancelled)
				mCancelled.put(event, new CancelReport(event.getClass(), listener));
			else if(initallyCancelled && !cancelled)
				mCancelled.remove(event);
		}
		super.recordStep(event, listener, initallyCancelled);
	}
	
	public Collection<CancelReport> getReports()
	{
		synchronized(mCancelled)
		{
			return Collections.unmodifiableCollection(mCancelled.values());
		}
	}
}
