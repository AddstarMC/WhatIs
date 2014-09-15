package au.com.addstar.whatis;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import au.com.addstar.whatis.commands.ChunkCommand;
import au.com.addstar.whatis.commands.CommandCommand;
import au.com.addstar.whatis.commands.CommandDispatcher;
import au.com.addstar.whatis.commands.DependencyCommand;
import au.com.addstar.whatis.commands.EntityCommand;
import au.com.addstar.whatis.commands.EventMonitorCommand;
import au.com.addstar.whatis.commands.EventViewCommand;
import au.com.addstar.whatis.commands.FiltersCommand;
import au.com.addstar.whatis.commands.FindCommand;
import au.com.addstar.whatis.commands.ReportCommand;
import au.com.addstar.whatis.commands.TPSCommand;
import au.com.addstar.whatis.commands.TasksCommand;
import au.com.addstar.whatis.commands.VersionCommand;
import au.com.addstar.whatis.commands.WhatCancelledCommand;
import au.com.addstar.whatis.util.CommandFinder;

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
		
		CommandDispatcher whatis = new CommandDispatcher("whatis", "");
		whatis.registerCommand(new EventViewCommand());
		whatis.registerCommand(new CommandCommand());
		File reportDir = new File(getDataFolder(), "reports");
		reportDir.mkdirs();
		whatis.registerCommand(new ReportCommand(reportDir));
		whatis.registerCommand(new EventMonitorCommand());
		whatis.registerCommand(new TPSCommand());
		whatis.registerCommand(new DependencyCommand());
		whatis.registerCommand(new EntityCommand());
		whatis.registerCommand(new ChunkCommand());
		whatis.registerCommand(new WhatCancelledCommand());
		whatis.registerCommand(new TasksCommand());
		whatis.registerCommand(new FindCommand());
		whatis.registerCommand(new FiltersCommand());
		whatis.registerCommand(new VersionCommand());
		
		getCommand("whatis").setExecutor(whatis);
		getCommand("whatis").setTabCompleter(whatis);
		
		Bukkit.getScheduler().runTask(this, new Runnable()
		{
			@Override
			public void run()
			{
				EventHelper.buildEventMap();
			}
		});
		
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
		ArrayList<BukkitTask> tasks = new ArrayList<BukkitTask>();
		
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
