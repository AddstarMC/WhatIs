package au.com.addstar.whatis.eventhook;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityEvent;
import org.bukkit.event.entity.EntityTameEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.inventory.FurnaceExtractEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.vehicle.VehicleDamageEvent;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.event.vehicle.VehicleEntityCollisionEvent;
import org.bukkit.event.vehicle.VehicleEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.event.weather.WeatherEvent;
import org.bukkit.event.world.ChunkEvent;
import org.bukkit.event.world.WorldEvent;
import org.bukkit.scheduler.BukkitTask;

import au.com.addstar.whatis.WhatIs;
import au.com.addstar.whatis.util.Callback;

public class HookRunner<T extends EventHookSession>
{
	private DurationTarget mTarget;
	private T mHook;
	private Callback<T> mCallback;
	
	private BukkitTask mTask;
	
	public HookRunner(DurationTarget target, T hook, Callback<T> callback)
	{
		mTarget = target;
		mHook = hook;
		mHook.setRunner(this);
		mCallback = callback;
		
		mTask = Bukkit.getScheduler().runTaskLater(WhatIs.instance, new Runnable()
		{
			@Override
			public void run()
			{
				stop();
			}
		}, target.ticks);
	}
	
	public HookRunner(DurationTarget target, T hook)
	{
		this(target, hook, null);
	}
	
	public void stop()
	{
		if(mTask != null)
		{
			mTask.cancel();
			mHook.unhook();
			mHook.onStop();
			mTask = null;
			
			if(mCallback != null)
				mCallback.onCompleted(mHook);
		}
	}
	
	public void checkConditions(Event event, boolean initallyCancelled)
	{
		if(!(event instanceof Cancellable) || initallyCancelled || !((Cancellable)event).isCancelled() || !mTarget.untilCancel)
			return;
		
		if(mTarget.onlyPlayer != null)
		{
			if(!mTarget.onlyPlayer.equals(getPlayer(event)))
				return;
		}
		
		if(mTarget.onlyWorld != null)
		{
			if(!mTarget.onlyWorld.equals(getWorld(event)))
				return;
		}
		
		Bukkit.getScheduler().runTask(WhatIs.instance, new Runnable()
		{
			@Override
			public void run()
			{
				stop();
			}
		});
	}
	
	public boolean shouldInclude(Event event)
	{
		if(mTarget.onlyPlayer != null)
		{
			if(!mTarget.onlyPlayer.equals(getPlayer(event)))
				return false;
		}
		
		if(mTarget.onlyWorld != null)
		{
			if(!mTarget.onlyWorld.equals(getWorld(event)))
				return false;
		}
		
		return true;
	}
	
	private static Player getPlayer(Event event)
	{
		if(event instanceof PlayerEvent)
			return ((PlayerEvent) event).getPlayer();
		else if(event instanceof EntityEvent && ((EntityEvent)event).getEntity() instanceof Player)
			return (Player)((EntityEvent)event).getEntity();
		else if(event instanceof EntityDamageByEntityEvent && ((EntityDamageByEntityEvent)event).getDamager() instanceof Player)
			return (Player)((EntityDamageByEntityEvent)event).getDamager();
		else if(event instanceof EntityTameEvent && ((EntityTameEvent)event).getOwner() instanceof Player)
			return (Player)((EntityTameEvent)event).getOwner();
		else if(event instanceof EntityTargetEvent && ((EntityTargetEvent)event).getTarget() instanceof Player)
			return (Player)((EntityTargetEvent)event).getTarget();
		else if(event instanceof BlockPlaceEvent)
			return ((BlockPlaceEvent)event).getPlayer();
		else if(event instanceof BlockDamageEvent)
			return ((BlockDamageEvent) event).getPlayer();
		else if(event instanceof BlockBreakEvent)
			return ((BlockBreakEvent) event).getPlayer();
		else if(event instanceof FurnaceExtractEvent)
			return ((FurnaceExtractEvent) event).getPlayer();
		else if(event instanceof BlockIgniteEvent)
			return ((BlockIgniteEvent) event).getPlayer();
		else if(event instanceof SignChangeEvent)
			return ((SignChangeEvent) event).getPlayer();
		else if(event instanceof VehicleEnterEvent && ((VehicleEnterEvent) event).getEntered() instanceof Player)
			return (Player)((VehicleEnterEvent) event).getEntered();
		else if(event instanceof VehicleExitEvent && ((VehicleExitEvent) event).getExited() instanceof Player)
			return (Player)((VehicleExitEvent) event).getExited();
		else if(event instanceof VehicleDamageEvent && ((VehicleDamageEvent)event).getAttacker() instanceof Player)
			return (Player)((VehicleDamageEvent)event).getAttacker();
		else if(event instanceof VehicleDestroyEvent && ((VehicleDestroyEvent)event).getAttacker() instanceof Player)
			return (Player)((VehicleDestroyEvent)event).getAttacker();
		else if(event instanceof VehicleEntityCollisionEvent && ((VehicleEntityCollisionEvent)event).getEntity() instanceof Player)
			return (Player)((VehicleEntityCollisionEvent)event).getEntity();
		else if(event instanceof HangingPlaceEvent)
			return ((HangingPlaceEvent) event).getPlayer();
		else if(event instanceof HangingBreakByEntityEvent && ((HangingBreakByEntityEvent)event).getRemover() instanceof Player)
			return (Player)((HangingBreakByEntityEvent)event).getRemover();
		
		return null;
	}
	
	private static World getWorld(Event event)
	{
		if(event instanceof WorldEvent)
			return ((WorldEvent) event).getWorld();
		else if(event instanceof ChunkEvent)
			return ((ChunkEvent) event).getWorld();
		else if(event instanceof WeatherEvent)
			return ((WeatherEvent) event).getWorld();
		else if(event instanceof BlockEvent)
			return ((BlockEvent) event).getBlock().getWorld();
		else if(event instanceof EntityEvent)
			return ((EntityEvent) event).getEntity().getWorld();
		else if(event instanceof VehicleEvent)
			return ((VehicleEvent) event).getVehicle().getWorld();
		else if(event instanceof HangingEvent)
			return ((HangingEvent) event).getEntity().getWorld();
		else if (event instanceof PlayerEvent)
			return ((PlayerEvent) event).getPlayer().getWorld();
		
		return null;
	}
}
