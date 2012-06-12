package com.codellion.golem.wrappers;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import com.codellion.golem.collections.PersistentSet;
import com.codellion.persistence.utils.PersistenceUtils;
import com.codellion.utils.reflexion.ReflexUtils;
import com.codellion.utils.reflexion.ReflexUtils.Visibility;

public class StoneGolem implements Comparable<StoneGolem>{
	
	private Object soul;
	private Golem golem;
	private Boolean fetched = false;
	
	public StoneGolem(Object soul, Golem golem)
	{
		this.soul = soul;
		this.golem = golem;
	}	
	
	public Object getSoul() {
		return soul;
	}
	public void setSoul(Object soul) {
		this.soul = soul;
	}
	public Golem getGolem() {
		return golem;
	}
	public void setGolem(Golem golem) {
		this.golem = golem;
	}	
	public Boolean getFetched()
	{
		return fetched;
	}
	
	public void set(String prop, Object value)
	{	
		ReflexUtils.setValueInObjectInDepth(soul, soul.getClass(), prop, value, Visibility.PRIVATE);
	}
	
	public <T> T get(String prop)
	{
		T res = null;
		
		res = ReflexUtils.getValueFromObjectInDepth(soul, soul.getClass(), prop, Visibility.PUBLIC);
		
		return res;
	}
	
	public StoneGolem getChild(String prop)
	{
		StoneGolem res = null;
		
		res = ReflexUtils.getValueFromObjectInDepth(soul, soul.getClass(), prop + "Child", Visibility.PUBLIC);
		
		return res;
	}
	
	public PersistentSet getChilds(String prop)
	{
		PersistentSet res = null;
		
		res = ReflexUtils.getValueFromObjectInDepth(soul, soul.getClass(), prop + "Childs", Visibility.PUBLIC);
		
		return res.listaActual();
	}
		
	public void fetchFullValue()
	{
		PersistenceUtils persistenceSvc = new PersistenceUtils(getGolem().getName());
		soul = persistenceSvc.getById((Long)get("id")).getSoul();
		
		fetched = true;
	}
	
	public String toString()
	{
		return soul.toString();
	}
	
	public String getDefinition()
	{
		Class<?> type = soul.getClass();
		
		StringBuilder stb = new StringBuilder("Class: \n");
		
		stb.append(type.getCanonicalName() + "\n");
		
		stb.append("\nImplements: \n");
		
		for(Class<?> interf : type.getInterfaces())
		{
			stb.append(String.format("%s\n", interf.getName()));
		}
		
		stb.append("\nExtends: \n");
		stb.append(String.format("%s\n", type.getSuperclass().getName()));
		
		stb.append("\nAttributes: \n");
				
		for(Field field : type.getFields())
		{
			stb.append(String.format("%s\n", field.toString()));
		}
		
		for(Field field : type.getDeclaredFields())
		{
			stb.append(String.format("%s\n", field.toString()));
		}
		
		stb.append("\nMethods: \n");
		
		for(Method method : type.getMethods())
		{
			stb.append(String.format("%s\n", method.toString()));
		}
		
		return stb.toString().replace("java.lang.", "")
				.replace(Object.class.getCanonicalName() + ".", "")
				.replace(type.getCanonicalName() + ".", "");
	}

	public int compareTo(StoneGolem o) {
		return ReflexUtils.executeGetMethodInObject(this.getSoul(), "compareTo", o.getSoul(), Visibility.PUBLIC);
	}
}
