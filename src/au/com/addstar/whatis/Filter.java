package au.com.addstar.whatis;

import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.plugin.RegisteredListener;

public class Filter
{
	public enum Op
	{
		Contains,
		NotContains
	}
	
	private Op mOp;
	private String mKey;
	private String mValue;
	
	public Filter(String key, String value, Op op)
	{
		mKey = key.toLowerCase();
		mValue = value.toLowerCase();
		mOp = op;
	}
	
	public boolean listenerMatches(RegisteredListener listener)
	{
		boolean has = false;
		if(mKey.equals("@plugin"))
			has = listener.getPlugin().getName().toLowerCase().contains(mValue);
		else if(mKey.equals("@priority"))
			has = listener.getPriority().toString().toLowerCase().equals(mValue);
		else if(mKey.equals("@ignorecancel"))
			has = String.valueOf(listener.isIgnoringCancelled()).equals(mValue);
		else if(mKey.equals("@listener"))
			has = listener.getListener().getClass().getName().toLowerCase().contains(mValue);
		else
			return true;
		
		if(mOp == Op.Contains)
			return has;
		else
			return !has;
	}
	
	@SuppressWarnings( "unchecked" )
	public boolean matches(Map<String, Object> data)
	{
		for(Entry<String, Object> entry : data.entrySet())
		{
			if(entry.getValue() instanceof Map)
			{
				boolean subResult = matches((Map<String, Object>)entry.getValue());
				if(mOp == Op.Contains && subResult)
					return true;
				else if(mOp == Op.NotContains && !subResult)
					return false;
			}
			else if(entry.getKey().toLowerCase().endsWith(mKey))
			{
				if(entry.getValue().toString().toLowerCase().contains(mValue))
					return (mOp == Op.Contains);
			}
		}
		
		return (mOp == Op.NotContains);
	}
	
	@Override
	public String toString()
	{
		return String.format("%s%s%s", mKey, (mOp == Op.Contains ? "=" : "!="), mValue);
	}
}
