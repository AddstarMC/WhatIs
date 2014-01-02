package au.com.addstar.whatis;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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
	
	public static List<EventCallback> resolveListener(Class<? extends Event> eventClass, RegisteredListener listener)
	{
		List<EventCallback> callbacks = getEventCallbacks(listener.getListener());
		Iterator<EventCallback> it = callbacks.iterator();
		
		while(it.hasNext())
		{
			EventCallback callback = it.next();
			if(!callback.eventClass.equals(eventClass))
				it.remove();
			else if(callback.priority != listener.getPriority())
				it.remove();
		}
		
		return callbacks;
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
	
	private static void dumpFields(Field[] fields, Object instance, Map<String, Object> map)
	{
		for(Field field : fields)
		{
			if(field.getType().equals(HandlerList.class))
				continue;
			
			field.setAccessible(true);
			try
			{
				Object contained = field.get(instance);
				
				if(contained == null)
					map.put(field.getName(), "null");
				else if(contained.getClass().isPrimitive() || contained.getClass().equals(Integer.class) || contained.getClass().equals(Short.class) || contained.getClass().equals(Byte.class) || contained.getClass().equals(String.class) || contained.getClass().equals(Boolean.class) || contained.getClass().equals(Float.class) || contained.getClass().equals(Double.class))
					map.put(field.getName(), String.valueOf(contained));
				else if(contained.getClass().isArray())
					map.put(field.getName(), Arrays.toString((Object[])contained));
				else
					map.put(field.getName(), contained.toString());
			}
			catch(Exception e)
			{
				map.put(field.getName(), "*ERROR*");
			}
		}
	}
	public static Map<String, Object> dumpClass(Object object)
	{
		HashMap<String, Object> result = new HashMap<String, Object>();
		
		Class<?> clazz = object.getClass();
		
		while(!clazz.equals(Object.class))
		{
			dumpFields(clazz.getDeclaredFields(), object, result);
			clazz = clazz.getSuperclass();
		}
		
		return result;
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
