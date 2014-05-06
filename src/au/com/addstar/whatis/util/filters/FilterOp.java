package au.com.addstar.whatis.util.filters;

public enum FilterOp
{
	Equals("="),
	NotEquals("!="),
	Contains(":"),
	NotContains("!:");
	
	private String mOperator;
	
	private FilterOp(String operator)
	{
		mOperator = operator;
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
}
