package au.com.addstar.whatis.util;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.HashMap;
import java.util.Set;

public class FilterHelper
{
	private static HashMap<Class<?>, ClassConnector> mConnectors = new HashMap<Class<?>, FilterHelper.ClassConnector>();
	
	public static ClassConnector getConnector(Object instance)
	{
		return getConnector(instance.getClass());
	}
	
	public static ClassConnector getConnector(Class<?> clazz)
	{
		ClassConnector connector = mConnectors.get(clazz);
		
		if(connector == null)
		{
			connector = new ClassConnector(clazz);
			mConnectors.put(clazz, connector);
		}
		
		return connector;
	}
	
	public static class ClassConnector
	{
		private Class<?> mClass;
		private HashMap<String, MethodHandle> mHandles;
		
		public ClassConnector(Class<?> clazz)
		{
			mClass = clazz;
			mHandles = new HashMap<String, MethodHandle>();
			for(Method method : clazz.getMethods())
			{
				if(Modifier.isStatic(method.getModifiers()))
					continue;

				if(method.getParameterTypes().length != 0 || method.getReturnType().equals(void.class))
					continue;
				
				String name = method.getName();
				if(name.equals("getHandlers") || name.equals("getClass"))
					continue;
				
				if(name.startsWith("get") && name.length() > 3 && Character.isUpperCase(name.charAt(3)))
					addHandle(name.substring(3), method);
				else if(name.startsWith("is") && name.length() > 2 && Character.isUpperCase(name.charAt(2)))
					addHandle(name, method);
				else if(name.startsWith("has") && name.length() > 3 && Character.isUpperCase(name.charAt(3)))
					addHandle(name, method);
			}
		}
		
		private void addHandle(String name, Method method)
		{
			try
			{
				mHandles.put(name.toLowerCase(), MethodHandles.lookup().unreflect(method));
			}
			catch ( IllegalAccessException e )
			{
				e.printStackTrace();
			}
		}
		
		public Set<String> getNames()
		{
			return Collections.unmodifiableSet(mHandles.keySet());
		}
		
		public Object getValue(Object instance, String name)
		{
			MethodHandle handle = mHandles.get(name);
			if(handle == null)
				throw new IllegalArgumentException("Unknown filter " + name);
			
			try
			{
				return handle.invoke(instance);
			}
			catch (RuntimeException e)
			{
				throw e;
			}
			catch ( Throwable e )
			{
				throw new RuntimeException(e);
			}
		}
		
		public MethodHandle getHandle(String name)
		{
			return mHandles.get(name);
		}
		
		public Class<?> getConnectedClass()
		{
			return mClass;
		}
	}
}
