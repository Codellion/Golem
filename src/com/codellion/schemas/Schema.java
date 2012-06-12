package com.codellion.schemas;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class Schema {
	
	private RootSchema root;	
	private Map<ElementSchema, Collection<ElementSchema>> elements;
	private Map<String, Collection<PropertySchema>> properties;
	
	public Schema()
	{
		elements = new HashMap<ElementSchema, Collection<ElementSchema>>();
		properties = new HashMap<String, Collection<PropertySchema>>();
	}
	
	
	public RootSchema getRoot() {
		return root;
	}
	public void setRoot(RootSchema root) {
		this.root = root;
	}
	public Map<ElementSchema, Collection<ElementSchema>> getElements() {
		return elements;
	}
	public void setElements(Map<ElementSchema, Collection<ElementSchema>> elements) {
		this.elements = elements;
	}
	public Map<String, Collection<PropertySchema>> getProperties() {
		return properties;
	}
	public void setProperties(Map<String, Collection<PropertySchema>> properties) {
		this.properties = properties;
	}
}
