package com.codellion.golem.mutation;

import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;

import com.codellion.golem.GolemFactory;
import com.codellion.golem.wrappers.AttributeGolem;
import com.codellion.golem.wrappers.Golem;

public class MutationUtils {
	
	public static Golem createGolemShape(Class<?> type)
	{
		Golem res = new Golem();
		
		res.setName(type.getSimpleName());
		res.setDataBeetle("false");
		res.setExtension(type.getName());
		res.setAttributes(new ArrayList<AttributeGolem>());
				
		return res;
	}
	
	public static Golem createGolemShape(String name, String token, String dataBeetle, Map<String, Class<?>> atts)
	{
		Golem res = new Golem();
		
		res.setName(name);
		res.setDataBeetle(dataBeetle);
		res.setAttributes(new ArrayList<AttributeGolem>(atts.size()));
		
		for(Entry<String, Class<?>>  propName : atts.entrySet())
		{
			AttributeGolem att = new AttributeGolem();
			att.setAccessor("ALL");
			att.setName(propName.getKey());
			att.setType(((Class<?>) propName.getValue()).getName());
			
			res.getAttributes().add(att);
		}
		
		return res;
	}
	
	public static void addProperty(Class<?> type, String propName, Class<?> propType) 
	{		
		Golem golem = createGolemShape(type);
		
		addProperty(golem,propName,propType);
	}
	
	public static void addProperty(Golem golem, String propName, Class<?> propType) 
	{		
		AttributeGolem att = new AttributeGolem();
		att.setAccessor("ALL");
		att.setName(propName);
		att.setType(propType.getName());
		
		golem.getAttributes().add(att);
		
		GolemFactory.getGolems().put(golem.getName(), golem);
		GolemFactory.invokeGolem(golem.getName());
	}
}
