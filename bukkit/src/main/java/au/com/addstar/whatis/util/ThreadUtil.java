package au.com.addstar.whatis.util;

import java.lang.management.ThreadInfo;
import java.util.LinkedHashSet;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

public class ThreadUtil
{
	public static Plugin getPlugin(StackTraceElement element)
	{
		Class<?> frameClass;
		try
		{
			frameClass = Class.forName(element.getClassName());
		}
		catch(ClassNotFoundException e)
		{
			return null;
		}
		
		ClassLoader loader = frameClass.getClassLoader();
		
		for(Plugin plugin : Bukkit.getPluginManager().getPlugins())
		{
			if(loader == plugin.getClass().getClassLoader())
				return plugin;
		}
		
		return null;
	}
	
	public static Plugin[] getPlugins(StackTraceElement[] stackTrace)
	{
		LinkedHashSet<Plugin> plugins = new LinkedHashSet<Plugin>();
		for(StackTraceElement frame : stackTrace)
		{
			Plugin plugin = getPlugin(frame);
			if(plugin != null)
				plugins.add(plugin);
		}
		
		return plugins.toArray(new Plugin[plugins.size()]);
	}
	
	public static String getThreadOwner(ThreadInfo thread)
	{
		StackTraceElement[] stacktrace = thread.getStackTrace();
		
		for(int i = stacktrace.length-1; i >= 0; --i)
		{
			StackTraceElement frame = stacktrace[i];
			Plugin plugin = getPlugin(frame);
			if(plugin != null)
				return plugin.getName();
			String className = frame.getClassName();
			if(className.startsWith("net.minecraft.server"))
				return "Minecraft";
		}
		
		return null;
	}
}
