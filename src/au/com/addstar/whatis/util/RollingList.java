package au.com.addstar.whatis.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

public class RollingList<T> implements Iterable<T>
{
	private T[] mData;
	private int mStart;
	private int mCount;
	
	@SuppressWarnings( "unchecked" )
	public RollingList(int size)
	{
		mData = (T[])new Object[size];
		mStart = 0;
		mCount = 0;
	}
	
	public int size()
	{
		return mCount;
	}
	
	public int capacity()
	{
		return mData.length;
	}

	public boolean isEmpty()
	{
		return mCount == 0;
	}

	@Override
	public Iterator<T> iterator()
	{
		return new Iterator<T>()
		{
			private int mIndex = 0;
			
			@Override
			public boolean hasNext()
			{
				return mIndex < size();
			}

			@Override
			public T next()
			{
				++mIndex;
				return get(mIndex);
			}

			@Override
			public void remove()
			{
				throw new UnsupportedOperationException();
			}
		};
	}

	public boolean add( T value )
	{
		mData[mStart++] = value;
		
		if(mCount < mData.length)
			++mCount;
		
		if(mStart >= mData.length)
			mStart = 0;
		
		return true;
	}

	public boolean addAll( Collection<? extends T> c )
	{
		for(T value : c)
			add(value);
		
		return true;
	}
	
	@SuppressWarnings( "unchecked" )
	public T[] toArray()
	{
		if(mCount < mData.length)
			return Arrays.copyOfRange(mData, 0, mCount);
		else if(mStart == 0)
			return mData.clone();
		else
		{
			T[] copy = (T[])new Object[mData.length];
			int index = 0;
			for(int i = mStart; i < mData.length; ++i)
				copy[index++] = mData[i];
			for(int i = 0; i < mStart; ++i)
				copy[index++] = mData[i];
			return copy;
		}
	}

	public void clear()
	{
		mCount = 0;
		mStart = 0;
	}

	public T get( int index )
	{
		if(index < 0 || index > mCount)
			throw new IndexOutOfBoundsException();
		
		if(index >= mCount - mStart)
			return mData[index - (mCount - mStart)];
		return mData[index];
	}
	
	
}
