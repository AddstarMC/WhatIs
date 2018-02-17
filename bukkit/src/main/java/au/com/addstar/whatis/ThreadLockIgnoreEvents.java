package au.com.addstar.whatis;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.world.WorldInitEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;

public class ThreadLockIgnoreEvents implements Listener
{
	@EventHandler(priority=EventPriority.NORMAL, ignoreCancelled=true)
	public void onWorldLoad(WorldLoadEvent event)
	{
		ThreadLockChecker.ignore();
	}
	
	@EventHandler(priority=EventPriority.NORMAL, ignoreCancelled=true)
	public void onWorldUnload(WorldUnloadEvent event)
	{
		ThreadLockChecker.ignore();
	}
	
	@EventHandler(priority=EventPriority.NORMAL, ignoreCancelled=true)
	public void onWorldInit(WorldInitEvent event)
	{
		ThreadLockChecker.ignore();
	}
	
	@EventHandler(priority=EventPriority.NORMAL, ignoreCancelled=true)
	public void onPluginDisable(PluginDisableEvent event)
	{
		ThreadLockChecker.ignore();
	}
}
