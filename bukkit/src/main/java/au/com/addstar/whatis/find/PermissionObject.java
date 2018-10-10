package au.com.addstar.whatis.find;

import org.bukkit.permissions.Permission;
import org.bukkit.plugin.Plugin;

public class PermissionObject implements IObject
{
	private Permission mPermission;
	
	public PermissionObject(Permission perm)
	{
		mPermission = perm;
	}
	
	@Override
	public String getType()
	{
		return "Permission";
	}

	@Override
	public String getName()
	{
		return mPermission.getName();
	}

	@Override
	public String getDescription()
	{
		return mPermission.getDescription();
	}

	@Override
	public Plugin getOwner()
	{
		return null;
	}
	
	@Override
	public int compareTo( IObject o )
	{
		return getName().compareTo(o.getName());
	}

}
