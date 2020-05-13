package au.com.addstar.whatis;

import org.bukkit.Chunk;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredListener;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

public class EventHelper
{
	public static int maxDepth = 2;
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
		eventMap = new HashMap<>();
		HashSet<Class<? extends Event>> unique = new HashSet<>();
		HashSet<Listener> listeners = new HashSet<>();
		
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
	
	public static Collection<Class<? extends Event>> getEvents()
	{
		return eventMap.values();
	}
	
	public static Class<? extends Event> parseEvent(String name)
	{
		return eventMap.get(name.toLowerCase());
	}
	
	private static Map<String, Object> dumpEntity(Entity ent)
	{
		HashMap<String, Object> subMap = new HashMap<String, Object>();
		subMap.put("location", dumpClass(ent.getLocation(), 0));
		subMap.put("velocity", ent.getVelocity());
		subMap.put("ticks", ent.getTicksLived());
		subMap.put("world", ent.getWorld().getName());

		if(ent instanceof LivingEntity)
		{
			LivingEntity living = (LivingEntity)ent;
			subMap.put("maxAir", living.getMaximumAir());
			subMap.put("air", living.getRemainingAir());
			subMap.put("customName", living.getCustomName());
			subMap.put("canPickupItems", living.getCanPickupItems());
		}
		
		if(ent instanceof Damageable)
		{
			subMap.put("maxHealth", ((Damageable)ent).getMaxHealth());
			subMap.put("health", ((Damageable)ent).getHealth());
		}
		
		if(ent instanceof ExperienceOrb)
		{
			subMap.put("xp", ((ExperienceOrb) ent).getExperience());
		}
		
		if(ent instanceof HumanEntity)
		{
			HumanEntity human = (HumanEntity)ent;
			subMap.put("name", human.getName());
		}
		
		return subMap;
	}
	
	private static void dumpFields(Field[] fields, Object instance, Map<String, Object> map, int depth)
	{
		for(Field field : fields)
		{
			if(field.getType().equals(HandlerList.class))
				continue;
			
			if(Modifier.isStatic(field.getModifiers()))
				continue;
			
			field.setAccessible(true);
			try
			{
				Object contained = field.get(instance);
				
				if(contained == null)
					map.put(field.getName(), "null");
				else if(contained.getClass().isPrimitive() || contained.getClass().equals(Integer.class) || contained.getClass().equals(Short.class) || contained.getClass().equals(Byte.class) || contained.getClass().equals(String.class) || contained.getClass().equals(Boolean.class) || contained.getClass().equals(Float.class) || contained.getClass().equals(Double.class) || contained.getClass() == Character.class )
					map.put(field.getName(), String.valueOf(contained));
				else if(contained.getClass().isArray())
					map.put(field.getName(), Arrays.toString((Object[])contained));
				else if(contained instanceof Collection)
					map.put(field.getName(), contained.toString());
				else if(contained instanceof Server)
					map.put(field.getName(), contained.toString());
				else if(contained instanceof Entity)
					map.put(field.getName(), dumpEntity((Entity)contained));
				else if(contained instanceof Chunk)
					map.put(field.getName(), String.format("[X=%d Z=%d]", ((Chunk)contained).getX(), ((Chunk)contained).getZ()));
				else if(contained instanceof World)
					map.put(field.getName(), ((World)contained).getName());
				else
				{
					Map<String, Object> result = dumpClass(contained, depth + 1);
					if(result.isEmpty())
						map.put(field.getName(), contained.toString());
					else
						map.put(field.getName(), result);
				}
			}
			catch(Exception e)
			{
				map.put(field.getName(), "*ERROR*");
			}
		}
	}
	
	public static Map<String, Object> dumpClass(Object object)
	{
		return dumpClass(object, 0);
	}
	
	private static Map<String, Object> dumpClass(Object object, int depth)
	{
		if(depth > maxDepth)
			return Collections.emptyMap();
		
		HashMap<String, Object> result = new HashMap<String, Object>();
		
		Class<?> clazz = object.getClass();
		
		while(!clazz.equals(Object.class))
		{
			Field[] fields = clazz.getDeclaredFields();
			
			dumpFields(fields, object, result, depth);
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
