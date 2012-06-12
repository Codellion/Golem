package com.codellion.utils;

public class StringUtils {
	
	public static String capitalizeFirst(String value)
	{
		String aux = value;
		
		return aux.substring(0, 1).toUpperCase()
			+ aux.substring(1, aux.length());
	}
	
	public static String deCapitalizeFirst(String value)
	{
		String aux = value;
		
		return aux.substring(0, 1).toLowerCase()
			+ aux.substring(1, aux.length());
	}

}
