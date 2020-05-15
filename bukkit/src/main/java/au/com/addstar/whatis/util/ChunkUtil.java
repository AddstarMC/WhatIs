package au.com.addstar.whatis.util;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ChunkUtil
{
	/**
	 * Gets all the things anchoring a chunk. Wont tell you if a plugin is forcing it loaded, yet
	 * @return The list of things anchroing this chunk. It will be empty if the chunk is not loaded. 
	 * If no causes are found, it will contain one entry "unknown".
	 * If it is known that it is loaded by players but the exact players cannot be 
	 * found then "unknown:players" will be returned
	 */
	public static List<String> getChunkAnchors(World world, int x, int z)
	{
		if(!world.isChunkLoaded(x, z))
			return Collections.emptyList();
		
		List<String> list = new ArrayList<>();
		
		if(isChunkIsSpawnRange(world, x, z))
			list.add("spawn");
		
		if(!world.isChunkInUse(x, z))
		{
			// Loaded by spawnregion or plugin
			if(list.isEmpty())
				list.add("unknown"); // Cant yet see if a plugin is keeping it loaded
		}
		else
		{
			// Loaded by player
			List<Player> players = getPlayersUsingChunk(world, x, z);
			
			if(players == null || players.isEmpty())
				list.add("unknown:players");
			else
			{
				for(Player player : players)
					list.add("player:" + player.getName());
			}
		}
		
		return list;
	}
	
	public static boolean isChunkIsSpawnRange(World world, int x, int z)
	{
		if(!world.getKeepSpawnInMemory())
			return false;
		
		Location spawn = world.getSpawnLocation();
		short loadRadius = 128; // This is hardcoded into MC so we're good
		
		int xx = x * 16 + 8 - spawn.getBlockX();
		int zz = z * 16 + 8 - spawn.getBlockZ();
		
		return (xx >= -loadRadius && xx <= loadRadius && zz >= -loadRadius && zz <= loadRadius);
	}
	
	private static boolean mReflectFail = false;
	/**
	 * Gets a list of players that are using the specified chunk
	 * @return The list of players using the chunk or NULL if something goes wrong in the retrieval of the list
	 */
	public static List<Player> getPlayersUsingChunk(World world, int x, int z)
	{
		if(mReflectFail)
			return null;
		
		try
		{
			Object nmsWorld = getWorldHandle(world);
			Object playerChunkMap = getPlayerChunkMap(nmsWorld);
			Object playerChunk = getPlayerChunk(playerChunkMap, x, z);
			List<Object> rawPlayers = getChunkPlayers(playerChunk);
			
			List<Player> players = new ArrayList<>(rawPlayers.size());
			for(Object player : rawPlayers)
				players.add(ChunkUtil.getBukkitEntity(player));

			return players;
		}
		catch(IllegalStateException e)
		{
			mReflectFail = true;
			e.printStackTrace();
			return null;
		}
	}
	
	private static Method mGetHandle;
	private static Object getWorldHandle(World world) throws IllegalStateException
	{
		try
		{
			if(mGetHandle == null)
				mGetHandle = world.getClass().getMethod("getHandle");
			
			return mGetHandle.invoke(world);
		}
		catch(NoSuchMethodException e)
		{
			throw new IllegalStateException("Something is wrong with this server. A core method is missing!", e);
		}
		catch ( Exception e )
		{
			throw new IllegalStateException(e);
		}
	}
	
	private static Method mGetPlayerChunkMap;
	private static Object getPlayerChunkMap(Object nmsWorld) throws IllegalStateException
	{
		try
		{
			if(mGetPlayerChunkMap == null)
				mGetPlayerChunkMap = nmsWorld.getClass().getMethod("getPlayerChunkMap");
			
			return mGetPlayerChunkMap.invoke(nmsWorld);
		}
		catch(NoSuchMethodException e)
		{
			throw new IllegalStateException("Something is wrong with this server. A core method is missing!", e);
		}
		catch ( Exception e )
		{
			throw new IllegalStateException(e);
		}
	}
	
	private static Method mGetPlayerChunk;
	private static Object getPlayerChunk(Object playerChunkMap, int x, int z) throws IllegalStateException
	{
		try
		{
			if(mGetPlayerChunk == null)
				mGetPlayerChunk = getMethodFuzzy(playerChunkMap.getClass(), "a", "PlayerChunk", int.class, int.class, boolean.class);
			
			return mGetPlayerChunk.invoke(playerChunkMap, x, z, false);
		}
		catch(NoSuchMethodException e)
		{
			throw new IllegalStateException("Was unable to find method even with fuzzy matching. A critical change has happened, please update WhatIs.", e);
		}
		catch ( Exception e )
		{
			throw new IllegalStateException(e);
		}
	}
	
	private static Method mGetPlayers;
	@SuppressWarnings( "unchecked" )
	private static List<Object> getChunkPlayers(Object playerChunk) throws IllegalStateException
	{
		try
		{
			if(mGetPlayers == null)
				mGetPlayers = getMethodFuzzy(playerChunk.getClass(), "b", List.class, "PlayerChunk");
			
			return (List<Object>)mGetPlayers.invoke(null, playerChunk);
		}
		catch(NoSuchMethodException e)
		{
			throw new IllegalStateException("Was unable to find method even with fuzzy matching. A critical change has happened, please update WhatIs.", e);
		}
		catch ( Exception e )
		{
			throw new IllegalStateException(e);
		}
	}
	
	private static Method getBukkitEntity;
	@SuppressWarnings( "unchecked" )
	private static <T extends Entity> T getBukkitEntity(Object nmsEntity)
	{
		try
		{
			if(getBukkitEntity == null)
				getBukkitEntity = nmsEntity.getClass().getMethod("getBukkitEntity");
			
			return (T)getBukkitEntity.invoke(nmsEntity);
		}
		catch(NoSuchMethodException e)
		{
			throw new IllegalStateException("Something is wrong with this server. A core method is missing!", e);
		}
		catch ( Exception e )
		{
			throw new IllegalStateException(e);
		}
	}
	
	private static Method getMethodFuzzy(Class<?> clazz, String startName, Object retType, Object... arguments) throws NoSuchMethodException
	{
		Class<?>[] parameterTypes = new Class<?>[arguments.length];
		boolean allClasses = true;
		for(int i = 0; i < arguments.length; ++i)
		{
			if(arguments[i] instanceof Class)
				parameterTypes[i] = (Class<?>)arguments[i];
			else
			{
				allClasses = false;
				break;
			}
		}
		
		if(allClasses)
		{
			try
			{
				Method method = clazz.getDeclaredMethod(startName, parameterTypes);
				Method result = checkMethod(method,retType);
				if(result != null) {
					return result;
				}
			}
			catch(NoSuchMethodException e)
			{
			}
		}
		
		Method[] methods = clazz.getDeclaredMethods();
		
		for(Method method : methods)
		{
			if(allClasses)
			{
				if(Arrays.equals(method.getParameterTypes(), parameterTypes))
				{
					Method result = checkMethod(method,retType);
					if(result != null) {
						return result;
					}
				}
			}
			else
			{
				boolean match = true;
				if(method.getParameterTypes().length != arguments.length)
					continue;
				
				Class<?>[] params = method.getParameterTypes();
				
				for(int i = 0; i < arguments.length; ++i)
				{
					if(arguments[i] instanceof Class<?>)
					{
						if(!params[i].equals(arguments[i]))
						{
							match = false;
							break;
						}
					}
					else
					{
						if(!params[i].getSimpleName().equals(arguments[i]))
						{
							match = false;
							break;
						}
					}
				}
				
				if(match)
				{
					Method result = checkMethod(method,retType);
					if(result != null) {
						return result;
					}
				}
			}
		}
		throw new NoSuchMethodException("Unable to find method " + startName + " in " + clazz.getName());
	}

	private static Method checkMethod(Method method, Object retType) throws SecurityException{
		if(retType instanceof Class<?>)
		{
			if(method.getReturnType().equals(retType))
			{
				method.setAccessible(true);
				return method;
			}
		} else {
			if(method.getReturnType().getSimpleName().equals(retType))
			{
				method.setAccessible(true);
				return method;
			}
		}
		return null;
	}
}
