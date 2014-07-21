package au.com.addstar.whatis.find;

import org.bukkit.command.Command;
import org.bukkit.command.PluginIdentifiableCommand;
import org.bukkit.plugin.Plugin;

public class CommandObject implements IObject
{
	private Command mCommand;
	private String mName;
	
	public CommandObject(String name, Command command)
	{
		mCommand = command;
		mName = name;
	}
	
	@Override
	public String getType()
	{
		return "Command";
	}

	@Override
	public String getName()
	{
		return mName;
	}

	@Override
	public String getDescription()
	{
		return mCommand.getName();
	}

	@Override
	public Plugin getOwner()
	{
		if(mCommand instanceof PluginIdentifiableCommand)
			return ((PluginIdentifiableCommand)mCommand).getPlugin();
		return null;
	}

	@Override
	public int compareTo( IObject o )
	{
		return getName().compareTo(o.getName());
	}
}
