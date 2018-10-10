package au.com.addstar.whatis.eventhook;

import org.apache.commons.lang.Validate;
import org.bukkit.World;
import org.bukkit.entity.Player;
public class DurationTarget
{
	public int ticks = -1;
	public boolean untilCancel = false;
	public Player onlyPlayer = null;
	public World onlyWorld = null;
	
	private DurationTarget(int ticks, boolean cancel, Player player, World world)
	{
		this.ticks = ticks;
		untilCancel = cancel;
		onlyPlayer = player;
		onlyWorld = world;
	}
	
	public static DurationTarget forTicks(int ticks)
	{
		Validate.isTrue(ticks > 0, "Ticks must be more than 0");
		return new DurationTarget(ticks, false, null, null);
	}
	
	public static DurationTarget forTicksOrCancel(int ticks)
	{
		Validate.isTrue(ticks > 0, "Ticks must be more than 0");
		return new DurationTarget(ticks, true, null, null);
	}
	
	public static DurationTarget playerForTicks(int ticks, Player player)
	{
		Validate.isTrue(ticks > 0, "Ticks must be more than 0");
		return new DurationTarget(ticks, false, player, null);
	}
	
	public static DurationTarget playerForTicksOrCancel(int ticks, Player player)
	{
		Validate.isTrue(ticks > 0, "Ticks must be more than 0");
		return new DurationTarget(ticks, true, player, null);
	}
	
	public static DurationTarget worldForTicks(int ticks, World world)
	{
		Validate.isTrue(ticks > 0, "Ticks must be more than 0");
		return new DurationTarget(ticks, false, null, world);
	}
	
	public static DurationTarget worldForTicksOrCancel(int ticks, World world)
	{
		Validate.isTrue(ticks > 0, "Ticks must be more than 0");
		return new DurationTarget(ticks, true, null, world);
	}
	
	public static DurationTarget playerInWorldForTicks(int ticks, Player player, World world)
	{
		Validate.isTrue(ticks > 0, "Ticks must be more than 0");
		return new DurationTarget(ticks, false, player, world);
	}
	
	public static DurationTarget playerInWorldForTicksOrCancel(int ticks, Player player, World world)
	{
		Validate.isTrue(ticks > 0, "Ticks must be more than 0");
		return new DurationTarget(ticks, true, player, world);
	}
}