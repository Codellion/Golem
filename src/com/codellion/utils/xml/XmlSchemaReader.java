package com.codellion.utils.xml;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.codellion.schemas.ElementSchema;
import com.codellion.schemas.PropertySchema;
import com.codellion.schemas.RootSchema;
import com.codellion.schemas.Schema;
import com.codellion.utils.reflexion.ReflexUtils;

public class XmlSchemaReader {

	public static Schema getSchema(String schemaFile) {
		Schema schema = new Schema();

		DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory
				.newInstance();
		DocumentBuilder docBuilder;
		
		try {
			docBuilder = docBuilderFactory.newDocumentBuilder();
			
			InputStream ficheroXML = XmlSchemaReader.class.getClassLoader().getResourceAsStream(schemaFile);
			
			Document doc = docBuilder.parse(ficheroXML);

			// normalize text representation
			doc.getDocumentElement().normalize();
			
			// Establecemos el root del esquema			
			Element root = (Element) doc.getFirstChild();
			
			RootSchema rootS = new RootSchema();
			rootS.setName(root.getAttribute("name"));
			rootS.setClassName(root.getAttribute("class"));
			
			schema.setRoot(rootS);
			
			NodeList attributes = root.getElementsByTagName("elements");
			NodeList properties = root.getElementsByTagName("properties");
			
			// Recogemos los atributos			
			for (int s = 0; s < attributes.getLength(); s++) {
				Node nodo = attributes.item(s);
				
				if (nodo.getNodeType() == Node.ELEMENT_NODE)
				{
					Element element = (Element) nodo;
					
					String attsName = element.getAttribute("name");
					String attsType = element.getAttribute("type");
					
					Collection<ElementSchema> elements = new ArrayList<ElementSchema>();
					
					NodeList attributesChild = element.getElementsByTagName("element");
					
					for (int s2 = 0; s2 < attributesChild.getLength(); s2++) {
						Node subNodo = attributesChild.item(s2);
						
						if (subNodo.getNodeType() == Node.ELEMENT_NODE)
						{
							Element subElement = (Element) subNodo;
							
							String attName = subElement.getAttribute("name");
							String attType = subElement.getAttribute("type");
							
							ElementSchema elto = new ElementSchema();
							elto.setName(attName);
							elto.setType(attType);
														
							elements.add(elto);
						}
					}
					
					ElementSchema elto = new ElementSchema();
					elto.setName(attsName);
					elto.setType(attsType);
					
					schema.getElements().put(elto, elements);					
				}
			}
			
			// Recogemos las propiedades			
			for (int s = 0; s < properties.getLength(); s++) {
				Node nodo = properties.item(s);
				
				if (nodo.getNodeType() == Node.ELEMENT_NODE)
				{
					Element element = (Element) nodo;
					
					String propsName = element.getAttribute("element");
					
					Collection<PropertySchema> propertiesCol = new ArrayList<PropertySchema>();
					
					NodeList propertiesChild = element.getElementsByTagName("property");
					
					for (int s2 = 0; s2 < propertiesChild.getLength(); s2++) {
						Node subNodo = propertiesChild.item(s2);
						
						if (subNodo.getNodeType() == Node.ELEMENT_NODE)
						{
							Element subElement = (Element) subNodo;
							
							String propName = subElement.getAttribute("name");
							
							PropertySchema prop = new PropertySchema();
							prop.setName(propName);
							prop.setElement(propsName);
							
							propertiesCol.add(prop);
						}
					}
					
					if("root".equals(propsName))
						schema.getRoot().setProperties(propertiesCol);
					else					
						schema.getProperties().put(propsName, propertiesCol);					
				}
			}
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return schema;
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T readXML(String xmlName, Schema schema)
	{
		T res = null;
		
		DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory
				.newInstance();
		DocumentBuilder docBuilder;
		
		// Leemos el root con sus propiedades
		String className = schema.getRoot().getClassName();
		
		try {
			res = (T) Class.forName(className).newInstance();
			
			docBuilder = docBuilderFactory.newDocumentBuilder();
			
			InputStream ficheroXML = Thread.currentThread()
			        .getContextClassLoader().getResourceAsStream("./" + xmlName.toLowerCase());
			
			Document doc = docBuilder.parse(ficheroXML);

			// normalize text representation
			doc.getDocumentElement().normalize();
			
			// Establecemos el root del esquema			
			Element root = (Element) doc.getFirstChild();
			
			if(!root.getNodeName().equals(schema.getRoot().getName()))
				root = doc.getElementById(schema.getRoot().getName());
			
			for(PropertySchema prop: schema.getRoot().getProperties())
			{
				String property = root.getAttribute(prop.getName());
				
				if(!"".equals(property))
					ReflexUtils.setValueInObject(res, prop.getName(), property, ReflexUtils.Visibility.PRIVATE);
			}
			
			for(ElementSchema mapAtt : schema.getElements().keySet())
			{
				Collection<ElementSchema> elements = schema.getElements().get(mapAtt);
				
				NodeList elementsNodes = doc.getElementsByTagName(mapAtt.getName());
				
				for(int i=0; i<elementsNodes.getLength(); i++)
				{
					Node nodo = elementsNodes.item(i);
					
					if (nodo.getNodeType() == Node.ELEMENT_NODE)
					{
						Element elementoNodo = (Element) nodo;
					
						if(schema.getProperties().containsKey(mapAtt.getName()))
						{						
							//Propiedades del elemento
							for(PropertySchema prop: schema.getProperties().get(mapAtt.getName()))
							{
								String property =elementoNodo.getAttribute(prop.getName());
								
								if(!"".equals(property))
									ReflexUtils.setValueInObject(res, prop.getName(), property, ReflexUtils.Visibility.PRIVATE);
							}
						}
						
						//Subelementos
						fillSubElements(nodo, elements, schema.getProperties(), res);
					}
				}
			}
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return res;
	}
	
	private static void fillSubElements(Node subNodo, Collection<ElementSchema> elements, Map<String, Collection<PropertySchema>> properties, Object res)
	{	
		Object col = null;
		Class<? extends Object> clase = null;
		Element superElementoNodo = null;
		
		if (subNodo.getNodeType() == Node.ELEMENT_NODE)
		{
			superElementoNodo = (Element) subNodo;
			
			try {	
				
				Field fieldProp = res.getClass().getDeclaredField(subNodo.getNodeName());
				fieldProp.setAccessible(true);
				
				clase = fieldProp.getType();
								
				try
				{					
					col = clase.newInstance();
				}
				catch(InstantiationException e)
				{
					if(clase.isArray())
						col = new ArrayList<Object>();
				}
			} catch (IllegalAccessException e) {
				e.printStackTrace();			
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (NoSuchFieldException e) {
				e.printStackTrace();
			}	
		}
		
		for(ElementSchema element : elements)
		{
			NodeList subNodos = subNodo.getChildNodes();
			
			for(int i=0; i<subNodos.getLength(); i++)
			{
				Node nodo = subNodos.item(i);
				
				if (nodo.getNodeType() == Node.ELEMENT_NODE && nodo.getNodeName().equals(element.getName()))
				{
					Element elementoNodo = (Element) nodo;
					
					Object tipoRes;
					
					try {	
						
						tipoRes =  Class.forName(element.getType()).newInstance();
														
						//Propiedades del elemento
						for(PropertySchema prop: properties.get(element.getName()))
						{
							String property =elementoNodo.getAttribute(prop.getName());
							
							if(!"".equals(property))
								ReflexUtils.setValueInObject(tipoRes, prop.getName(), property, ReflexUtils.Visibility.PRIVATE);
						}	
						
						if(tipoRes != null && col != null)
						{
							for(Class<? extends Object> interfaz: col.getClass().getInterfaces())
							{
								if(isCollection(interfaz))
								{
									ReflexUtils.executeMethodInObject(col, "add", (Object) tipoRes, ReflexUtils.Visibility.PUBLIC);
									break;
								}
								else if(isMap(interfaz))
								{
									ReflexUtils.executeMethodInObject(col, "add", (Object) tipoRes, ReflexUtils.Visibility.PUBLIC);
									break;
								}
							}
						}
						
						
					} catch (InstantiationException e) {
						e.printStackTrace();
					} catch (IllegalAccessException e) {
						e.printStackTrace();
					} catch (ClassNotFoundException e) {
						e.printStackTrace();
					}	
				}
			}				
		}		
		
		if(clase != null && clase.isArray())
		{					
			ReflexUtils.setValueInObject(res, subNodo.getNodeName(), 
					ReflexUtils.executeGetMethodInObject(col, "toArray", null, ReflexUtils.Visibility.PUBLIC), ReflexUtils.Visibility.PRIVATE);
		}
		else
			ReflexUtils.setValueInObject(res, subNodo.getNodeName(), col, ReflexUtils.Visibility.PRIVATE);
					
		if(properties.containsKey(subNodo.getNodeName()))
		{
			//Propiedades del elemento
			for(PropertySchema prop: properties.get(subNodo.getNodeName()))
			{
				String property =superElementoNodo.getAttribute(prop.getName());
				
				if(!"".equals(property))
					ReflexUtils.setValueInObject(res, prop.getName(), property, ReflexUtils.Visibility.PRIVATE);
			}
		}
	}

	
	private static Boolean isCollection(Class<?> type)
	{	
		if(type.getInterfaces() == null || type.getInterfaces().length == 0)
			return false;
		
		for(Class<? extends Object> interfaz: type.getInterfaces())
		{
			if(interfaz.equals(Collection.class))
				return true;
			else
				return isCollection(interfaz);
		}	
		
		return false;
	}
	
	private static Boolean isMap(Class<?> type)
	{
		if(type.getInterfaces() == null || type.getInterfaces().length == 0)
			return false;
		
		for(Class<? extends Object> interfaz: type.getInterfaces())
		{
			if(interfaz.equals(Map.class))
				return true;
			else
				return isCollection(interfaz);
		}	
		
		return false;
	}
}
