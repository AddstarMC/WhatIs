package au.com.addstar.whatis.entities;

import org.bukkit.entity.EntityType;

public enum EntityCategory
{
	Item,
	Hanging,
	Mob,
	Animal,
	NPC,
	Player,
	Vehicle,
	Other;
	
	public static EntityCategory from(EntityType type)
	{
		switch(type)
		{
		case DROPPED_ITEM:
			return Item;
		
		case BOAT:
		case MINECART:
		case MINECART_CHEST:
		case MINECART_COMMAND:
		case MINECART_FURNACE:
		case MINECART_HOPPER:
		case MINECART_MOB_SPAWNER:
		case MINECART_TNT:
			return Vehicle;
			
		case ITEM_FRAME:
		case PAINTING:
			return Hanging;
		
		case PLAYER:
			return Player;
		
		case VILLAGER:
			return NPC;
		
		case BAT:
		case CHICKEN:
		case COW:
		case HORSE:
		case IRON_GOLEM:
		case MUSHROOM_COW:
		case OCELOT:
		case PIG:
		case SHEEP:
		case SNOWMAN:
		case SQUID:
		case WOLF:
			return Animal;
		
		case BLAZE:
		case CAVE_SPIDER:
		case CREEPER:
		case ENDERMAN:
		case ENDER_DRAGON:
		case GHAST:
		case GIANT:
		case MAGMA_CUBE:
		case PIG_ZOMBIE:
		case SILVERFISH:
		case SKELETON:
		case SLIME:
		case SPIDER:
		case WITCH:
		case WITHER:
		case ZOMBIE:
			return Mob;
		
		default:
			return Other;
		}
	}
}
