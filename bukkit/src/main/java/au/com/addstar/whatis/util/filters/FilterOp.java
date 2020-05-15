package au.com.addstar.whatis.util.filters;

public enum FilterOp
{
	Equals("="),
	NotEquals("!="),
	Contains(":"),
	NotContains("!:"),
	LessThan("<"),
	LessThanEqual("<="),
	GreaterThan(">"),
	GreaterThanEqual(">=");
	
	private final String mOperator;
	
	FilterOp(String operator)
	{
		mOperator = operator;
	}
	
	@Override
	public String toString()
	{
		return mOperator;
	}
	
	public static FilterOp from(String operator)
	{
		for(FilterOp op : values())
		{
			if(op.mOperator.equals(operator))
				return op;
		}
		
		throw new IllegalArgumentException("No such operator " + operator);
	}
	
	public boolean isValueOk(Object object)
	{
		switch(this)
		{
		case LessThan:
		case GreaterThan:
		case LessThanEqual:
		case GreaterThanEqual:
			return (object instanceof Comparable<?>);
		default:
			return true;
		}
	}
}
