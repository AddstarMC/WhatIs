package au.com.addstar.whatis.util;

import java.util.TreeMap;
import java.util.Map.Entry;

import org.apache.commons.lang.Validate;
import org.bukkit.ChatColor;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Graph
{
	private static final char[] mCharsConsole = new char[] {'_','Ü','Û'};
	private static final char[] mCharsInGame = new char[] {'\u2581', '\u2582', '\u2583', '\u2584', '\u2585', '\u2586', '\u2587', '\u2588'};
	
	private float[] mData;
	
	private double mMin;
	private double mMax;
	private TreeMap<Double, ChatColor> mColourStops;
	private boolean mShowLowest = false;
	
	public Graph()
	{
		mColourStops = new TreeMap<Double, ChatColor>();
	}
	
	public void addColourStop(double value, ChatColor colour)
	{
		Validate.notNull(colour);
		
		mColourStops.put(value, colour);
	}
	
	public void clearStops()
	{
		mColourStops.clear();
	}
	
	public void setData(Long[] data, long min, long max)
	{
		mData = new float[data.length];
		for(int i = 0; i < data.length; ++i)
			mData[i] = (float)Math.min(1, Math.max(0, (data[i] - min) / (double)(max - min)));
		
		mMin = min;
		mMax = max;
	}
	
	public void setData(Double[] data, double min, double max)
	{
		mData = new float[data.length];
		for(int i = 0; i < data.length; ++i)
			mData[i] = (float)Math.min(1, Math.max(0, (data[i] - min) / (max - min)));
		
		mMin = min;
		mMax = max;
	}
	
	public void setShowLowest(boolean lowest)
	{
		mShowLowest = lowest;
	}
	
	public void draw(CommandSender target)
	{
		Validate.notNull(mData);
		
		StringBuilder graph = new StringBuilder();

		int points = (target instanceof Player || target instanceof BlockCommandSender) ? 20 : 40;
		int stepSize = Math.max(mData.length / points, 1);
		
		for(int i = 0; i < mData.length; i += stepSize)
		{
			// Average those few data points
//			float total = 0;
//			int count = 0;
			float stepMin = Float.MAX_VALUE;
			float stepMax = 0;
			for(int j = i; j < i + stepSize && j < mData.length; ++j)
			{
				//total += mData[j];
				if(mData[j] > stepMax)
					stepMax = mData[j];
				if(mData[j] < stepMin)
					stepMin = mData[j];
				
				//++count;
			}
			
			float value;
			if(mShowLowest)
				value = stepMin;
			else
				value = stepMax;
			
//			if (average < avg)
//				average = stepMin;
//			else
//				average = stepMax;
			
			for(Entry<Double, ChatColor> stop : mColourStops.descendingMap().entrySet())
			{
				double val = (stop.getKey() - mMin) / (mMax - mMin);
				if(value >= val)
				{
					graph.append(stop.getValue());
					break;
				}
			}
			
			if(target instanceof Player || target instanceof BlockCommandSender)
				graph.append(mCharsInGame[(int)(value * (mCharsInGame.length - 1))]);
			else
				graph.append(mCharsConsole[(int)(value * (mCharsConsole.length - 1))]);
		}
		
		target.sendMessage(graph.toString());
	}
}
