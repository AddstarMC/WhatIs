package au.com.addstar.whatis.util;

import java.lang.reflect.Field;
import java.util.Arrays;

import com.google.common.collect.Iterables;

public class ReflectionUtil
{
	public static Field getDeclaredField(String name, Class<?> clazz) throws NoSuchFieldException
	{
		try
		{
			return clazz.getDeclaredField(name);
		}
		catch (NoSuchFieldException e)
		{
			clazz = clazz.getSuperclass();
			while(!clazz.equals(Object.class))
			{
				try
				{
					return clazz.getDeclaredField(name);
				}
				catch(NoSuchFieldException ex)
				{
					clazz = clazz.getSuperclass();
				}
			}
		}
		
		throw new NoSuchFieldException("Unknown field " + name);
	}
	
	public static Iterable<Field> getAllFields(Class<?> clazz)
	{
		Iterable<Field> classFields = Arrays.asList(clazz.getDeclaredFields());
		
		clazz = clazz.getSuperclass();
		while(!clazz.equals(Object.class))
		{
			classFields = Iterables.concat(classFields, Arrays.asList(clazz.getDeclaredFields()));
			clazz = clazz.getSuperclass();
		}
		
		return classFields;
	}
}
