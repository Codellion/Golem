package com.codellion.utils.reflexion;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.codellion.golem.mutation.IMutable;
import com.codellion.utils.StringUtils;

public class ReflexUtils {

	public enum Visibility {
		PUBLIC,
		PROTECTED,
		PRIVATE
	}
	
	public static <T> T setValueInObject(T object, String prop, Object value, Visibility visibility)
	{
		try {
			return setValueInObjectError(object, prop, value, visibility);
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	private static <T> T setValueInObjectError(T object, String prop, Object value, Visibility visibility) throws NoSuchFieldException
	{
		Field fieldProp = null;
		Method method = null;
		
		try {
			if(visibility == Visibility.PRIVATE)			
			{
				try
				{
					fieldProp = object.getClass().getDeclaredField(prop);	
				}
				catch(NoSuchFieldException ex)
				{
					fieldProp = object.getClass().getField(prop);	
				}
			}
			else
				method = object.getClass().getDeclaredMethod("set" +  StringUtils.capitalizeFirst(prop), value.getClass());
		
			if(object.getClass().equals(String.class))
			{
				char[] newString = ((String) value).toCharArray();
				
				if(visibility == Visibility.PRIVATE && fieldProp != null)
				{
					Field fieldCount = object.getClass().getDeclaredField("count");
					
					fieldProp.setAccessible(true);	
					fieldCount.setAccessible(true);
					
					fieldCount.set(object, newString.length);					
					fieldProp.set(object, Arrays.copyOf(newString, newString.length));
				}
			}
			else
			{		
				Object auxValue = null;

				if(visibility == Visibility.PRIVATE && fieldProp != null)
				{
					List<Class<?>> interfaces = Arrays.asList(value.getClass().getInterfaces());
					
					if(!value.getClass().equals(fieldProp.getType()) 
							&& interfaces.contains(IMutable.class))
					{												
						IMutable mut = (IMutable) value;
						auxValue = mut.mutateTo(fieldProp.getType());
					}
					else
						auxValue = value;
					
					fieldProp.setAccessible(true);
					fieldProp.set(object, auxValue);
				}			
				else if(visibility != Visibility.PRIVATE && method != null)
				{
					method.invoke(object, auxValue);
				}
			}
			
		} catch (SecurityException e) {
			e.printStackTrace();		
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) { 
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		
		return object;		
	}
	
	public static <T> T setValueInObjectInDepth(T object, Class<? extends Object> clase, String prop, Object value, Visibility visibility)
	{
		T res = null;
		
		try
		{
			res = setValueInObjectError(object, prop, value, visibility);
		} catch (NoSuchFieldException e) {
			if(clase.getSuperclass() != null)
				return setValueInObjectInDepth(object, clase.getSuperclass(), prop,value, visibility);
		}
		
		return res;
	}
	
	public static <T> T getValueFromObject(Object object, String prop, Visibility visibility)
	{
		try {
			return getValueFromObject(object, object.getClass(), prop, visibility);
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T getValueFromObject(Object object, Class<? extends Object> clase, String prop, Visibility visibility) throws NoSuchFieldException
	{
		T res = null;		
		
		Field fieldProp = null;
		Method method = null;
		
		try {
			if(visibility == Visibility.PRIVATE)			
				fieldProp = clase.getDeclaredField(prop);			
			else
			{	
				method = clase.getDeclaredMethod("get" + StringUtils.capitalizeFirst(prop));
			}
		
			if(visibility == Visibility.PRIVATE && fieldProp != null)
			{
				fieldProp.setAccessible(true);				
				res = (T) fieldProp.get(object);
			}			
			else if(visibility != Visibility.PRIVATE && method != null)
			{
				res = (T) method.invoke(object, new Object[]{});
			}
			
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) { 
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		
		return res;		
	}
	
	public static <T> T getValueFromObjectInDepth(Object object, Class<? extends Object> clase, String prop, Visibility visibility)
	{
		T res = null;
		
		try
		{
			res = getValueFromObject(object, clase, prop, visibility);
		} catch (NoSuchFieldException e) {
			if(clase.getSuperclass() != null)
				return getValueFromObjectInDepth(object, clase.getSuperclass(), prop, visibility);
		}
		
		return res;
	}

	public static <T> T executeGetMethodInObject(Object object, String method, Object param, Visibility visibility)
	{
		return executeGetMethodInObject(object, method, new Object[]{param}, visibility);
	}
	
	public static <T> T executeGetMethodInObjectInDepth(Object object, Class<?> clase, String method, Object[] params, Visibility visibility)
	{
		T res = null;
		
		try
		{
			res = _executeGetMethodInObject(object, method, params, visibility);
		} catch (NoSuchMethodException e) {
			if(clase.getSuperclass() != null)
				return executeGetMethodInObjectInDepth(object, clase.getSuperclass(), method, params, visibility);
		}
		
		return res;
	}
	
	@SuppressWarnings("unchecked")
	private static <T> T _executeGetMethodInObject(Object object, String method, Object[] params, Visibility visibility) throws NoSuchMethodException
	{
		T res = null;
		Class<? extends Object> clase = object.getClass();
		Method _method = null;
		
		try {
					
			if(visibility == Visibility.PRIVATE)			
				_method = clase.getMethod(method);			
			else
			{	
				List<Class<? extends Object>> types = new ArrayList<Class<? extends Object>>(params.length);
				
				for(int i=0; i<params.length; i++)
					types.add(params[i].getClass());
				
				_method = clase.getDeclaredMethod(method, types.toArray(new Class<?>[params.length]));
			}	
		
			if(visibility == Visibility.PRIVATE && _method != null)
			{
				_method.setAccessible(true);				
				res = (T) _method.invoke(object, params);
			}			
			else if(visibility != Visibility.PRIVATE && method != null)
			{
				res = (T) _method.invoke(object, params);			
			}
		
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		}
		
		return res;
	}
	
	public static <T> T executeGetMethodInObject(Object object, String method, Object[] params, Visibility visibility)
	{
		T res = null;		
		
		try {
					
			res = _executeGetMethodInObject(object, method, params, visibility);			
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}
		
		return res;
	}
	
	public static void executeMethodInObject(Object object, String method, Object param, Visibility visibility)
	{
		executeMethodInObject(object, method, new Object[]{param}, visibility);
	}
	
	public static void executeMethodInObject(Object object, String method, Object[] params, Visibility visibility)
	{	
		Class<? extends Object> clase = object.getClass();
		Method _method = null;
		
		try {
					
			if(visibility == Visibility.PRIVATE)			
				_method = clase.getMethod(method);			
			else
			{	
				List<Class<? extends Object>> types = new ArrayList<Class<? extends Object>>(params.length);
				
				for(int i=0; i<params.length; i++)
					types.add(params[i].getClass());
				
				try
				{										
					_method = clase.getDeclaredMethod(method, types.toArray(new Class<?>[params.length]));	
				} catch (NoSuchMethodException e) {
					types = new ArrayList<Class<? extends Object>>(params.length);
					
					for(int i=0; i<params.length; i++)
						types.add(Object.class);
					
					_method = clase.getDeclaredMethod(method, types.toArray(new Class<?>[params.length]));
				}
			}	
		
			if(visibility == Visibility.PRIVATE && _method != null)
			{
				_method.setAccessible(true);				
				_method.invoke(object, params);
			}			
			else if(visibility != Visibility.PRIVATE && method != null)
			{
				_method.invoke(object, params);			
			}
		
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}
	}
	
}
