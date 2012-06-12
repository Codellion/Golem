package com.codellion.golem.wrappers;


public class AttributeGolem {

	private String type;
	private String name;
	private String dataBeetle = new String();
	private String accessor = "ALL";
	
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getDataBeetle() {
		return dataBeetle;
	}
	public void setDataBeetle(String dataBeetle) {
		this.dataBeetle = dataBeetle;
	}
	public String getAccessor() {
		return accessor;
	}
	public void setAccessor(String accessor) {
		this.accessor = accessor;
	}	
}
