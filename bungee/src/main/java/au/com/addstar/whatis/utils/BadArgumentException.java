package au.com.addstar.whatis.utils;

import com.google.common.collect.Lists;

import java.util.List;

public class BadArgumentException extends IllegalArgumentException {
	private static final long serialVersionUID = -6202307868577695232L;
	
	private final int index;
	private final List<String> info;
	
	public BadArgumentException(int index, String message) {
		super(message);
		this.index = index;
		info = Lists.newArrayList();
	}
	public void addInfo(String info){
		this.info.add(info);
	}
	public int getIndex() {
		return index;
	}
	
	public List<String> getInfo() {
		return info;
	}
}
