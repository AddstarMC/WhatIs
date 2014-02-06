package au.com.addstar.whatis;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.RegisteredListener;
import org.bukkit.scheduler.BukkitTask;

import au.com.addstar.whatis.EventHelper.EventCallback;

public class EventReporter
{
	public static void hookEvent(Class<? extends Event> eventClass)
	{
		HandlerList list = EventHelper.getHandlers(eventClass);
		
		LinkedList<RegisteredListener> toRegister = new LinkedList<RegisteredListener>();
		RegisteredListener[] listeners = list.getRegisteredListeners();
		for(RegisteredListener listener : listeners)
		{
			if(listener instanceof ReportingRegisteredListener)
				continue;
			
			list.unregister(listener);
			toRegister.add(new ReportingRegisteredListener(listener));
		}
		
		list.registerAll(toRegister);
	}
	
	public static void restoreEvent(Class<? extends Event> eventClass)
	{
		HandlerList list = EventHelper.getHandlers(eventClass);
		
		LinkedList<RegisteredListener> toRegister = new LinkedList<RegisteredListener>();
		for(RegisteredListener listener : list.getRegisteredListeners())
		{
			if(!(listener instanceof ReportingRegisteredListener))
				continue;
			
			toRegister.add(((ReportingRegisteredListener)listener).getOriginal());
			list.unregister(listener);
		}
		
		list.registerAll(toRegister);
	}
	
	private static IdentityHashMap<Event, EventReport> mCurrentReports = new IdentityHashMap<Event, EventReport>();
	private static HashMap<Class<? extends Event>, ReportSession> mSessions = new HashMap<Class<? extends Event>, EventReporter.ReportSession>();
	private static HashMap<Class<? extends Event>, List<EventReport>> mEventOrder = new HashMap<Class<? extends Event>, List<EventReport>>();
	
	public static void recordEventInitialState(Event event, boolean cancelled)
	{
		synchronized (mCurrentReports)
		{
			EventReport report = mCurrentReports.get(event);
			ReportSession session = mSessions.get(event.getClass());
			
			if(report == null)
			{
				report = new EventReport(event.getClass());
				mCurrentReports.put(event, report);
				
				List<EventReport> list = mEventOrder.get(event.getClass());
				if(list == null)
				{
					list = new LinkedList<EventReporter.EventReport>();
					mEventOrder.put(event.getClass(), list);
				}
				
				list.add(report);
			}
			
			report.recordInitialStep(event, cancelled, session.filters);
		}
	}
	
	public static void recordEventState(Event event, ReportingRegisteredListener listener, boolean cancelled)
	{
		synchronized (mCurrentReports)
		{
			EventReport report = mCurrentReports.get(event);
			ReportSession session = mSessions.get(event.getClass());
			
			if(report == null)
			{
				report = new EventReport(event.getClass());
				mCurrentReports.put(event, report);
				
				List<EventReport> list = mEventOrder.get(event.getClass());
				if(list == null)
				{
					list = new LinkedList<EventReporter.EventReport>();
					mEventOrder.put(event.getClass(), list);
				}
				
				list.add(report);
			}
			
			for(Filter filter : session.filters)
			{
				if(!filter.listenerMatches(listener.getOriginal()))
					return;
			}
			report.recordStep(event, listener, cancelled);
		}
	}
	
	public static List<EventReport> popReports(Class<? extends Event> eventClass)
	{
		synchronized (mCurrentReports)
		{
			LinkedList<EventReport> onlyValid = new LinkedList<EventReport>();
			List<EventReport> reports = mEventOrder.remove(eventClass);
			
			if(reports == null)
				reports = Collections.emptyList();
			
			mCurrentReports.values().removeAll(reports);
			
			mSessions.remove(eventClass);
			
			for(EventReport report : reports)
			{
				if(report.isValid())
					onlyValid.add(report);
			}
			
			return onlyValid;
		}
	}
	
	private static HashMap<Class<? extends Event>, ReportSession> mCurrentMonitors = new HashMap<Class<? extends Event>, ReportSession>();
	
	public static void monitorEvent(Class<? extends Event> eventClass, int forTicks, CommandSender sender, File reportLocation, List<Filter> filters) throws IllegalArgumentException, IllegalStateException
	{
		if(mCurrentMonitors.containsKey(eventClass))
			throw new IllegalStateException(eventClass.getName() + " is already being monitored");
		
		if(forTicks <= 0)
			throw new IllegalArgumentException("forTicks must be greater than 0");
		
		if(sender == null)
			sender = Bukkit.getConsoleSender();
		
		ReportSession session = new ReportSession(sender, reportLocation, filters);
		mSessions.put(eventClass, session);
		
		hookEvent(eventClass);
		
		session.task = Bukkit.getScheduler().runTaskLater(WhatIs.instance, new EventReportTimer(eventClass, session), forTicks);
		mCurrentMonitors.put(eventClass, session);
	}
	
	public static void cancelMonitor(Class<? extends Event> eventClass)
	{
		ReportSession session = mCurrentMonitors.remove(eventClass);
		if(session != null)
		{
			restoreEvent(eventClass);
			List<EventReport> reports = popReports(eventClass);
			session.task.cancel();
			writeReport(reports, session.outputFile, session.filters);
			session.output.sendMessage("Event report saved to " + session.outputFile.getPath());
		}
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
	
	public static void writeReport(List<EventReport> reports, File file, List<Filter> filters)
	{
		PrintWriter writer;
		
		try
		{
			writer = new PrintWriter(file);
		}
		catch(FileNotFoundException e)
		{
			e.printStackTrace();
			return;
		}
		
		writer.println("-----------------------------------------");
		writer.println("  Event report:");
		writer.println("  Completed " + DateFormat.getDateTimeInstance().format(new Date()));
		writer.println("-----------------------------------------");
		writer.println();
		
		reportLoop: for(EventReport report : reports)
		{
			for(Filter filter : filters)
			{
				if(!filter.matches(report.getInitial().getData()))
					continue reportLoop;
			}
			
			writer.println("-----------------------------------------");
			writer.println("Event: " + report.getEventType().getName());
			writer.println("Handlers: " + report.getSteps().size());
			
			if(report.getInitial().isCancelled())
				writer.println(" - [Cancelled] " + makeDataBrief(report.getInitial().getData()));
			else
				writer.println(" - " + makeDataBrief(report.getInitial().getData()));
			
			writer.println();
			
			Map<String, Object> lastState = report.getInitial().getData(); 
			
			for(EventStep step : report.getSteps())
			{
				String location = "";
				for(EventCallback callback : EventHelper.resolveListener(report.getEventType(), step.getListener()))
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
		
		writer.flush();
		writer.close();
	}
	
	private static class EventStep
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
	
	public static class EventReport
	{
		private Class<? extends Event> mEventClass;
		private EventStep mInitial;
		private ArrayList<EventStep> mSteps = new ArrayList<EventStep>();
		private boolean mAllow;
		
		public EventReport(Class<? extends Event> eventClass)
		{
			mEventClass = eventClass;
			mAllow = true;
		}
		
		public synchronized void recordInitialStep(Event event, boolean cancelled, List<Filter> filters)
		{
			if(mInitial != null)
				return;
			
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
		
		public synchronized void recordStep(Event event, ReportingRegisteredListener listener, boolean cancelled)
		{
			if(!mAllow)
				return;
			
			boolean newCancel = false;
			if(event instanceof Cancellable)
				newCancel = ((Cancellable)event).isCancelled();
			
			if(cancelled && listener.isIgnoringCancelled())
				mSteps.add(new EventStep(listener.getOriginal(), null, newCancel));
			else
				mSteps.add(new EventStep(listener.getOriginal(), EventHelper.dumpClass(event), newCancel));
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
		
	}
	
	public static class EventReportTimer implements Runnable
	{
		private Class<? extends Event> mClass;
		private ReportSession mSession;
		
		public EventReportTimer(Class<? extends Event> clazz, ReportSession session)
		{
			mClass = clazz;
			mSession = session;
		}
		
		@Override
		public void run()
		{
			restoreEvent(mClass);
			List<EventReport> reports = popReports(mClass);
			writeReport(reports, mSession.outputFile, mSession.filters);
			mSession.output.sendMessage("Event report saved to " + mSession.outputFile.getPath());
				
			mCurrentMonitors.remove(mClass);
		}
	}
	
	private static class ReportSession
	{
		public ReportSession(CommandSender sender, File file, List<Filter> filters)
		{
			output = sender;
			outputFile = file;
			this.filters = filters;
		}
		public BukkitTask task;
		public CommandSender output;
		public File outputFile;
		public List<Filter> filters;
	}
	
	
}
