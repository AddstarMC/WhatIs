package au.com.addstar.whatis.eventhook;

import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.plugin.RegisteredListener;

import au.com.addstar.whatis.EventHelper;
import au.com.addstar.whatis.Filter;
import au.com.addstar.whatis.QuickMonitor.CancelReport;

public class CancelHook extends EventHookSession
{
	private IdentityHashMap<Event, CancelReport> mCancelled;
	private List<Filter> mFilters;
	
	public CancelHook()
	{
		mCancelled = new IdentityHashMap<Event, CancelReport>();
	}
	
	public CancelHook(List<Filter> filters)
	{
		mCancelled = new IdentityHashMap<Event, CancelReport>();
		mFilters = filters;
	}
	
	@Override
	public synchronized void recordStep( Event event, RegisteredListener listener, boolean initallyCancelled )
	{
		if(!shouldInclude(event) || !matchesHandlerFilters(event, listener))
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
	
	public boolean matchesHandlerFilters(Event event, RegisteredListener listener)
	{
		if(mFilters == null)
			return true;

		Map<String, Object> dump = EventHelper.dumpClass(event);
		
		for(Filter filter : mFilters)
		{
			if(!filter.listenerMatches(listener) || !filter.matches(dump))
				return false;
		}
		
		return true;
	}
	
	public Collection<CancelReport> getReports()
	{
		synchronized(mCancelled)
		{
			return Collections.unmodifiableCollection(mCancelled.values());
		}
	}
}
