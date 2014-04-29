package au.com.addstar.whatis.eventhook;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.List;

import org.bukkit.event.Event;
import org.bukkit.plugin.RegisteredListener;
import au.com.addstar.whatis.Filter;

public class EventReportHook extends EventHookSession
{
	private List<Filter> mFilters;
	private IdentityHashMap<Event, EventReport> mCurrentReports;
	
	private ArrayList<EventReport> mEvents;
	
	public EventReportHook(List<Filter> filters)
	{
		mFilters = filters;
		mCurrentReports = new IdentityHashMap<Event, EventReport>();
		mEvents = new ArrayList<EventReport>();
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
				report = new EventReport(event.getClass());
				mCurrentReports.put(event, report);
				mEvents.add(report);
			}
			
			report.recordInitialStep(event, mFilters);
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
			
			for(Filter filter : mFilters)
			{
				if(!filter.listenerMatches(listener))
					return;
			}
			
			report.recordStep(event, listener, initallyCancelled);
		}
		
		super.recordStep(event, listener, initallyCancelled);
	}
	
	public synchronized List<EventReport> getReports()
	{
		LinkedList<EventReport> onlyValid = new LinkedList<EventReport>();
		
		for(EventReport report : mEvents)
		{
			if(report.isValid())
				onlyValid.add(report);
		}
		
		return onlyValid;
	}
	
	public List<Filter> getFilters()
	{
		return mFilters;
	}
	
	public void print(PrintWriter writer)
	{
		writer.println("-----------------------------------------");
		writer.println("  Event report:");
		writer.println("  Completed " + DateFormat.getDateTimeInstance().format(new Date()));
		writer.println("-----------------------------------------");
		writer.println();
		
		reportLoop: for(EventReport report : mEvents)
		{
			for(Filter filter : mFilters)
			{
				if(!filter.matches(report.getInitial().getData()))
					continue reportLoop;
			}
			
			report.print(writer);
		}
		
		writer.flush();
	}
	
	public void save(File file) throws IOException
	{
		PrintWriter writer = new PrintWriter(file);
		print(writer);
		writer.close();
	}
}
