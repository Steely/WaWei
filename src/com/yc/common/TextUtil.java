package com.yc.common;

public class TextUtil {
	public static boolean isEmpty(String source){
		if(source==null){
			return true;
		}
		if(source.length()==0){
			return true;
		}
		
		return false;
	}
}
