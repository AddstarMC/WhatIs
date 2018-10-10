package au.com.addstar.whatis.entities;

public enum SimpleFacing
{
	North(0, -1),
	East(1, 0),
	South(0, 1),
	West(-1, 0);
	
	private int mModX;
	private int mModZ;
	
	private SimpleFacing(int x, int z)
	{
		mModX = x;
		mModZ = z;
	}
	
	public int getModX()
	{
		return mModX;
	}
	
	public int getModZ()
	{
		return mModZ;
	}
}
