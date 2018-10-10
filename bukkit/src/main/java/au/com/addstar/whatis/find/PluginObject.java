package au.com.addstar.whatis.find;

import org.bukkit.plugin.Plugin;

public class PluginObject implements IObject
{
	private Plugin mPlugin;
	
	public PluginObject(Plugin plugin)
	{
		mPlugin = plugin;
	}
	
	@Override
	public int compareTo( IObject o )
	{
		return getName().compareTo(o.getName());
	}

	@Override
	public String getType()
	{
		return "Plugin";
	}

	@Override
	public String getName()
	{
		return mPlugin.getName();
	}

	@Override
	public String getDescription()
	{
		return mPlugin.getDescription().getDescription();
	}

	@Override
	public Plugin getOwner()
	{
		return mPlugin;
	}

}
