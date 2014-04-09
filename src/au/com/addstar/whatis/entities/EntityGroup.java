package au.com.addstar.whatis.entities;

import org.apache.commons.lang.Validate;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.util.Vector;

public class EntityGroup
{
	public static double defaultRadius = 8;
	
	private int mCount = 0;
	
	private int[] mTypeCounts;
	private double mRadius = 0;
	private Location mLocation;
	
	public EntityGroup(Location location)
	{
		mLocation = location;
		mRadius = defaultRadius * defaultRadius;
		mTypeCounts = new int[EntityCategory.values().length];
	}
	
	public void addEntity(EntityType type)
	{
		++mCount;
		++mTypeCounts[EntityCategory.from(type).ordinal()];
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
		
		mCount += other.mCount;
		for(EntityCategory cat : EntityCategory.values())
			mTypeCounts[cat.ordinal()] += other.mTypeCounts[cat.ordinal()];
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
	
	@Override
	public String toString()
	{
		return String.format("Group{%d,%d,%d,%s-%d Entities: %d}", mLocation.getBlockX(), mLocation.getBlockY(), mLocation.getBlockZ(), mLocation.getWorld().getName(), (int)getRadius(), mCount);
	}
}
