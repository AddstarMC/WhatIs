package au.com.addstar.whatis.util.filters;

import java.util.Collection;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

public class Stringifier
{
	public static String asString( Object obj )
	{
		if(obj instanceof Player)
			return ((Player)obj).getName();
		else if(obj instanceof Block)
			return String.format("%d %d %d", ((Block) obj).getX(), ((Block) obj).getY(), ((Block) obj).getZ());
		else if(obj instanceof Location)
			return String.format("%.1f %.1f %.1f", ((Location) obj).getX(), ((Location) obj).getY(), ((Location) obj).getZ());
		else if(obj instanceof World)
			return ((World) obj).getName();
		else if(obj instanceof LivingEntity)
		{
			if(((LivingEntity) obj).getCustomName() != null)
				return ((LivingEntity) obj).getCustomName();
			return ((LivingEntity) obj).getType().toString();
		}
		else if(obj instanceof Entity)
			return ((LivingEntity) obj).getType().toString();
		else if(obj instanceof ItemStack)
			return ((ItemStack) obj).getType().name();
		else if(obj instanceof Collection)
		{
			StringBuilder out = new StringBuilder();
			
			boolean first = true;
			for(Object o2 : (Collection<?>)obj)
			{
				if(!first)
					out.append(",");
				first = false;
				out.append(asString(o2));
			}
			
			return out.toString();
		}
		else if(obj instanceof Listener)
			return obj.getClass().getName();
		else if(obj != null)
			return obj.toString();
		
		return null;
	}
}
