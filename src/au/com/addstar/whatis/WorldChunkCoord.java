package au.com.addstar.whatis;

import java.util.UUID;

public class WorldChunkCoord
{
	public final int x;
	public final int z;
	public final UUID world;
	
	public WorldChunkCoord(int x, int z, UUID world)
	{
		this.x = x;
		this.z = z;
		this.world = world;
	}
	
	@Override
	public boolean equals( Object obj )
	{
		if(!(obj instanceof WorldChunkCoord))
			return false;
		
		return x == ((WorldChunkCoord)obj).x && z == ((WorldChunkCoord)obj).z && world.equals(((WorldChunkCoord)obj).world);
	}
	
	@Override
	public int hashCode()
	{
		return x | z << 16 ^ world.hashCode();
	}
	
	@Override
	public String toString()
	{
		return String.format("%s %d,%d", world, x, z);
	}
}
