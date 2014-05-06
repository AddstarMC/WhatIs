package au.com.addstar.whatis.util.filters;

import java.lang.invoke.MethodHandle;

import org.apache.commons.lang.Validate;

public class ExecutionPath
{
	private MethodHandle[] mHandles;
	private Class<?>[] mExpected;
	
	public ExecutionPath(MethodHandle[] handles, Class<?>[] expected)
	{
		Validate.isTrue(handles.length == expected.length);
		mHandles = handles;
		mExpected = expected;
	}
	
	public Object invoke(Object instance)
	{
		try
		{
			for(int i = 0; i < mHandles.length; ++i)
			{
				Validate.isTrue(mExpected[i].isInstance(instance));
				instance = mHandles[i].invoke(instance);
			}
			
			return instance;
		}
		catch(RuntimeException e)
		{
			throw e;
		}
		catch(Throwable e)
		{
			throw new RuntimeException(e);
		}
	}
	
	public Class<?> getReturnType()
	{
		return mHandles[mHandles.length-1].type().returnType();
	}
}
