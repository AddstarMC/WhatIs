package au.com.addstar.whatis;

import java.util.Arrays;

public class TickMonitor implements Runnable
{
	private long[] mHistory;
	private int mHistoryStart;
	private int mHistoryCount;
	
	private double[] mTPSHistory;
	private int mTPSHistoryStart;
	private int mTPSHistoryCount;
	
	private long mLastTime;
	private long mLastTPSTime;
	
	public TickMonitor(int historySize)
	{
		mHistory = new long[40];
		mHistoryStart = 0;
		mHistoryCount = 0;
		
		mTPSHistory = new double[historySize];
		mTPSHistoryStart = 0;
		mTPSHistoryCount = 0;
		
		mLastTime = mLastTPSTime = System.nanoTime();
	}
	
	@Override
	public void run()
	{
		long currentTime = System.nanoTime();
		
		long elapsed = currentTime - mLastTime;
		mLastTime = currentTime;
		
		mHistory[mHistoryStart++] = elapsed;
		
		if(mHistoryCount < mHistory.length)
			++mHistoryCount;
		
		if(mHistoryStart >= mHistory.length)
			mHistoryStart = 0;
		
		if(currentTime - mLastTPSTime >= 1000000)
		{
			mLastTPSTime = currentTime;
			
			mTPSHistory[mTPSHistoryStart++] = getCurrentTPS();
			
			if(mTPSHistoryCount < mTPSHistory.length)
				++mTPSHistoryCount;
			
			if(mTPSHistoryStart >= mTPSHistory.length)
				mTPSHistoryStart = 0;
		}
	}

	public long getAverageTickTime()
	{
		long total = 0;
		for(int i = 0; i < mHistoryCount; ++i)
			total += mHistory[i];
		
		return total / mHistoryCount;
	}
	
	public long getMaxTickTime()
	{
		long max = 0;
		for(int i = 0; i < mHistoryCount; ++i)
		{
			if(mHistory[i] > max)
				max = mHistory[i];
		}
		
		return max;
	}
	
	public long getMinTickTime()
	{
		long min = Long.MAX_VALUE;
		for(int i = 0; i < mHistoryCount; ++i)
		{
			if(mHistory[i] < min)
				min = mHistory[i];
		}
		
		return min;
	}
	
	public int getMaxHistorySize()
	{
		return mHistory.length;
	}
	
	public int getMaxTPSHistorySize()
	{
		return mTPSHistory.length;
	}
	
	public long[] getHistory()
	{
		if(mHistoryCount < mHistory.length)
			return Arrays.copyOfRange(mHistory, 0, mHistoryCount);
		else if(mHistoryStart == 0)
			return mHistory;
		else
		{
			long[] history = new long[mHistory.length];
			int index = 0;
			for(int i = mHistoryStart; i < mHistory.length; ++i)
				history[index++] = mHistory[i];
			for(int i = 0; i < mHistoryStart; ++i)
				history[index++] = mHistory[i];
			return history;
		}
	}
	
	public double[] getTPSHistory()
	{
		if(mTPSHistoryCount < mTPSHistory.length)
			return Arrays.copyOfRange(mTPSHistory, 0, mTPSHistoryCount);
		else if(mTPSHistoryStart == 0)
			return mTPSHistory;
		else
		{
			double[] history = new double[mTPSHistory.length];
			int index = 0;
			for(int i = mTPSHistoryStart; i < mTPSHistory.length; ++i)
				history[index++] = mTPSHistory[i];
			for(int i = 0; i < mTPSHistoryStart; ++i)
				history[index++] = mTPSHistory[i];
			return history;
		}
	}
	
	public double getMaxTPS()
	{
		double max = 0;
		for(int i = 0; i < mTPSHistoryCount; ++i)
		{
			if(mTPSHistory[i] > max)
				max = mTPSHistory[i];
		}
		
		return max;
	}
	
	public double getMinTPS()
	{
		double min = Long.MAX_VALUE;
		for(int i = 0; i < mTPSHistoryCount; ++i)
		{
			if(mTPSHistory[i] < min)
				min = mTPSHistory[i];
		}
		
		return min;
	}
	
	public double getCurrentTPS()
	{
		return Math.min(1000 / (getAverageTickTime() / 1000000D), 20D);
	}
}
