package com.codellion.golem.wrappers;

import java.util.ArrayList;


public class Golem {
	
	private String name;
	private String dataBeetle;	
	private ArrayList<AttributeGolem> attributes = new ArrayList<AttributeGolem>();
	private ArrayList<String> implementations = new ArrayList<String>(); 
	private String extension;
	private ArrayList<GolemChild> golemChilds = new ArrayList<GolemChild>();;
	
	public ArrayList<GolemChild> getGolemChilds() {
		return golemChilds;
	}
	public void setGolemChilds(ArrayList<GolemChild> golemChilds) {
		this.golemChilds = golemChilds;
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
	public ArrayList<AttributeGolem> getAttributes() {
		return attributes;
	}
	public void setAttributes(ArrayList<AttributeGolem> attributes) {
		this.attributes = attributes;
	}
	public ArrayList<String> getImplementations() {
		return implementations;
	}
	public void setImplementations(ArrayList<String> implementations) {
		this.implementations = implementations;
	}
	public String getExtension() {
		return extension;
	}
	public void setExtension(String extension) {
		this.extension = extension;
	}
}
