package au.com.addstar.whatis;

import java.util.HashMap;

public class FriendlyNameGenerator
{
	private static final String[] colors = new String[] {"Red", "Blue", "Black", "White", "Green", "Yellow", "Gold", "Purple", "Magenta", "Cyan", "Silver", "Pink", "Brown", "Gray", "Lime", "Orange"};
	private static final String[] alphabet = new String[] {"Alpha", "Bravo", "Charlie", "Delta", "Echo", "Foxtrot", "Golf", "Hotel", "India", "Juliett", "Kilo", "Lima", "Mike", "November", "Oscar", "Papa", "Quebec", "Romeo", "Sierra", "Tango", "Uniform", "Victor", "Whiskey", "Xray", "Yankee", "Zulu"};
	
	private final HashMap<Integer, String> mNames;
	private final int[] mUsageCount;
	
	public FriendlyNameGenerator()
	{
		mNames = new HashMap<>();
		mUsageCount = new int[colors.length];
	}
	
	private int getColor(int id)
	{
		return id % colors.length;
	}
	
	public String getNameId(int objectId)
	{
		String name = mNames.get(objectId);
		if(name == null)
		{
			int color = getColor(objectId);
			int count = mUsageCount[color];
			
			if(count >= alphabet.length)
				name = String.format("%s %d", colors[color], count);
			else
				name = String.format("%s %s", colors[color], alphabet[count]);
			
			mUsageCount[color] = count+1;
			mNames.put(objectId, name);
		}
		
		return name;
	}
	
	public String getName(Object object)
	{
		return getNameId(System.identityHashCode(object));
	}
}
