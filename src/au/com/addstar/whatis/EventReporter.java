package au.com.addstar.whatis;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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
	
	public static void recordEventState(Event event, ReportingRegisteredListener listener, boolean cancelled)
	{
		synchronized (mCurrentReports)
		{
			EventReport report = mCurrentReports.get(event);
			if(report == null)
			{
				report = new EventReport(event.getClass());
				mCurrentReports.put(event, report);
			}
			
			report.recordStep(event, listener, cancelled);
		}
	}
	
	public static List<EventReport> popReports(Class<? extends Event> eventClass)
	{
		ArrayList<EventReport> reports = new ArrayList<EventReport>();
		synchronized (mCurrentReports)
		{
			for(EventReport report : mCurrentReports.values())
			{
				if(report.getEventType().equals(eventClass))
					reports.add(report);
			}
			
			mCurrentReports.values().removeAll(reports);
		}
		
		return reports;
	}
	
	private static HashMap<Class<? extends Event>, ReportSession> mCurrentMonitors = new HashMap<Class<? extends Event>, ReportSession>();
	
	public static void monitorEvent(Class<? extends Event> eventClass, int forTicks, CommandSender sender, File reportLocation) throws IllegalArgumentException, IllegalStateException
	{
		if(mCurrentMonitors.containsKey(eventClass))
			throw new IllegalStateException(eventClass.getName() + " is already being monitored");
		
		if(forTicks <= 0)
			throw new IllegalArgumentException("forTicks must be greater than 0");
		
		hookEvent(eventClass);
		
		if(sender == null)
			sender = Bukkit.getConsoleSender();
		
		ReportSession session = new ReportSession(sender, reportLocation);
		
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
			writeReport(reports, session.outputFile);
			session.output.sendMessage("Event report saved to " + session.outputFile.getPath());
		}
	}
	
	public static void writeReport(List<EventReport> reports, File file)
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
		
		for(EventReport report : reports)
		{
			
			writer.println("-----------------------------------------");
			writer.println("Event: " + report.getEventType().getName());
			writer.println("Handlers: " + report.getSteps().size());
			writer.println();
			
			for(Map.Entry<RegisteredListener, Map<String, Object>> step : report.getSteps())
			{
				String location = "";
				for(EventCallback callback : EventHelper.resolveListener(report.getEventType(), step.getKey()))
				{
					if(!location.isEmpty())
						location += "\n OR \n";
					location += String.format("[%s %s%s] %s", step.getKey().getPlugin().getName(), callback.priority, callback.ignoreCancelled ? " Ignores Cancel" : "", callback.signature);
				}
				
				if(location.isEmpty())
					location = String.format("[%s %s%s] %s", step.getKey().getPlugin().getName(), step.getKey().getPriority(), step.getKey().isIgnoringCancelled() ? " Ignores Cancel" : "", step.getKey().getListener().getClass().getName() + ".???");
				
				writer.println(location);
				writer.println(" - " + (step.getValue() == null ? "*SKIP*" : step.getValue()));
				writer.println();
			}
			writer.println();
		}
		
		writer.flush();
		writer.close();
	}
	
	public static class EventReport
	{
		private Class<? extends Event> mEventClass;
		private ArrayList<Map.Entry<RegisteredListener, Map<String, Object>>> mSteps = new ArrayList<Map.Entry<RegisteredListener,Map<String, Object>>>();
		
		public EventReport(Class<? extends Event> eventClass)
		{
			mEventClass = eventClass;
		}
		
		public synchronized void recordStep(Event event, ReportingRegisteredListener listener, boolean cancelled)
		{
			if(cancelled)
				mSteps.add(new AbstractMap.SimpleEntry<RegisteredListener, Map<String, Object>>(listener.getOriginal(), null));
			else
				mSteps.add(new AbstractMap.SimpleEntry<RegisteredListener, Map<String, Object>>(listener.getOriginal(), EventHelper.dumpClass(event)));
		}
		
		public synchronized Class<? extends Event> getEventType()
		{
			return mEventClass;
		}
		
		public synchronized List<Map.Entry<RegisteredListener, Map<String, Object>>> getSteps()
		{
			return mSteps;
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
			writeReport(reports, mSession.outputFile);
			mSession.output.sendMessage("Event report saved to " + mSession.outputFile.getPath());
				
			mCurrentMonitors.remove(mClass);
		}
	}
	
	private static class ReportSession
	{
		public ReportSession(CommandSender sender, File file)
		{
			output = sender;
			outputFile = file;
		}
		public BukkitTask task;
		public CommandSender output;
		public File outputFile;
	}
	
	
}
