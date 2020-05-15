package au.com.addstar.whatis;

import au.com.addstar.whatis.eventhook.DelegatingRegisteredListener;
import au.com.addstar.whatis.eventhook.EventHookSession;
import au.com.addstar.whatis.eventhook.EventOutput;
import au.com.addstar.whatis.eventhook.EventReportHook;
import au.com.addstar.whatis.eventhook.PasteEventOutput;
import au.com.addstar.whatis.eventhook.ReportingRegisteredListener;
import au.com.addstar.whatis.util.filters.FilterSet;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.RegisteredListener;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import org.kitteh.pastegg.Paste;
import org.kitteh.pastegg.PasteBuilder;
import org.kitteh.pastegg.PasteFile;

import java.io.IOException;
import java.util.*;

public class EventReporter
{
	private static final Collection<Class<? extends Event>> mHookedEvents = new HashSet<>();
	public static void hookEvent(Class<? extends Event> eventClass, EventHookSession hook)
	{
		Validate.isTrue(!mHookedEvents.contains(eventClass), "The specified event is already hooked");
		
		mHookedEvents.add(eventClass);
		HandlerList list = EventHelper.getHandlers(eventClass);
		
		Queue<RegisteredListener> toRegister = new LinkedList<>();
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
		
		Collection<RegisteredListener> toRegister = new LinkedList<>();
		for(RegisteredListener listener : list.getRegisteredListeners())
		{
			if(!(listener instanceof DelegatingRegisteredListener))
				continue;
			
			toRegister.add(((DelegatingRegisteredListener)listener).getOriginal());
			list.unregister(listener);
		}
		
		list.registerAll(toRegister);
	}
	
	private static final HashMap<Class<? extends Event>, ReportSession> mCurrentMonitors = new HashMap<>();
	
	public static void monitorEvent(Class<? extends Event> eventClass, int forTicks, CommandSender sender, EventOutput reportLocation, FilterSet filter) throws IllegalArgumentException, IllegalStateException
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
		public final EventReportHook hook;
		public final CommandSender sender;
		public final EventOutput output;
		private BukkitTask mTask;
		private final Class<? extends Event> mClass;
		
		public ReportSession(EventReportHook hook, CommandSender sender, EventOutput output, Class<? extends Event> eventClass)
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
					hook.save(output.getWriter());
					if(output instanceof PasteEventOutput) {
						Bukkit.getScheduler().runTaskAsynchronously(WhatIs.instance, () -> {
							PasteBuilder.PasteResult result = new PasteBuilder()
								.addFile(new PasteFile("Whatis Event Report - " + mClass.getSimpleName(),((PasteEventOutput) output).getContent()))
								.expireIn(60*60*1000)
								.build();
							if(result.getPaste().isPresent()) {
								Paste paste = result.getPaste().get();
								sender.sendMessage(ChatColor.GOLD + "[WhatIs]" + ChatColor.WHITE + " Event report saved");
								sender.sendMessage(ChatColor.GOLD + "[WhatIs]" + ChatColor.WHITE + " https://paste.gg/"+paste.getId());
								sender.sendMessage(ChatColor.GOLD + "[WhatIs]" + ChatColor.WHITE + " Deletion Key: "+paste.getDeletionKey());
								sender.sendMessage(ChatColor.GRAY + "Recorded " + hook.getReportCount() + " events");
							}
						});
						return;
					}
					sender.sendMessage(ChatColor.GOLD + "[WhatIs]" + ChatColor.WHITE + " Event report saved");
					sender.sendMessage(ChatColor.YELLOW + output.getDescription());
					sender.sendMessage(ChatColor.GRAY + "Recorded " + hook.getReportCount() + " events");
				}
				else
					sender.sendMessage("No matching events were detected");
			}
			catch(IOException e)
			{
				e.printStackTrace();
				sender.sendMessage(ChatColor.RED + "An error occurred while saving the report. Please check the console.");
			}
		}
	}
}
