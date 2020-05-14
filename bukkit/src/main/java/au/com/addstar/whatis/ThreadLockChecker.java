package au.com.addstar.whatis;

import java.lang.management.LockInfo;
import java.lang.management.ManagementFactory;
import java.lang.management.MonitorInfo;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.ListIterator;
import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import au.com.addstar.whatis.util.ThreadUtil;

public class ThreadLockChecker extends Thread
{
	private static volatile long mLastPing;
	private static boolean mIgnore;
	
	private final long mTimeoutTime = 10000;
	
	private final Thread mServerThread;
	private final Logger mLogger = Bukkit.getLogger();
	
	private boolean mStop;
	
	private final ThreadMXBean mManager;
	private StackTraceElement[] mLastTrace;
	private LockInfo mLastLockInfo;
	private int mStuckCount;
	
	private FriendlyNameGenerator mNameGen;
	
	public static void ping()
	{
		mLastPing = System.currentTimeMillis();
		mIgnore = false;
	}
	
	public static void ignore()
	{
		mIgnore = true;
	}
	
	public ThreadLockChecker()
	{
		super("WhatIs ThreadLockChecker");
		mServerThread = Thread.currentThread();
		mStop = false;
		mLastPing = Long.MAX_VALUE;
		
		mManager = ManagementFactory.getThreadMXBean();
	}
	
	@Override
	public void run()
	{
		try
		{
			while(!mStop)
			{
				Thread.sleep(mTimeoutTime);
				threadCheck();
			}
		}
		catch(InterruptedException e)
		{
		}
	}
	
	public void shutdown()
	{
		mStop = true;
		interrupt();
	}
	
	private void threadCheck() throws InterruptedException
	{
		if(mIgnore)
			return;
		
		if(System.currentTimeMillis() - mLastPing < mTimeoutTime)
		{
			mLastTrace = null;
			mStuckCount = 0;
			return;
		}
		
		++mStuckCount;
		
		if(mLastTrace == null)
		{
			mLogger.warning("The main thread failed to respond after 10 seconds");
			mNameGen = new FriendlyNameGenerator();
			
			ThreadInfo[] allThreads = mManager.dumpAllThreads(true, true);
			checkForDeadlock(allThreads);
			
			ThreadInfo serverThread = getServerThread(allThreads);
			
			mLastTrace = serverThread.getStackTrace();
			Plugin[] plugins = ThreadUtil.getPlugins(mLastTrace);
			if(plugins.length == 1)
				mLogger.warning("Possible cause: " + plugins[0].getName());
			else if(plugins.length > 1)
			{
				String[] names = new String[plugins.length];
				for(int i = 0; i < plugins.length; ++i)
					names[i] = plugins[i].getName();
				mLogger.warning("Possible causes: " + StringUtils.join(names, ", "));
			}
			
			printThread(serverThread);
			mLogger.warning("-------------------------------------");
			
			mLastLockInfo = serverThread.getLockInfo();
		}
		else if(mStuckCount == 2)
		{
			mLogger.warning("The server thread is still not responding:");
			
			ThreadInfo[] allThreads = mManager.dumpAllThreads(true, true);
			ThreadInfo serverThread = getServerThread(allThreads);
			
			if(mLastLockInfo != null && mLastLockInfo.equals(serverThread.getLockInfo()))
				mLogger.severe("The main thread is still waiting on lock " + mLastLockInfo.getClassName() + " owned by " + serverThread.getLockOwnerId() + "-" + serverThread.getLockOwnerName());
			
			StackTraceElement[] loopStack = findLoopHead(mServerThread, mLastTrace);
			StackTraceElement loopHead = loopStack[0];
			
			mLogger.severe("Current loop line is: ");
			mLogger.severe("    at " + loopHead.toString());
			
			Plugin[] plugins = ThreadUtil.getPlugins(loopStack);
			if(plugins.length > 0)
				mLogger.severe("This appears to be caused by " + plugins[0].getName());
			
			mLogger.warning("The full stack trace of the main thread is as follows:");
			printStackTrace(loopStack);
		}
		else if(mStuckCount == 3)
		{
			ThreadInfo[] allThreads = mManager.dumpAllThreads(true, true);
			mLogger.warning("The server thread is still not responding after 30 seconds:");
			mLogger.warning("Below is the full stack trace of all threads:");
			for(ThreadInfo thread : allThreads)
				printThread(thread);
		}
	}
	
	private void checkForDeadlock(ThreadInfo[] allThreads)
	{
		if(mManager.isSynchronizerUsageSupported())
		{
			long[] deadlockThreads = mManager.findDeadlockedThreads();
			if(deadlockThreads != null)
			{
				ThreadInfo[] threads = mManager.getThreadInfo(deadlockThreads);
				
				mLogger.severe("The following threads are in deadlock:");
				String[] names = new String[threads.length];
				for(int i = 0; i < threads.length; ++i)
					names[i] = threads[i].getThreadId() + ":" + threads[i].getThreadName();
				
				mLogger.severe(StringUtils.join(names, ", "));
			}
		}
		else
		{
			// TODO: Custom check
		}
	}
	
	private StackTraceElement[] findLoopHead(Thread thread, StackTraceElement[] last) throws InterruptedException
	{
		int count = 0;

		ArrayList<StackTraceElement> loopTrace = new ArrayList<>(Arrays.asList(last));
		int max = loopTrace.size();
		
		// find the highest level in loopTrace that is consistent
		while(count < 100)
		{
			StackTraceElement[] current = thread.getStackTrace();
			max = Math.min(current.length, max);
			
			for(int i = 0; i < max; ++i)
			{
				if(!current[current.length-1-i].equals(loopTrace.get(loopTrace.size()-1-i)))
				{
					max = i;
					break;
				}
			}
			
			++count;
			sleep(5);
		}
		
		// Remove all that have changed
		ListIterator<StackTraceElement> it = loopTrace.listIterator(loopTrace.size()-max);
		while(it.hasPrevious())
		{
			it.previous();
			it.remove();
		}
		
		return loopTrace.toArray(new StackTraceElement[loopTrace.size()]);
	}
	
	private void printStackTrace(StackTraceElement[] stackTrace)
	{
		for(StackTraceElement frame : stackTrace)
		{
			Plugin plugin = ThreadUtil.getPlugin(frame);
			if(plugin != null)
				mLogger.warning(String.format("    at %s [%s]", frame.toString(), plugin.getName()));
			else
				mLogger.warning(String.format("    at %s", frame.toString()));
		}
	}
	
	private void printThread(ThreadInfo thread)
	{
		String owner = ThreadUtil.getThreadOwner(thread);
		
		mLogger.warning("-------------------------------------");
		if(owner != null)
			mLogger.warning(String.format("Name: %s | Id: %d | Suspended: %s | State: %s | Owner: %s", thread.getThreadName(), thread.getThreadId(), thread.isSuspended(), thread.getThreadState().name(), owner));
		else
			mLogger.warning(String.format("Name: %s | Id: %d | Suspended: %s | State: %s", thread.getThreadName(), thread.getThreadId(), thread.isSuspended(), thread.getThreadState().name()));
		mLogger.warning("");
		
		if(thread.getLockedMonitors().length != 0)
		{
			mLogger.warning("Owned monitors:");
			for(MonitorInfo monitor : thread.getLockedMonitors())
				mLogger.warning(String.format(" - %s[%s] at %s", monitor.getClassName(), mNameGen.getNameId(monitor.getIdentityHashCode()), monitor.getLockedStackFrame()));
		}
		
		if(thread.getLockedSynchronizers().length != 0)
		{
			mLogger.warning("Owned locks:");
			for(LockInfo lock : thread.getLockedSynchronizers())
				mLogger.warning(String.format(" - %s[%s]", lock.getClassName(), mNameGen.getNameId(lock.getIdentityHashCode())));
		}
		
		if(thread.getLockInfo() != null)
		{
			if(thread.getLockOwnerId() == -1)
				mLogger.warning(String.format("Waiting for: %s[%s] (unowned)", thread.getLockInfo().getClassName(), mNameGen.getNameId(thread.getLockInfo().getIdentityHashCode())));
			else
				mLogger.warning(String.format("Waiting for: %s[%s] (owned by %d-%s)", thread.getLockInfo().getClassName(), mNameGen.getNameId(thread.getLockInfo().getIdentityHashCode()), thread.getLockOwnerId(), thread.getLockOwnerName()));
		}
		
		mLogger.warning("Stack trace:");
		printStackTrace(thread.getStackTrace());
	}
	
	private ThreadInfo getServerThread(ThreadInfo[] threads)
	{
		for(ThreadInfo thread : threads)
		{
			if(thread.getThreadId() == mServerThread.getId())
				return thread;
		}
		
		throw new IllegalStateException("Server thread is missing for some reason");
	}
}
