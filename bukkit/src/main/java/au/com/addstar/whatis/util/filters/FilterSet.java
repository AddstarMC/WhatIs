package au.com.addstar.whatis.util.filters;

import java.util.List;

import org.bukkit.plugin.RegisteredListener;

public class FilterSet
{
	private final List<CompiledFilter> mFilters;
	private final List<CompiledFilter> mHandlerFilters;
	
	public FilterSet(List<CompiledFilter> filters, List<CompiledFilter> handlerFilters)
	{
		mFilters = filters;
		mHandlerFilters = handlerFilters;
	}
	
	public boolean matches(Object instance)
	{
		for(CompiledFilter filter : mFilters)
		{
			if(!filter.matches(instance))
				return false;
		}
		
		return true;
	}
	
	public boolean matchesHandler(RegisteredListener handler)
	{
		for(CompiledFilter filter : mHandlerFilters)
		{
			if(!filter.matches(handler))
				return false;
		}
		
		return true;
	}
}
