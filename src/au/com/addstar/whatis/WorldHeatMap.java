package au.com.addstar.whatis;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.Map.Entry;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

public class WorldHeatMap implements Listener
{
	private HashMap<UUID, HashSet<Chunk>> mActiveChunks;
	private HashMap<WorldChunkCoord, Integer> mHeatMap;
	private Plugin mPlugin;
	
	private BukkitTask mTask;

	private WorldHeatMap(Plugin plugin)
	{
		Bukkit.getPluginManager().registerEvents(this, plugin);
		
		mPlugin = plugin;
		
		mHeatMap = new HashMap<WorldChunkCoord, Integer>();
		mActiveChunks = new HashMap<UUID, HashSet<Chunk>>();
	}
	
	private void applyActiveChunks(World world)
	{
		Chunk[] active = world.getLoadedChunks();
		HashSet<Chunk> activeChunks = new HashSet<Chunk>(active.length + 1000);
		for(Chunk c : active)
			activeChunks.add(c);
		
		mActiveChunks.put(world.getUID(), activeChunks);
	}
	
	public void start()
	{
		Validate.isTrue(mTask == null, "Heat map is already running");
		
		for(World world : Bukkit.getWorlds())
			applyActiveChunks(world);
		
		mTask = Bukkit.getScheduler().runTaskTimer(mPlugin, new HeatMapUpdater(), 20L, 20L);
	}
	
	public void finish()
	{
		Validate.notNull(mTask, "Heat map is not running");
		HandlerList.unregisterAll(this);
		mTask.cancel();
		mTask = null;
	}
	
	public boolean isRunning()
	{
		return mTask != null;
	}
	
	@EventHandler(priority=EventPriority.MONITOR, ignoreCancelled=true)
	private void onChunkLoad(ChunkLoadEvent event)
	{
		mActiveChunks.get(event.getWorld().getUID()).add(event.getChunk());
	}
	
	@EventHandler(priority=EventPriority.MONITOR, ignoreCancelled=true)
	private void onChunkUnload(ChunkUnloadEvent event)
	{
		mActiveChunks.get(event.getWorld().getUID()).remove(event.getChunk());
	}
	
	@EventHandler(priority=EventPriority.MONITOR, ignoreCancelled=true)
	private void onWorldLoad(WorldLoadEvent event)
	{
		applyActiveChunks(event.getWorld());
	}
	
	@EventHandler(priority=EventPriority.MONITOR, ignoreCancelled=true)
	private void onWorldUnload(WorldUnloadEvent event)
	{
		mActiveChunks.remove(event.getWorld().getUID());
	}
	
	private void increaseHeat(Chunk chunk)
	{
		WorldChunkCoord coords = new WorldChunkCoord(chunk.getX(), chunk.getZ(), chunk.getWorld().getUID());
		Integer heat = mHeatMap.get(coords);
		if(heat == null)
			heat = 1;
		else
			++heat;
		
		mHeatMap.put(coords, heat);
	}
	
	private void decreaseHeat(Chunk chunk)
	{
		WorldChunkCoord coords = new WorldChunkCoord(chunk.getX(), chunk.getZ(), chunk.getWorld().getUID());
		Integer heat = mHeatMap.get(coords);
		if(heat == null)
			return;
		else
		{
			if(heat > 0)
				--heat;
		}
		
		mHeatMap.put(coords, heat);
	}
	
	private boolean isTPSOk()
	{
		return WhatIs.instance.getTickMonitor().getCurrentTPS() >= 18;
	}
	
	public List<Entry<Integer, WorldChunkCoord>> getHottestChunks(int count)
	{
		ArrayList<Entry<Integer, WorldChunkCoord>> chunks = new ArrayList<Entry<Integer,WorldChunkCoord>>();
		Comparator<Entry<Integer, WorldChunkCoord>> comparator = new Comparator<Entry<Integer,WorldChunkCoord>>()
		{
			@Override
			public int compare( Entry<Integer, WorldChunkCoord> o1, Entry<Integer, WorldChunkCoord> o2 )
			{
				return o1.getKey().compareTo(o2.getKey()) * -1;
			}
		};
		
		int lowest = 0;
		
		for(Entry<WorldChunkCoord, Integer> entry : mHeatMap.entrySet())
		{
			if(chunks.size() < count || entry.getValue() > lowest)
			{
				Entry<Integer, WorldChunkCoord> ent = new AbstractMap.SimpleEntry<Integer, WorldChunkCoord>(entry.getValue(), entry.getKey());
				int insert = Collections.binarySearch(chunks, ent, comparator);
				if(insert < 0)
					insert = (1 + insert) * -1;
				
				chunks.add(insert, ent);
				
				if(chunks.size() > count)
					chunks.remove(chunks.size()-1);
			}
		}
		
		return chunks;
	}
	
	
	private static WorldHeatMap mInstance;
	
	public static WorldHeatMap getHeatMap()
	{
		if(mInstance == null)
			mInstance = new WorldHeatMap(WhatIs.instance);
		
		return mInstance;
	}
	
	private class HeatMapUpdater implements Runnable
	{
		@Override
		public void run()
		{
			boolean good = isTPSOk();
			
			for(Entry<UUID, HashSet<Chunk>> list : mActiveChunks.entrySet())
			{
				for(Chunk chunk : list.getValue())
				{
					if(good)
						decreaseHeat(chunk);
					else
						increaseHeat(chunk);
				}
			}
		}
	}
}
