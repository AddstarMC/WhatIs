package au.com.addstar.whatis.entities;

import org.bukkit.entity.EntityType;

public enum EntityCategory {
    Item,
    Hanging,
    Mob,
    Animal,
    NPC,
    Player,
    Vehicle,
    Other;

    public static EntityCategory from(EntityType type) {
        switch (type) {
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
			case SALMON:
			case COD:
			case TROPICAL_FISH:
			case PUFFERFISH:
			case LLAMA:
			case POLAR_BEAR:
			case RABBIT:
			case ZOMBIE_HORSE:
			case SKELETON_HORSE:
			case DOLPHIN:
			case DONKEY:
			case PARROT:
			case MULE:
			case TURTLE:
				return Animal;

            case BLAZE:
            case CAVE_SPIDER:
            case CREEPER:
            case ENDERMAN:
            case ENDER_DRAGON:
            case GHAST:
            case GIANT:
            case MAGMA_CUBE:
            case ZOMBIFIED_PIGLIN:
            case SILVERFISH:
            case SKELETON:
            case SLIME:
            case SPIDER:
            case WITCH:
            case WITHER:
            case ZOMBIE:
            case HUSK:
            case STRAY:
			case WITHER_SKELETON:
			case SHULKER:
			case ZOMBIE_VILLAGER:
			case ELDER_GUARDIAN:
			case GUARDIAN:
			case DROWNED:
			case VEX:
			case EVOKER:
			case VINDICATOR:
			case ILLUSIONER:
			case ENDERMITE:
			case PHANTOM:
                return Mob;
            default:
                return Other;
        }
    }
}
