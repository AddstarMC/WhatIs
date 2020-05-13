package au.com.addstar.whatis;

import au.com.addstar.whatis.commands.*;
import au.com.addstar.whatis.util.CommandFinder;
import au.com.addstar.whatis.util.ReflectionUtil;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class WhatIs extends JavaPlugin
{
	public static WhatIs instance;
	private TickMonitor mTickMonitor;
	private ThreadLockChecker mThreadChecker;
	
	@Override
	public void onEnable()
	{
		instance = this;
		CommandFinder.init();
		
		CommandDispatcher whatIs = new CommandDispatcher("whatis", "");
		whatIs.registerCommand(new EventViewCommand());
		whatIs.registerCommand(new CommandCommand());
		File reportDir = new File(getDataFolder(), "reports");
		if(!reportDir.exists() && reportDir.mkdirs()){
			this.getLogger().warning("Whatis - Could not create report directory");
		}
		ReflectionUtil.setLogger(getLogger());
		whatIs.registerCommand(new ReportCommand(reportDir));
		whatIs.registerCommand(new EventMonitorCommand());
		whatIs.registerCommand(new TPSCommand());
		whatIs.registerCommand(new DependencyCommand());
		whatIs.registerCommand(new EntityCommand());
		whatIs.registerCommand(new ChunkCommand());
		whatIs.registerCommand(new WhatCancelledCommand());
		whatIs.registerCommand(new TasksCommand());
		whatIs.registerCommand(new FindCommand());
		whatIs.registerCommand(new FiltersCommand());
		whatIs.registerCommand(new VersionCommand());
		whatIs.registerCommand(new PrintFieldCommand());
		whatIs.registerCommand(new ChannelCommand());
		getCommand("whatis").setExecutor(whatIs);
		getCommand("whatis").setTabCompleter(whatIs);
		
		Bukkit.getScheduler().runTaskLaterAsynchronously(this, EventHelper::buildEventMap,10);
		
		mTickMonitor = new TickMonitor(120);
		Bukkit.getScheduler().runTaskTimer(this, mTickMonitor, 1, 1);
		
		mThreadChecker = new ThreadLockChecker();
		mThreadChecker.start();
		ThreadLockChecker.ignore();
		Bukkit.getPluginManager().registerEvents(new ThreadLockIgnoreEvents(), this);
	}
	
	public TickMonitor getTickMonitor()
	{
		return mTickMonitor;
	}
	
	public static File getPluginSource(Plugin plugin)
	{
		if(plugin instanceof JavaPlugin)
		{
			try
			{
				Field field = JavaPlugin.class.getDeclaredField("file");
				field.setAccessible(true);
				return (File)field.get(plugin);
			}
			catch(Exception e)
			{
				// Wont happen
				return null;
			}
		}
		
		return null;
	}
	
	public static List<BukkitTask> getPluginTasks(Plugin plugin)
	{
		List<BukkitTask> tasks = new ArrayList<>();
		
		for(BukkitTask pending : Bukkit.getScheduler().getPendingTasks())
		{
			if(pending.getOwner() == plugin)
				tasks.add(pending);
		}
		
		return tasks;
	}
	
	private static Method mGetTaskClass = null;

	public static String getTaskSource(BukkitTask task)
	{
		try
		{
			if(mGetTaskClass == null)
			{
				Class<?> clazz = task.getClass();
				while(clazz != null && !clazz.getSimpleName().equals("CraftTask"))
					clazz = clazz.getSuperclass();
				
				if(clazz == null)
					return null;
				
				mGetTaskClass = clazz.getDeclaredMethod("getTaskClass");
				mGetTaskClass.setAccessible(true);
			}
			
			@SuppressWarnings( "unchecked" )
			Class<? extends Runnable> clazz = (Class<? extends Runnable>)mGetTaskClass.invoke(task);
			
			return clazz.getName();
		}
		catch(NullPointerException e)
		{
			return "Unknown";
		}
		catch(Exception e)
		{
			throw new RuntimeException(e);
		}
	}
	
	private static String mCBVersion;
	public static Class<?> getVersionedClass(String name)
	{
		if (mCBVersion == null)
		{
			String clss = Bukkit.getServer().getClass().getName();
			String[] parts = clss.split("\\.");
			mCBVersion = parts[3];
		}
		
		String[] parts = name.split("\\.");
		if (name.startsWith("net.minecraft.server"))
			parts = (String[]) ArrayUtils.add(parts, 3, mCBVersion);
		else if (name.startsWith("org.bukkit.craftbukkit"))
			parts = (String[]) ArrayUtils.add(parts, 3, mCBVersion);
		
		name = StringUtils.join(parts, ".");
		try
		{
			return Class.forName(name);
		}
		catch(ClassNotFoundException e)
		{
			return null;
		}
	}
}
