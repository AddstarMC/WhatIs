package au.com.addstar.whatis.util.filters;

import java.lang.invoke.MethodHandle;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredListener;
import org.bukkit.util.Vector;

import au.com.addstar.whatis.util.FilterHelper;
import au.com.addstar.whatis.util.FilterHelper.ClassConnector;

public abstract class FilterCompiler
{
	private static Pattern mFilterDefPattern = Pattern.compile("\\[(?:@?[a-zA-Z0-9_]+(?:\\.[a-zA-Z0-9_]+)*(?:=|!=|\\:|!\\:|<|>|<=|>=)@?[a-zA-Z0-9_ \\-\\.]+)(?:,[a-zA-Z0-9_]+(?:\\.[a-zA-Z0-9_]+)*(?:=|!=|\\:|!\\:|<|>|<=|>=)[a-zA-Z0-9_ \\-\\.]+)*\\]");
	private static Pattern mFilterPattern = Pattern.compile("(@?[a-zA-Z0-9_]+(?:\\.[a-zA-Z0-9_]+)*)(=|!=|\\:|!\\:|<|>|<=|>=)([a-zA-Z0-9_ \\-\\.]+)");
	
	public static FilterSet compile(Class<?> clazz, String filterString)
	{
		Matcher match = mFilterDefPattern.matcher(filterString);
		if(!match.matches())
			throw new IllegalArgumentException("Invalid filter specified: '" + filterString + "'");
		
		ClassConnector connector = FilterHelper.getConnector(clazz);
		ClassConnector handlerConnector = FilterHelper.getConnector(RegisteredListener.class);
		ArrayList<CompiledFilter> filters = new ArrayList<>();
		ArrayList<CompiledFilter> handlerFilters = new ArrayList<>();
		
		match = mFilterPattern.matcher(filterString);
		while(match.find())
		{
			String name = match.group(1);
			boolean handler = name.startsWith("@");
			if(handler)
				name = name.substring(1);
			
			FilterOp op = FilterOp.from(match.group(2));
			
			ExecutionPath path = null;
			
			if(handler)
			{
				path = getExecutionPath(handlerConnector, name, new ArrayList<MethodHandle>(), new ArrayList<Class<?>>());
				if(path == null)
					throw new IllegalArgumentException(name + " cannot be found in " + RegisteredListener.class.getName());
			}
			else
			{
				path = getExecutionPath(connector, name, new ArrayList<MethodHandle>(), new ArrayList<Class<?>>());
				if(path == null)
					throw new IllegalArgumentException(name + " cannot be found in " + clazz.getName());
			}
			
			Object value;
			if(op == FilterOp.Contains || op == FilterOp.NotContains)
				value = match.group(3).toLowerCase();
			else
				value = compileToMatch(match.group(3), path.getReturnType());
			
			if(!op.isValueOk(value))
				throw new IllegalArgumentException(name + ": The value " + match.group(3) + " is not valid with the operator " + op.toString());
			
			if(handler)
				handlerFilters.add(new CompiledFilter(path, op, value));
			else
				filters.add(new CompiledFilter(path, op, value));
		}
		
		return new FilterSet(filters, handlerFilters);
	}
	
	private static ExecutionPath getExecutionPath(ClassConnector connector, String path, ArrayList<MethodHandle> handles, ArrayList<Class<?>> classes)
	{
		if(!path.contains("."))
		{
			MethodHandle handle = connector.getHandle(path);
			if(handle == null)
				return null;
			
			handles.add(handle);
			classes.add(connector.getConnectedClass());
			
			return new ExecutionPath(handles.toArray(new MethodHandle[0]), classes.toArray(new Class<?>[0]));
		}
		else
		{
			String next = path.split("\\.")[0];
			MethodHandle handle = connector.getHandle(next);
			if(handle == null)
				return null;
			
			handles.add(handle);
			classes.add(connector.getConnectedClass());
			
			return getExecutionPath(FilterHelper.getConnector(handle.type().returnType()), path.substring(path.indexOf('.')+1), handles, classes);
		}
	}
	
	@SuppressWarnings( "unchecked" )
	private static Object compileToMatch(String string, Class<?> clazz)
	{
		Object value;
		if(Player.class.isAssignableFrom(clazz))
		{
			value = Bukkit.getPlayerExact(string);
			if(value == null)
				throw new IllegalArgumentException("Could not find player '" + string + "'");
		}
		else if(World.class.isAssignableFrom(clazz))
		{
			value = Bukkit.getWorld(string);
			if(value == null)
				throw new IllegalArgumentException("Could not find world '" + string + "'");
		}
		else if(Plugin.class.isAssignableFrom(clazz))
		{
			value = Bukkit.getPluginManager().getPlugin(string);
			if(value == null)
				throw new IllegalArgumentException("Could not find plugin '" + string + "'");
		}
		else if(Number.class.isAssignableFrom(clazz))
		{
			try
			{
				Double val = Double.parseDouble(string);
				
				if(clazz.equals(Byte.class))
					value = val.byteValue();
				else if(clazz.equals(Short.class))
					value = val.shortValue();
				else if(clazz.equals(Integer.class))
					value = val.intValue();
				else if(clazz.equals(Long.class))
					value = val.longValue();
				else if(clazz.equals(Float.class))
					value = val.floatValue();
				else if(clazz.equals(Double.class))
					value = val;
				else
					throw new IllegalArgumentException("Cannot compile '" + string + "'. Value expected is of type " + clazz.getSimpleName() + " but no known method is available");
			}
			catch(NumberFormatException e)
			{
				throw new IllegalArgumentException(string + " is not a number and needs to be");
			}
		}
		else if(Boolean.class.isAssignableFrom(clazz))
		{
			if(string.equalsIgnoreCase("true"))
				value = true;
			else if(string.equalsIgnoreCase("false"))
				value = false;
			else
				throw new IllegalArgumentException("Expected true or false, got '" + string + "'");
		}
		else if(String.class.isAssignableFrom(clazz))
			value = string;
		else if(Block.class.isAssignableFrom(clazz))
		{
			Location loc = toLocation(string);
			value = loc.getBlock();
		}
		else if(Location.class.isAssignableFrom(clazz))
			value = toLocation(string);
		else if(Vector.class.isAssignableFrom(clazz))
			value = toVector(string);
		else if(Enum.class.isAssignableFrom(clazz))
		{
			value = null;
			@SuppressWarnings( "rawtypes" )
			Class<? extends Enum> enumClass = clazz.asSubclass(Enum.class);
			for(Object v : EnumSet.allOf(enumClass))
			{
				String name = ((Enum<?>)v).name();
				if(name.equalsIgnoreCase(string))
				{
					value = v;
					break;
				}
			}
			
			if(value == null)
				throw new IllegalArgumentException("Cannot find value '" + string + "' in enum " + clazz.getName());
		}
		else
			throw new IllegalArgumentException("Cannot compile '" + string + "'. Value expected is of type " + clazz.getSimpleName() + " but no known method is available");
		
		return value;
	}
	
	private static Location toLocation(String string)
	{
		String[] parts = string.split(" ");
		if(parts.length != 4)
			throw new IllegalArgumentException("Location should be specified as 'x y z world'. Got '" + string + "'");
		
		World world = Bukkit.getWorld(parts[3]);
		if(world == null)
			throw new IllegalArgumentException("Location should be specified as 'x y z world'. Invalid world. Got '" + string + "'");
		
		double x, y, z;
		
		try
		{
			x = Double.parseDouble(parts[0]);
			y = Double.parseDouble(parts[1]);
			z = Double.parseDouble(parts[2]);
		}
		catch(NumberFormatException e)
		{
			throw new IllegalArgumentException("Location should be specified as 'x y z world'. Invalid coordinate. Got '" + string + "'");
		}
		
		return new Location(world, x, y, z);
	}
	
	private static Vector toVector(String string)
	{
		String[] parts = string.split(" ");
		if(parts.length != 3)
			throw new IllegalArgumentException("Vector should be specified as 'x y z'. Got '" + string + "'");
		
		double x, y, z;
		
		try
		{
			x = Double.parseDouble(parts[0]);
			y = Double.parseDouble(parts[1]);
			z = Double.parseDouble(parts[2]);
		}
		catch(NumberFormatException e)
		{
			throw new IllegalArgumentException("Vector should be specified as 'x y z'. Invalid coordinate. Got '" + string + "'");
		}
		
		return new Vector(x, y, z);
	}
}
