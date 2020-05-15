package au.com.addstar.whatis;

import org.bukkit.event.Event;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.PlayerLeashEntityEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerBedLeaveEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerChatTabCompleteEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerEditBookEvent;
import org.bukkit.event.player.PlayerEggThrowEvent;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemBreakEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerLevelChangeEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerShearEntityEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.event.player.PlayerToggleSprintEvent;
import org.bukkit.event.player.PlayerVelocityEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;

public enum EventGroups
{
	Block(BlockPlaceEvent.class, BlockBreakEvent.class, PlayerInteractEvent.class, BlockIgniteEvent.class, PlayerBucketEmptyEvent.class, PlayerBucketFillEvent.class, PlayerBedEnterEvent.class, PlayerBedLeaveEvent.class),
	Item(PlayerInteractEvent.class, PlayerDropItemEvent.class, PlayerItemConsumeEvent.class, PlayerEditBookEvent.class, PlayerItemBreakEvent.class, PlayerItemHeldEvent.class, PlayerFishEvent.class, PlayerEggThrowEvent.class, EntityShootBowEvent.class),
	Move(PlayerMoveEvent.class, PlayerTeleportEvent.class, PlayerPortalEvent.class, PlayerRespawnEvent.class, PlayerVelocityEvent.class),
	Chat(AsyncPlayerChatEvent.class, PlayerCommandPreprocessEvent.class),
	Entity(PlayerInteractEntityEvent.class, PlayerShearEntityEvent.class, EntityDeathEvent.class, EntityDamageEvent.class, EntityDamageByEntityEvent.class, PlayerFishEvent.class, VehicleEnterEvent.class, VehicleExitEvent.class, EntityShootBowEvent.class, PlayerLeashEntityEvent.class, EntityPickupItemEvent.class),
	Player(PlayerExpChangeEvent.class, PlayerGameModeChangeEvent.class, PlayerKickEvent.class, PlayerQuitEvent.class, PlayerToggleFlightEvent.class, PlayerToggleSneakEvent.class, PlayerToggleSprintEvent.class, PlayerDeathEvent.class, PlayerLevelChangeEvent.class, FoodLevelChangeEvent.class);

	public final Class<? extends Event>[] events;
	
	EventGroups(Class<? extends Event>... events)
	{
		this.events = events;
	}
}
