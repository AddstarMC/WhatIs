package au.com.addstar.whatis.eventhook;

import org.bukkit.event.Event;
import org.bukkit.event.EventException;
import org.bukkit.plugin.RegisteredListener;
import org.jetbrains.annotations.NotNull;

public class DelegatingRegisteredListener extends RegisteredListener
{
	private final RegisteredListener mExisting;
	
	public DelegatingRegisteredListener(RegisteredListener existing)
	{
		super(existing.getListener(), null, existing.getPriority(), existing.getPlugin(), existing.isIgnoringCancelled());
		mExisting = existing;
	}

	@Override
	public void callEvent(@NotNull Event event ) throws EventException
	{
		mExisting.callEvent(event);
	}
	
	public RegisteredListener getOriginal()
	{
		return mExisting;
	}
}
