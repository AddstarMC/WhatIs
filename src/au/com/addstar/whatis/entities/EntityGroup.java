package au.com.addstar.whatis.entities;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.lang.Validate;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;

import au.com.addstar.whatis.ChunkUtil;

public class EntityGroup implements Comparable<EntityGroup>
{
	public static double defaultRadius = 8;
	private static int mNextId = 0;
	
	private int mId;
	
	private int mCount;
	private int[] mTypeCounts;
	private double mRadius = 0;
	private Location mLocation;
	
	private double mMeanDistance;
	
	private ArrayList<Entity> mEntities;
	
	private HashSet<String> mChunkCauses;
	
	public EntityGroup(Location location)
	{
		mId = mNextId++;
		
		mLocation = location;
		mRadius = defaultRadius * defaultRadius;
		mTypeCounts = new int[EntityCategory.values().length];
		mEntities = new ArrayList<Entity>();
		mChunkCauses = new HashSet<String>();
	}
	
	public void addEntity(Entity entity)
	{
		++mTypeCounts[EntityCategory.from(entity.getType()).ordinal()];
		mEntities.add(entity);
	}
	
	public Location getLocation()
	{
		return mLocation;
	}
	
	public double getRadius()
	{
		return Math.sqrt(mRadius);
	}
	
	public double getRadiusSq()
	{
		return mRadius;
	}
	
	public boolean isInGroup(Location location)
	{
		if(mLocation.getWorld() != location.getWorld())
			return false;
		
		return (mLocation.distanceSquared(location) <= mRadius);
	}
	
	public boolean isTooClose(Location location)
	{
		if(mLocation.getWorld() != location.getWorld())
			return false;
		
		return (mLocation.distanceSquared(location) <= (mRadius + (defaultRadius*defaultRadius)));
	}
	
	public void mergeWith(EntityGroup other)
	{
		Validate.isTrue(other.mLocation.getWorld() == mLocation.getWorld());

		// Shift this groups locations by the radius of the other group, towards the other group
		double radiusOther = other.getRadius();
		Vector vec = other.mLocation.toVector().subtract(mLocation.toVector()).normalize().multiply(radiusOther);
		mLocation.add(vec);
		
		// Now expand my radius so i still cover the area I had before
		mRadius += other.mRadius;
		
		for(EntityCategory cat : EntityCategory.values())
			mTypeCounts[cat.ordinal()] += other.mTypeCounts[cat.ordinal()];
		
		mEntities.addAll(other.mEntities);
	}
	
	public void mergeWith(Location location)
	{
		Validate.isTrue(location.getWorld() == mLocation.getWorld());

		// Shift this groups locations by the radius of the other group, towards the other group
		double radiusOther = defaultRadius;
		Vector vec = location.toVector().subtract(mLocation.toVector()).normalize().multiply(radiusOther);
		mLocation.add(vec);
		
		// Now expand my radius so i still cover the area I had before
		mRadius += defaultRadius * defaultRadius;
	}
	
	public boolean shouldMergeWith(EntityGroup other)
	{
		if(mLocation.getWorld() != other.mLocation.getWorld())
			return false;
		
		return (mLocation.distanceSquared(other.mLocation) < (mRadius + other.mRadius));
	}
	
	private double getSmallestDistanceToNeighbour(Entity entity, Location temp)
	{
		double min = Double.MAX_VALUE;
		Location loc = entity.getLocation();
		for(Entity ent : mEntities)
		{
			if(ent == entity)
				continue;
			
			ent.getLocation(temp);
			
			double dist = temp.distanceSquared(loc);
			if(dist < min)
				min = dist;
		}
		
		return min;
	}
	
	public void stripOutliers()
	{
		if(mEntities.size() <= 2)
			return;
		
		double mean = 0;
		Location temp = new Location(null, 0, 0, 0);
		
		ArrayList<Double> dists = new ArrayList<Double>(mEntities.size());
		
		for(Entity ent : mEntities)
		{
			double dist = getSmallestDistanceToNeighbour(ent, temp);
			
			mean += dist;
			dists.add(dist);
		}
		
		mean /= mEntities.size();
		mMeanDistance = mean;
		
		// Calculate STD dev
		double stdDev = 0;
		for(double dist : dists)
			stdDev += Math.pow(dist - mean, 2);
		
		stdDev = Math.sqrt(stdDev / mEntities.size());
		
		Iterator<Entity> it = mEntities.iterator();
		while(it.hasNext())
		{
			Entity ent = it.next();
			double dist = getSmallestDistanceToNeighbour(ent, temp);
			if(dist - mean > 2 * stdDev)
				it.remove();
		}
	}
	
	public void adjustBoundingSphere()
	{
		if(mEntities.size() < 3)
			return;
		
		Location temp = new Location(null, 0, 0, 0);
		
		Entity point1;
		Entity point2;
		
		point1 = getFurthestFrom(mEntities.get(0), temp);
		point2 = getFurthestFrom(point1, temp);
		
		mLocation = point1.getLocation().add(point2.getLocation()).multiply(0.5);
		mRadius = point1.getLocation().distance(point2.getLocation()) / 2D;
		
		while(true)
		{
			Entity outside = null;
			double dist = 0;
			
			for(Entity ent : mEntities)
			{
				ent.getLocation(temp);
				dist = temp.distance(mLocation);
				if(dist - 2 > mRadius)
				{
					outside = ent;
					break;
				}
			}
			
			if(outside == null)
				break;

			dist += mRadius;
			mRadius = dist / 2;
			
			Vector vec = temp.toVector().subtract(mLocation.toVector());
			vec.normalize();
			vec.multiply(mRadius);

			temp.subtract(vec);
			
			Location temp2 = mLocation;
			mLocation = temp;
			temp = temp2;

			Validate.isTrue(outside.getLocation().distance(mLocation) <= mRadius + 2, "Sphere did not move to encompas the point. Rad: " + mRadius + " Req: " + temp.distance(mLocation));
		}
		
		mRadius = mRadius * mRadius;
	}
	
	private Entity getFurthestFrom(Entity entity, Location temp)
	{
		double max = Double.MIN_VALUE;
		Entity maxEnt = entity;
		
		Location loc = entity.getLocation();
		for(Entity ent : mEntities)
		{
			if(ent == entity)
				continue;
			
			ent.getLocation(temp);
			
			double dist = temp.distanceSquared(loc);
			if(dist > max)
			{
				max = dist;
				maxEnt = ent;
			}
		}
		
		return maxEnt;
	}
	
	public void finalizeCounts()
	{
		mCount = mEntities.size();
		mTypeCounts = new int[EntityCategory.values().length];
		
		for(Entity ent : mEntities)
			++mTypeCounts[EntityCategory.from(ent.getType()).ordinal()];
		
		mEntities.clear();
		mEntities = null;
	}
	
	public void buildCauses()
	{
		double radius = getRadius();
		int minX = ((int)(mLocation.getBlockX() - radius) >> 4);
		int minZ = ((int)(mLocation.getBlockZ() - radius) >> 4);
		
		int maxX = ((int)(mLocation.getBlockX() + radius) >> 4);
		int maxZ = ((int)(mLocation.getBlockZ() + radius) >> 4);
		
		for(int x = minX; x <= maxX; ++x)
		{
			for(int z = minZ; z <= maxZ; ++z)
				mChunkCauses.addAll(ChunkUtil.getChunkAnchors(mLocation.getWorld(), x, z));
		}
	}
	
	public int getTotalCount()
	{
		return mCount;
	}
	
	public int getCount(EntityCategory category)
	{
		return mTypeCounts[category.ordinal()];
	}
	
	public float getDensity()
	{
		// Treat it as a 2D area
		return mCount / (float)(Math.PI * mRadius);
	}
	
	public float getSpacing()
	{
		return (float)mMeanDistance;
	}
	
	public Set<String> getCauses()
	{
		return mChunkCauses;
	}
	
	@Override
	public String toString()
	{
		return String.format("Group{%d,%d,%d,%s-%d Entities: %d}", mLocation.getBlockX(), mLocation.getBlockY(), mLocation.getBlockZ(), mLocation.getWorld().getName(), (int)getRadius(), mCount);
	}

	@Override
	public int compareTo( EntityGroup o )
	{
		return Integer.valueOf(mCount).compareTo(o.mCount) * -1; // Higher first
	}
	
	@Override
	public boolean equals( Object obj )
	{
		if(!(obj instanceof EntityGroup))
			return false;
		
		EntityGroup other = (EntityGroup)obj;
		
		return other.mId == mId;
	}
	
	@Override
	public int hashCode()
	{
		return mId;
	}
}
