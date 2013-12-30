package au.com.addstar.whatis;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredListener;

public class EventHelper
{
	public static HandlerList getHandlers(Class<? extends Event> clazz)
	{
		try
		{
			Method method = clazz.getMethod("getHandlerList");
			return (HandlerList)method.invoke(null);
		}
		catch(NoSuchMethodException e)
		{
			return null;
		}
		catch(Exception e)
		{
			throw new RuntimeException(e);
		}
	}
	
	public static List<EventCallback> getEventCallbacks(Listener listener)
	{
		ArrayList<EventCallback> callbacks = new ArrayList<EventCallback>();
		
		Method[] methods = listener.getClass().getDeclaredMethods();
		
		for (Method method : methods) 
		{
            EventHandler handler = method.getAnnotation(EventHandler.class);
            if (handler == null)
            	continue;
            
            Class<?> checkClass;
            if (method.getParameterTypes().length != 1 || !Event.class.isAssignableFrom(checkClass = method.getParameterTypes()[0])) 
                continue;

            Class<? extends Event> eventClass = checkClass.asSubclass(Event.class);
            
            String args = "";
            for(Class<?> clazz : method.getParameterTypes())
            {
            	if(!args.isEmpty())
            		args += ", ";
            	
            	args += clazz.getSimpleName();
            }
            
            callbacks.add(new EventCallback(eventClass, handler.priority(), handler.ignoreCancelled(), String.format("%s.%s(%s)", listener.getClass().getName(), method.getName(), args)));
		}
		
		return callbacks;
	}
	
	public static List<EventCallback> getEventCallbacks(Plugin plugin)
	{
		ArrayList<EventCallback> callbacks = new ArrayList<EventHelper.EventCallback>();
		List<RegisteredListener> all = HandlerList.getRegisteredListeners(plugin);
		HashSet<Listener> unique = new HashSet<Listener>();
		
		for(RegisteredListener listener : all)
		{
			try
			{
				unique.add(listener.getListener());
			}
			catch(NullPointerException e)
			{
				// No idea why this happens
			}
		}
		
		for(Listener listener : unique)
			callbacks.addAll(getEventCallbacks(listener));

		return callbacks;
	}
	
	private static HashMap<String, Class<? extends Event>> eventMap;
	
	public static void buildEventMap()
	{
		eventMap = new HashMap<String, Class<? extends Event>>();
		HashSet<Class<? extends Event>> unique = new HashSet<Class<? extends Event>>();
		HashSet<Listener> listeners = new HashSet<Listener>();
		
		for(HandlerList list : HandlerList.getHandlerLists())
		{
			for(RegisteredListener listener : list.getRegisteredListeners())
			{
				try
				{
					listeners.add(listener.getListener());
				}
				catch(NullPointerException e) {}
			}
		}
		
		for(Listener listener : listeners)
		{
			List<EventCallback> callbacks = getEventCallbacks(listener);
			for(EventCallback callback : callbacks)
				unique.add(callback.eventClass);
		}
		
		for(Class<? extends Event> eventClass : unique)
		{
			String name = eventClass.getSimpleName();
			if(name.endsWith("Event"))
				name = name.substring(0, name.length() - 5);
			
			name = name.toLowerCase();
			
			if(eventMap.containsKey(name))
			{
				int count = 2;
				while(eventMap.containsKey(name + count))
					++count;
				
				name = name + count;
			}
			
			eventMap.put(name, eventClass);
		}
	}
	
	public static Set<String> getEventNames()
	{
		return eventMap.keySet();
	}
	
	public static Class<? extends Event> parseEvent(String name)
	{
		return eventMap.get(name.toLowerCase());
	}
	
	public static class EventCallback
	{
		public EventCallback(Class<? extends Event> clazz, EventPriority eventPriority, boolean ignore, String signature)
		{
			eventClass = clazz;
			priority = eventPriority;
			ignoreCancelled = ignore;
			this.signature = signature;
		}
		public Class<? extends Event> eventClass;
		public EventPriority priority;
		public boolean ignoreCancelled;
		public String signature;
		
		@Override
		public String toString()
		{
			return String.format("%s: %s ignoresCancelled? %s", signature, priority, ignoreCancelled);
		}
	}
}
