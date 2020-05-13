package au.com.addstar.whatis.eventhook;

import java.io.PrintWriter;
import java.io.Writer;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.List;

import org.bukkit.event.Event;
import org.bukkit.plugin.RegisteredListener;

import au.com.addstar.whatis.util.filters.FilterSet;

public class EventReportHook extends EventHookSession
{
	private final FilterSet mFilter;
	private final  IdentityHashMap<Event, EventReport> mCurrentReports;
	
	private final ArrayList<EventReport> mEvents;
	
	public EventReportHook(FilterSet filter)
	{
		mFilter = filter;
		mCurrentReports = new IdentityHashMap<>();
		mEvents = new ArrayList<>();
	}
	
	public int getReportCount()
	{
		return mCurrentReports.size();
	}
	
	@Override
	public synchronized void recordInitialStep( Event event )
	{
		if(!shouldInclude(event))
			return;
		
		synchronized (mCurrentReports)
		{
			EventReport report = mCurrentReports.get(event);
			
			if(report == null)
			{
				if (mFilter != null && !mFilter.matches(event))
					return;
				
				report = new EventReport(event.getClass(), mFilter);
				mCurrentReports.put(event, report);
				mEvents.add(report);
			}
			
			report.recordInitialStep(event);
		}
	}
	
	@Override
	public synchronized void recordStep( Event event, RegisteredListener listener, boolean initallyCancelled )
	{
		if(!shouldInclude(event))
			return;
		
		synchronized (mCurrentReports)
		{
			EventReport report = mCurrentReports.get(event);

			if(report == null)
			{
				// Should not happen
				return;
			}
			
			if(mFilter != null && !mFilter.matchesHandler(listener))
				return;
			
			report.recordStep(event, listener, initallyCancelled);
		}
		
		super.recordStep(event, listener, initallyCancelled);
	}
	
	public synchronized List<EventReport> getReports()
	{
		LinkedList<EventReport> onlyValid = new LinkedList<>();
		
		for(EventReport report : mEvents)
		{
			if(report.isValid())
				onlyValid.add(report);
		}
		
		return onlyValid;
	}
	
	public void print(PrintWriter writer)
	{
		writer.println("-----------------------------------------");
		writer.println("  Event report:");
		writer.println("  Completed " + DateFormat.getDateTimeInstance().format(new Date()));
		writer.println("-----------------------------------------");
		writer.println();
		
		for(EventReport report : mEvents)
		{
			if(report.isValid())
				report.print(writer);
		}
		
		writer.flush();
	}
	
	public void save(Writer file) {
		PrintWriter writer = new PrintWriter(file);
		print(writer);
		writer.close();
	}
}
