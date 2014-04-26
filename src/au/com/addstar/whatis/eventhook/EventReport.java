package au.com.addstar.whatis.eventhook;

import java.io.PrintWriter;
import java.text.DateFormat;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.plugin.RegisteredListener;

import au.com.addstar.whatis.EventHelper;
import au.com.addstar.whatis.Filter;
import au.com.addstar.whatis.EventHelper.EventCallback;

public class EventReport
{
	private Class<? extends Event> mEventClass;
	private EventStep mInitial;
	private ArrayList<EventStep> mSteps = new ArrayList<EventStep>();
	private boolean mAllow;
	private long mTimestamp;
	
	public EventReport(Class<? extends Event> eventClass)
	{
		mEventClass = eventClass;
		mAllow = true;
	}
	
	public synchronized void recordInitialStep(Event event, List<Filter> filters)
	{
		if(mInitial != null)
			return;
		
		mTimestamp = System.currentTimeMillis();
		
		boolean newCancel = false;
		if(event instanceof Cancellable)
			newCancel = ((Cancellable)event).isCancelled();
		
		Map<String, Object> dump = EventHelper.dumpClass(event);
		
		for(Filter filter : filters)
		{
			if(!filter.matches(dump))
			{
				mAllow = false;
				return;
			}
		}
		
		mInitial = new EventStep(null, dump, newCancel);
	}
	
	public synchronized void recordStep(Event event, RegisteredListener listener, boolean cancelled)
	{
		if(!mAllow)
			return;
		
		boolean newCancel = false;
		if(event instanceof Cancellable)
			newCancel = ((Cancellable)event).isCancelled();
		
		if(cancelled && listener.isIgnoringCancelled())
			mSteps.add(new EventStep(listener, null, newCancel));
		else
			mSteps.add(new EventStep(listener, EventHelper.dumpClass(event), newCancel));
	}
	
	public synchronized Class<? extends Event> getEventType()
	{
		return mEventClass;
	}
	
	public synchronized List<EventStep> getSteps()
	{
		return mSteps;
	}
	
	public synchronized EventStep getInitial()
	{
		return mInitial;
	}
	
	public synchronized boolean isValid()
	{
		return mAllow;
	}
	
	public synchronized long getTimestamp()
	{
		return mTimestamp;
	}
	
	public void print(PrintWriter writer)
	{
		writer.println("-----------------------------------------");
		writer.println("Event: " + mEventClass.getName());
		writer.println("Handlers: " + mSteps.size());
		writer.println("Timestamp: " + DateFormat.getDateTimeInstance().format(mTimestamp));
		
		if(mInitial.isCancelled())
			writer.println(" - [Cancelled] " + makeDataBrief(mInitial.getData()));
		else
			writer.println(" - " + makeDataBrief(mInitial.getData()));
		
		writer.println();
		
		Map<String, Object> lastState = mInitial.getData(); 
		
		for(EventStep step : mSteps)
		{
			String location = "";
			for(EventCallback callback : EventHelper.resolveListener(mEventClass, step.getListener()))
			{
				if(!location.isEmpty())
					location += "\n OR \n";
				location += String.format("[%s %s%s] %s", step.getListener().getPlugin().getName(), callback.priority, callback.ignoreCancelled ? " Ignores Cancel" : "", callback.signature);
			}
			
			if(location.isEmpty())
				location = String.format("[%s %s%s] %s", step.getListener().getPlugin().getName(), step.getListener().getPriority(), step.getListener().isIgnoringCancelled() ? " Ignores Cancel" : "", step.getListener().getListener().getClass().getName() + ".???");
			
			writer.println(location);
			if(step.isCancelled())
				writer.println(" - [Cancelled] " + (step.getData() == null ? "*SKIP*" : makeDataBrief(step.getData())));
			else
				writer.println(" - " + (step.getData() == null ? "*SKIP*" : makeDataBrief(step.getData())));
			
			if(step.getData() != null)
			{
				ArrayList<Entry<String, Object>> changes = new ArrayList<Map.Entry<String,Object>>();
				getChanges(step.getData(), lastState, changes);
				
				if(!changes.isEmpty())
				{
					writer.println("Changes:");
					
					for(Entry<String, Object> change : changes)
						writer.println(" - " + change.getKey() + ": " + change.getValue());
				}
				
				lastState = step.getData();
			}
			
			writer.println();
		}
		writer.println();
	}
	
	private static String makeDataBrief(Map<String, Object> data)
	{
		return data.toString();
	}
	
	@SuppressWarnings( "unchecked" )
	private static void getChanges(Map<String, Object> data, Map<String, Object> last, List<Entry<String, Object>> changes)
	{
		for(String key : data.keySet())
		{
			Object newVal,oldVal;
			newVal = data.get(key);
			oldVal = last.get(key);

			if(newVal instanceof Map && oldVal instanceof Map)
				getChanges((Map<String,Object>)newVal, (Map<String,Object>)oldVal, changes);
			else if((newVal == null && oldVal != null) || (newVal != null && !newVal.equals(oldVal)))
				changes.add(new AbstractMap.SimpleEntry<String, Object>(key, newVal));
		}
	}
	
	public static class EventStep
	{
		private RegisteredListener mListener;
		private Map<String, Object> mData;
		private boolean mIsCancelled;
		
		public EventStep(RegisteredListener listener, Map<String, Object> data, boolean cancelled)
		{
			mListener = listener;
			mData = data;
			mIsCancelled = cancelled;
		}
		
		public boolean isCancelled()
		{
			return mIsCancelled;
		}
		
		public Map<String, Object> getData()
		{
			return mData;
		}
		
		public RegisteredListener getListener()
		{
			return mListener;
		}
	}
}
