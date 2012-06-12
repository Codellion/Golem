package com.codellion.golem.mutation;

import java.lang.reflect.Field;

import com.codellion.golem.GolemFactory;
import com.codellion.utils.reflexion.ReflexUtils;
import com.codellion.utils.reflexion.ReflexUtils.Visibility;

public class GenericMutation {
	
	public static Object mutateTo(Object value, Class<?> mutateClass) {
		Object res = null;
		try {
			res = mutateClass.newInstance();
			
			for(Field field :value.getClass().getDeclaredFields())
			{
				try
				{	
					Object prop = null;
					prop = ReflexUtils.getValueFromObjectInDepth(value, value.getClass(), field.getName(), Visibility.PRIVATE);
					
					ReflexUtils.setValueInObjectInDepth(res, mutateClass, 
							 field.getName(), prop, Visibility.PRIVATE);
				}
				catch (Exception e) {
				}
			}
			
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		
		return res;
	}

}
