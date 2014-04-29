package au.com.addstar.whatis.eventhook;

import java.util.ArrayList;

import org.bukkit.event.Event;
import org.bukkit.plugin.RegisteredListener;

import au.com.addstar.whatis.EventReporter;

public abstract class EventHookSession
{
	private HookRunner<? extends EventHookSession> mRunner;
	private ArrayList<Class<? extends Event>> events;
	
	public EventHookSession()
	{
		events = new ArrayList<Class<? extends Event>>();
	}
	
	public final void setRunner(HookRunner<? extends EventHookSession> runner)
	{
		mRunner = runner;
	}
	
	public synchronized void recordInitialStep(Event event)
	{
	}
	
	public synchronized void recordStep(Event event, RegisteredListener listener, boolean initallyCancelled)
	{
		if(mRunner != null)
			mRunner.checkConditions(event, initallyCancelled);
	}
	
	public void onStop()
	{
	}
	
	protected final boolean shouldInclude(Event event)
	{
		if(mRunner != null)
			return mRunner.shouldInclude(event);
		return true;
	}
	
	public final void hook(Class<? extends Event> eventClass)
	{
		events.add(eventClass);
		EventReporter.hookEvent(eventClass, this);
	}
	
	public final void unhook()
	{
		for(Class<? extends Event> event : events)
			EventReporter.restoreEvent(event);
	}
}
