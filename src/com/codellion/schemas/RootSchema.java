package com.codellion.schemas;

import java.util.Collection;

public class RootSchema {
	
	private String name;
	private String className;	
	private Collection<PropertySchema> properties;	

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getClassName() {
		return className;
	}
	public void setClassName(String className) {
		this.className = className;
	}
	public Collection<PropertySchema> getProperties() {
		return properties;
	}
	public void setProperties(Collection<PropertySchema> properties) {
		this.properties = properties;
	}
}
