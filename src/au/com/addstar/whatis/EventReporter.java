package au.com.addstar.whatis;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.RegisteredListener;
import org.bukkit.scheduler.BukkitTask;

import au.com.addstar.whatis.eventhook.DelegatingRegisteredListener;
import au.com.addstar.whatis.eventhook.EventHookSession;
import au.com.addstar.whatis.eventhook.EventReportHook;
import au.com.addstar.whatis.eventhook.ReportingRegisteredListener;
import au.com.addstar.whatis.util.filters.FilterSet;

public class EventReporter
{
	private static HashSet<Class<? extends Event>> mHookedEvents = new HashSet<Class<? extends Event>>();  
	public static void hookEvent(Class<? extends Event> eventClass, EventHookSession hook)
	{
		Validate.isTrue(!mHookedEvents.contains(eventClass), "The specified event is already hooked");
		
		mHookedEvents.add(eventClass);
		HandlerList list = EventHelper.getHandlers(eventClass);
		
		LinkedList<RegisteredListener> toRegister = new LinkedList<RegisteredListener>();
		RegisteredListener[] listeners = list.getRegisteredListeners();
		for(RegisteredListener listener : listeners)
		{
			if(listener instanceof DelegatingRegisteredListener)
				continue;
			
			list.unregister(listener);
			toRegister.add(new ReportingRegisteredListener(hook, listener));
		}
		
		list.registerAll(toRegister);
	}
	
	public static void restoreEvent(Class<? extends Event> eventClass)
	{
		mHookedEvents.remove(eventClass);
		HandlerList list = EventHelper.getHandlers(eventClass);
		
		LinkedList<RegisteredListener> toRegister = new LinkedList<RegisteredListener>();
		for(RegisteredListener listener : list.getRegisteredListeners())
		{
			if(!(listener instanceof DelegatingRegisteredListener))
				continue;
			
			toRegister.add(((DelegatingRegisteredListener)listener).getOriginal());
			list.unregister(listener);
		}
		
		list.registerAll(toRegister);
	}
	
	private static HashMap<Class<? extends Event>, ReportSession> mCurrentMonitors = new HashMap<Class<? extends Event>, ReportSession>();
	
	public static void monitorEvent(Class<? extends Event> eventClass, int forTicks, CommandSender sender, File reportLocation, FilterSet filter) throws IllegalArgumentException, IllegalStateException
	{
		if(mCurrentMonitors.containsKey(eventClass))
			throw new IllegalStateException(eventClass.getName() + " is already being monitored");
		
		if(forTicks <= 0)
			throw new IllegalArgumentException("forTicks must be greater than 0");
		
		if(sender == null)
			sender = Bukkit.getConsoleSender();
		
		EventReportHook hook = new EventReportHook(filter);
		ReportSession session = new ReportSession(hook, sender, reportLocation, eventClass);
		mCurrentMonitors.put(eventClass, session);
		
		session.start(forTicks);
	}
	
	public static void cancelMonitor(Class<? extends Event> eventClass)
	{
		ReportSession session = mCurrentMonitors.remove(eventClass);
		if(session != null)
			session.cancel();
	}
	
	private static class ReportSession implements Runnable
	{
		public EventReportHook hook;
		public CommandSender sender;
		public File output;
		private BukkitTask mTask;
		private Class<? extends Event> mClass;
		
		public ReportSession(EventReportHook hook, CommandSender sender, File output, Class<? extends Event> eventClass)
		{
			this.hook = hook;
			this.sender = sender;
			this.output = output;
			mClass = eventClass;
		}
		
		public void start(int ticks)
		{
			hookEvent(mClass, hook);
			
			mTask = Bukkit.getScheduler().runTaskLater(WhatIs.instance, this, ticks);
		}
		
		public void cancel()
		{
			if(mTask != null)
			{
				mTask.cancel();
				onFinish();
				mTask = null;
			}
		}
		
		@Override
		public void run()
		{
			onFinish();
			mTask = null;
		}
		
		private void onFinish()
		{
			restoreEvent(mClass);
			mCurrentMonitors.remove(mClass);
			
			try
			{
				if(hook.getReportCount() != 0)
				{
					hook.save(output);
					sender.sendMessage(ChatColor.GOLD + "[WhatIs]" + ChatColor.WHITE + " Event report saved");
					sender.sendMessage(ChatColor.YELLOW + output.getPath());
					sender.sendMessage(ChatColor.GRAY + "Recorded " + hook.getReportCount() + " events");
				}
				else
					sender.sendMessage("No matching events were detected");
			}
			catch(IOException e)
			{
				e.printStackTrace();
				sender.sendMessage(ChatColor.RED + "An error occured while saving the report. Please check the console.");
			}
		}
	}
}
