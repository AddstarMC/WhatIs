package au.com.addstar.whatis.entities;

import au.com.addstar.whatis.util.Callback;
import au.com.addstar.whatis.util.ChunkCoord;
import com.google.common.collect.HashMultimap;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;

import java.util.*;

public class EntityConcentrationMap
{
	private final HashSet<EntityGroup> mAllGroups;
	private final HashMultimap<ChunkCoord, EntityGroup> mChunkGroups;
	private final Plugin mPlugin;
	
	private final HashMap<World, List<Entity>> mBuildBuffer;
	private boolean mIsBuilding;
	private Callback<EntityConcentrationMap> mCallback;
	
	private List<EntityGroup> mOrdered;
	
	public EntityConcentrationMap(Plugin plugin)
	{
		mAllGroups = new HashSet<>();
		mChunkGroups = HashMultimap.create();
		mBuildBuffer = new HashMap<>();
		
		mPlugin = plugin;
		mIsBuilding = false;
	}
	
	// WARNING: BuildThread only
	private Set<EntityGroup> getNearby(ChunkCoord coord)
	{
		HashSet<EntityGroup> allGroups = null;
		
		for(SimpleFacing face : SimpleFacing.values())
		{
			ChunkCoord neighbour = ChunkCoord.getChunkCoord(coord.x + face.getModX(), coord.z + face.getModZ(), coord.world);
			
			Collection<EntityGroup> groups = mChunkGroups.get(neighbour);
			if(groups != null && !groups.isEmpty())
			{
				if(allGroups == null)
					allGroups = new HashSet<>(groups);
				else
					allGroups.addAll(groups);
			}
		}
		
		if(allGroups == null)
			return Collections.emptySet();
		else
			return allGroups;
	}
	
	// WARNING: BuildThread only
	private void updateChunkRegistrations(EntityGroup group, World world)
	{
		double radius = group.getRadius();
		int minX = ((int)(group.getLocation().getBlockX() - radius) >> 4);
		int minZ = ((int)(group.getLocation().getBlockZ() - radius) >> 4);
		
		int maxX = ((int)(group.getLocation().getBlockX() + radius) >> 4);
		int maxZ = ((int)(group.getLocation().getBlockZ() + radius) >> 4);
		
		for(int x = minX; x <= maxX; ++x)
		{
			for(int z = minZ; z <= maxZ; ++z)
				mChunkGroups.put(ChunkCoord.getChunkCoord(x, z, world), group);
		}
	}
	
	// WARNING: BuildThread only
	private void unregister(EntityGroup group, World world, boolean all)
	{
		double radius = group.getRadius();
		int minX = ((int)(group.getLocation().getBlockX() - radius) >> 4);
		int minZ = ((int)(group.getLocation().getBlockZ() - radius) >> 4);
		
		int maxX = ((int)(group.getLocation().getBlockX() + radius) >> 4);
		int maxZ = ((int)(group.getLocation().getBlockZ() + radius) >> 4);
		
		if(all)
			mAllGroups.remove(group);
		
		for(int x = minX; x <= maxX; ++x)
		{
			for(int z = minZ; z <= maxZ; ++z)
				mChunkGroups.remove(ChunkCoord.getChunkCoord(x, z, world), group);
		}
	}
	
	// WARNING: BuildThread only
	private EntityGroup doMerges(EntityGroup group, World world)
	{
		double radius = group.getRadius();
		int minX = ((int)(group.getLocation().getBlockX() - radius) >> 4);
		int minZ = ((int)(group.getLocation().getBlockZ() - radius) >> 4);
		
		int maxX = ((int)(group.getLocation().getBlockX() + radius) >> 4);
		int maxZ = ((int)(group.getLocation().getBlockZ() + radius) >> 4);
		
		for(int x = minX; x <= maxX; ++x)
		{
			for(int z = minZ; z <= maxZ; ++z)
			{
				Set<EntityGroup> groups = mChunkGroups.get(ChunkCoord.getChunkCoord(x, z, world));
				if(groups == null)
					continue;
				
				for(EntityGroup g : groups)
				{
					if(g != group && g.shouldMergeWith(group))
					{
						unregister(group, world, false);
						group.mergeWith(g);
						unregister(g, world, true);
						updateChunkRegistrations(group, world);
						
						// Call again to merge with any other groups needed
						return doMerges(group, world);
					}
				}
			}
		}
		return group;
	}
	
	// WARNING: BuildThread only
	private EntityGroup expandToInclude(EntityGroup group, Location location)
	{
		unregister(group, location.getWorld(), false);
		group.mergeWith(location);
		updateChunkRegistrations(group, location.getWorld());
		
		return doMerges(group, location.getWorld());
	}
	
	// WARNING: BuildThread only
	private void recordEntity(Entity entity, Location location, ChunkCoord chunk, Iterable<EntityGroup> possibles)
	{
		EntityGroup group = null;
		
		if(possibles != null)
		{
			for(EntityGroup g : possibles)
			{
				if(g.isInGroup(location))
				{
					group = g;
					break;
				}
			}
		}
		
		if(group == null)
		{
			Set<EntityGroup> nearby = getNearby(chunk);
			if(nearby != null)
			{
				for(EntityGroup g : nearby)
				{
					if(g.isTooClose(location))
					{
						group = expandToInclude(g, location);
						break;
					}
				}
			}
			
			if(group == null)
			{
				group = new EntityGroup(location.clone());
				updateChunkRegistrations(group, location.getWorld());
				mAllGroups.add(group);
			}
		}
		
		group.addEntity(entity);
	}
	
	// WARNING: BuildThread only
	private void processWorld(World world)
	{
		Location temp = new Location(null, 0, 0, 0);
		for(Entity entity : mBuildBuffer.get(world))
		{
			entity.getLocation(temp);
			ChunkCoord coord = ChunkCoord.getChunkCoord(temp.getBlockX() >> 4, temp.getBlockZ() >> 4, world);
			
			recordEntity(entity, temp, coord, mChunkGroups.get(coord));
		}
		
		ChunkCoord.clearCache();
	}
	
	// WARNING: BuildThread only
	private void orderGroups()
	{
		mOrdered = new ArrayList<>(mAllGroups.size());
		
		for(EntityGroup group : mAllGroups)
		{
			// These are not really groups
			if(group.getTotalCount() < 2)
				continue;
			
			int index = Collections.binarySearch(mOrdered, group);
			if(index < 0)
				index = (index + 1) * -1;
			
			mOrdered.add(index, group);
		}
	}
	
	private void onBuildComplete()
	{
		mIsBuilding = false;
		if(mCallback != null)
			mCallback.onCompleted(this);
	}
	
	public void queueWorld(World world)
	{
		Validate.isTrue(!mIsBuilding, "A build is in progress!");
		
		mBuildBuffer.put(world, world.getEntities());
	}
	
	public void queueAll()
	{
		Validate.isTrue(!mIsBuilding, "A build is in progress!");
		
		for(World world : Bukkit.getWorlds())
			queueWorld(world);
	}
	
	public void build(Callback<EntityConcentrationMap> callback)
	{
		Validate.isTrue(!mIsBuilding, "A build is already in progress!");
		
		mIsBuilding = true;
		mCallback = callback;
		BuildThread thread = new BuildThread();
		thread.start();
	}
	
	public void reset()
	{
		Validate.isTrue(!mIsBuilding, "A build is in progress!");
		
		mAllGroups.clear();
		mChunkGroups.clear();
	}
	
	public List<EntityGroup> getAllGroups()
	{
		Validate.isTrue(!mIsBuilding, "A build is in progress!");
		
		return mOrdered;
	}
	
	private class BuildThread extends Thread
	{
		@Override
		public void run()
		{
			mAllGroups.clear();
			mChunkGroups.clear();
			
			for(World world : mBuildBuffer.keySet())
				processWorld(world);
			
			for(EntityGroup group : mAllGroups)
			{
				group.stripOutliers();
				group.adjustBoundingSphere();
				group.finalizeCounts();
				group.buildCauses();
			}
			
			orderGroups();
			
			mBuildBuffer.clear();
			mChunkGroups.clear();
			mAllGroups.clear();
			
			Bukkit.getScheduler().runTask(mPlugin, () -> onBuildComplete());
		}
	}
}
