package au.com.addstar.whatis;

import au.com.addstar.whatis.util.RollingList;

public class TickMonitor implements Runnable
{
	private RollingList<Long> mTickHistory;
	private RollingList<Double> mTPSHistory;
	
	private long mLastTime;
	private long mLastTPSTime;
	
	public TickMonitor(int historySize)
	{
		mTickHistory = new RollingList<Long>(40);
		mTPSHistory = new RollingList<Double>(historySize);
		
		mLastTime = mLastTPSTime = System.nanoTime();
	}
	
	@Override
	public void run()
	{
		long currentTime = System.nanoTime();
		
		long elapsed = currentTime - mLastTime;
		mLastTime = currentTime;
		
		mTickHistory.add(elapsed);
		
		if(currentTime - mLastTPSTime >= 1000000000)
		{
			mLastTPSTime = currentTime;
			mTPSHistory.add(getCurrentTPS());
		}
	}

	public long getAverageTickTime()
	{
		long total = 0;
		for(long time : mTickHistory)
			total += time;
		
		return total / mTickHistory.size();
	}
	
	public long getMaxTickTime()
	{
		long max = 0;
		for(long time : mTickHistory)
		{
			if(time > max)
				max = time;
		}
		
		return max;
	}
	
	public long getMinTickTime()
	{
		long min = Long.MAX_VALUE;
		for(long time : mTickHistory)
		{
			if(time < min)
				min = time;
		}
		
		return min;
	}
	
	public int getMaxTickHistorySize()
	{
		return mTickHistory.capacity();
	}
	
	public int getMaxTPSHistorySize()
	{
		return mTPSHistory.capacity();
	}
	
	public Long[] getTickHistory()
	{
		return mTickHistory.toArray();
	}
	
	public Double[] getTPSHistory()
	{
		return mTPSHistory.toArray();
	}
	
	public double getMaxTPS()
	{
		double max = 0;
		for(double val : mTPSHistory)
		{
			if(val > max)
				max = val;
		}
		
		return max;
	}
	
	public double getMinTPS()
	{
		double min = Long.MAX_VALUE;
		for(double val : mTPSHistory)
		{
			if(val < min)
				min = val;
		}
		
		return min;
	}
	
	public double getCurrentTPS()
	{
		return Math.min(1000 / (getAverageTickTime() / 1000000D), 20D);
	}
}
