package au.com.addstar.whatis.commands;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import au.com.addstar.whatis.util.ReflectionUtil;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

public class PrintFieldCommand implements ICommand
{
	@Override
	public String getName()
	{
		return "printfield";
	}

	@Override
	public String[] getAliases()
	{
		return new String[] {"field", "pf"};
	}

	@Override
	public String getPermission()
	{
		return "whatis.printfield";
	}

	@Override
	public String getUsageString( String label, CommandSender sender )
	{
		return label + " print [-v] <plugin>.<field>[.<field>[...]]";
	}

	@Override
	public String getDescription()
	{
		return "Prints the value of the the specified field as a path from a plugin. Using -v will enable verbose print which will print the value of each field if the target field is a class";
	}

	@Override
	public boolean canBeConsole()
	{
		return true;
	}

	@Override
	public boolean canBeCommandBlock()
	{
		return false;
	}
	
	private Pattern mPathPattern = Pattern.compile("^([a-zA-Z_ 0-9]+)((?:\\.[a-zA-Z_][a-zA-Z_0-9]*(?:\\[[^\\[\\]]+\\])?)*)$");
	private Pattern mPathPatternPartial = Pattern.compile("^([a-zA-Z_ 0-9]+)((?:\\.[a-zA-Z_][a-zA-Z_0-9]*(?:\\[[^\\[\\]]+\\])?)*)(\\.)?$");
	private Pattern mPathSectionPattern = Pattern.compile("(?:\\.([a-zA-Z_][a-zA-Z_0-9]*)(?:\\[([^\\[\\]]+)\\])?)");
	
	private Object resolve(String fullPath) throws IllegalArgumentException, IllegalStateException
	{
		Matcher matcher = mPathPattern.matcher(fullPath);
		
		if (!matcher.matches())
			throw new IllegalArgumentException("Error in path");

		// The plugin first
		Object object = Bukkit.getPluginManager().getPlugin(matcher.group(1));
		if (object == null)
			throw new IllegalArgumentException("No such plugin " + matcher.group(1));
		
		// Used for error reporting
		StringBuilder pathBuilder = new StringBuilder();
		pathBuilder.append(matcher.group(1));
		
		if (matcher.group(2) == null)
			return object;
		
		// Match the path segments
		Matcher pathMatcher = mPathSectionPattern.matcher(matcher.group(2));
		while(pathMatcher.find())
		{
			if (object == null)
				throw new IllegalStateException("Value was null at " + pathBuilder);
			
			pathBuilder.append(pathMatcher.group(0));
			
			try
			{
				Field field = ReflectionUtil.getDeclaredField(pathMatcher.group(1), object.getClass());
				field.setAccessible(true);
				object = field.get(object);
				
				if (pathMatcher.group(2) != null)
				{
					if (object != null)
					{
						if (object.getClass().isArray())
						{
							int index = Integer.parseInt(pathMatcher.group(2));
							if (index < 0 || index > Array.getLength(object))
								throw new IllegalArgumentException("Index " + pathMatcher.group(2) + " is out of bounds of array at " + pathBuilder);
							object = Array.get(object, index);
						}
						else if (object instanceof List<?>)
						{
							int index = Integer.parseInt(pathMatcher.group(2));
							if (index < 0 || index > ((List<?>)object).size())
								throw new IllegalArgumentException("Index " + pathMatcher.group(2) + " is out of bounds of list at " + pathBuilder);
							object = ((List<?>)object).get(index);
						}
						else if (object instanceof Map<?, ?>)
						{
							boolean found = false;
							for (Object key : ((Map<?,?>)object).keySet())
							{
								if (pathMatcher.group(2).equals(String.valueOf(key)))
								{
									object = ((Map<?,?>)object).get(key);
									found = true;
									break;
								}
							}
							
							if (!found)
								throw new IllegalArgumentException("Key " + pathMatcher.group(2) + " was not found in " + pathBuilder);
						}
					}
				}
			}
			catch(NumberFormatException e)
			{
				throw new IllegalArgumentException("Expected numerical index at " + pathBuilder);
			}
			catch( NoSuchFieldException e )
			{
				throw new IllegalArgumentException("No such field " + pathBuilder);
			}
			catch ( Exception e )
			{
				e.printStackTrace();
				throw new IllegalArgumentException("An internal error occured while looking up the field");
			}
		}
		
		return object;
	}
	
	private List<String> resolveOptions(String fullPath)
	{
		if (fullPath.isEmpty())
			return matchPlugin("");
		
		Matcher matcher = mPathPatternPartial.matcher(fullPath);
		
		if (!matcher.matches())
			return null;

		if (matcher.group(2).isEmpty() && matcher.group(3) == null)
			return matchPlugin(matcher.group(1));
		
		// The plugin first
		Object object = Bukkit.getPluginManager().getPlugin(matcher.group(1));
		if (object == null)
			return null;
		
		// Used for error reporting
		StringBuilder pathBuilder = new StringBuilder();
		pathBuilder.append(matcher.group(1));
		
		// Match the path segments
		Matcher pathMatcher = mPathSectionPattern.matcher(matcher.group(2));
		while(pathMatcher.find())
		{
			if (object == null)
				return null;
			
			if (pathMatcher.hitEnd() && matcher.group(3) == null)
			{
				pathBuilder.append('.');
				
				final String path = pathBuilder.toString();
				return Lists.transform(matchField(pathMatcher.group(1), object.getClass()), new Function<String, String>()
				{
					@Override
					public String apply( String fieldName )
					{
						return path + fieldName;
					}
				});
			}
			
			pathBuilder.append(pathMatcher.group(0));
			
			try
			{
				Field field = ReflectionUtil.getDeclaredField(pathMatcher.group(1), object.getClass());
				field.setAccessible(true);
				object = field.get(object);
				
				if (pathMatcher.group(2) != null)
				{
					if (object != null)
					{
						if (object.getClass().isArray())
						{
							int index = Integer.parseInt(pathMatcher.group(2));
							if (index < 0 || index > Array.getLength(object))
								return null;
							object = Array.get(object, index);
						}
						else if (object instanceof List<?>)
						{
							int index = Integer.parseInt(pathMatcher.group(2));
							if (index < 0 || index > ((List<?>)object).size())
								return null;
							object = ((List<?>)object).get(index);
						}
						else if (object instanceof Map<?, ?>)
						{
							boolean found = false;
							for (Object key : ((Map<?,?>)object).keySet())
							{
								if (pathMatcher.group(2).equals(String.valueOf(key)))
								{
									object = ((Map<?,?>)object).get(key);
									found = true;
									break;
								}
							}
							
							if (!found)
								return null;
						}
					}
				}
			}
			catch(NumberFormatException e)
			{
				return null;
			}
			catch( NoSuchFieldException e )
			{
				return null;
			}
			catch ( Exception e )
			{
				return null;
			}
		}
		
		pathBuilder.append('.');
		
		final String path = pathBuilder.toString();
		return Lists.transform(matchField("", object.getClass()), new Function<String, String>()
		{
			@Override
			public String apply( String fieldName )
			{
				return path + fieldName;
			}
		});
	}

	@Override
	public boolean onCommand( CommandSender sender, String label, String[] args )
	{
		int start = 0;
		boolean verbose = false;
		
		if (args.length == 0)
			return false;
		
		if (args[0].equalsIgnoreCase("-v"))
		{
			verbose = true;
			start++;
		}
		
		String fullPath = StringUtils.join(args, ' ', start, args.length);
		
		Object object;
		try
		{
			object = resolve(fullPath);
		}
		catch(IllegalArgumentException e)
		{
			sender.sendMessage(ChatColor.RED + e.getMessage());
			return true;
		}
		catch(IllegalStateException e)
		{
			sender.sendMessage(ChatColor.GOLD + e.getMessage());
			return true;
		}
		
		StringBuilder builder = new StringBuilder();
		
		builder.append(ChatColor.GOLD);
		builder.append(args[start]);
		builder.append(ChatColor.GRAY);
		builder.append(" = ");
		builder.append(ChatColor.WHITE);
		
		if (object == null)
		{
			builder.append(ChatColor.ITALIC);
			builder.append("NULL");
		}
		else if (object.getClass().isArray())
			printArray(object, builder);
		else
		{
			if (verbose)
			{
				if (object.getClass().isPrimitive())
					builder.append(object);
				else
				{
					builder.append(object.getClass().getName());
					builder.append(" [\n");
					
					for (Field field : ReflectionUtil.getAllFields(object.getClass()))
					{
						builder.append("  ");
						if (Modifier.isStatic(field.getModifiers()))
						{
							builder.append(ChatColor.BLUE);
							builder.append('s');
						}
						else
							builder.append(ChatColor.YELLOW);
						
						if (Modifier.isFinal(field.getModifiers()))
							builder.append('f');
						
						if (Modifier.isPublic(field.getModifiers()))
							builder.append('+');
						else if (Modifier.isPrivate(field.getModifiers()))
							builder.append('-');
						else if (Modifier.isProtected(field.getModifiers()))
							builder.append('%');
						else
							builder.append('D');
						
						
						builder.append(' ');
						builder.append(field.getName());
						builder.append(ChatColor.GRAY);
						builder.append(" = ");
						builder.append(ChatColor.WHITE);
						
						Object value = "*ERROR*";
						
						try
						{
							field.setAccessible(true);
							value = field.get(object);
						}
						catch(IllegalAccessException e)
						{
							System.out.println("IllegalAccessException while attempting to read field " + field.getName() + " in " + object.getClass().getName());
						}
						
						if (value == null)
						{
							builder.append(ChatColor.ITALIC);
							builder.append("NULL");
						}
						else if (value.getClass().isArray())
							printArray(value, builder);
						else
						{
							builder.append(value);
							printSuffix(value, builder);
						}
						builder.append("\n");
					}
					builder.append(ChatColor.GRAY);
					builder.append("]");
				}
			}
			else
			{
				builder.append(object);
				printSuffix(object, builder);
			}
		}
		
		sender.sendMessage(builder.toString());
		
		return true;
	}
	
	private void printArray(Object array, StringBuilder builder)
	{
		int size = Array.getLength(array);
		builder.append("Array #");
		builder.append(size);
		
		builder.append(" [");
		for (int i = 0; i < size; ++i)
		{
			if (i != 0)
				builder.append(',');
			builder.append(Array.get(array, i));
		}
		builder.append(']');
	}
	
	private void printSuffix(Object object, StringBuilder builder)
	{
		Class<?> clazz = object.getClass();
		if (clazz == Byte.TYPE || clazz == Byte.class)
			builder.append('b');
		else if (clazz == Short.TYPE || clazz == Short.class)
			builder.append('s');
		else if (clazz == Long.TYPE || clazz == Long.class)
			builder.append('l');
		else if (clazz == Float.TYPE || clazz == Float.class)
			builder.append('f');
		else if (clazz == Double.TYPE || clazz == Double.class)
			builder.append('d');
	}

	@Override
	public List<String> onTabComplete( CommandSender sender, String label, String[] args )
	{
		int start = 0;
		
		if (args[0].equalsIgnoreCase("-v"))
			start++;
		
		String current = StringUtils.join(args, ' ', start, args.length);
		
		return resolveOptions(current);
	}

	private List<String> matchPlugin(String prefix)
	{
		List<String> matches = Lists.newArrayList();
		prefix = prefix.toLowerCase();
		
		for (Plugin plugin : Bukkit.getPluginManager().getPlugins())
		{
			String name = plugin.getName();
			if (name.toLowerCase().startsWith(prefix))
				matches.add(name);
		}
		
		return matches;
	}
	
	private List<String> matchField(String prefix, Class<?> clazz)
	{
		List<String> matches = Lists.newArrayList();
		
		for (Field field : ReflectionUtil.getAllFields(clazz))
		{
			if (field.getName().startsWith(prefix))
				matches.add(field.getName());
		}
		
		return matches;
	}
}
