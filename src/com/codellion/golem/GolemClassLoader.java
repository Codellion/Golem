package com.codellion.golem;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import org.springframework.context.ApplicationContext;

import sun.applet.AppletClassLoader;
import sun.org.mozilla.javascript.internal.Context;


public class GolemClassLoader extends ClassLoader {
	
	private enum golemClass{
		BEAN,
		SVC,
	}
			
	private Map<String, Stack<Class<?>>> golemClasses;
	
	public Map<String, Stack<Class<?>>> getGolemClasses()
	{
		return golemClasses;
	}
	
    public GolemClassLoader(ClassLoader parent) {
        super(parent);
    	golemClasses = new HashMap<String, Stack<Class<?>>>();
    }
        
    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
    
    	golemClass typeClass = golemClass.BEAN;
    	
    	if(name.contains("com.codellion.golem.persistence.svc."))
    	{
    		typeClass = golemClass.SVC;
    		name = name.replace("com.codellion.golem.persistence.svc.", "");
    	}
    	else    		
    		name = name.replace("com.codellion.golem.beans.", "");
    	
    	
    	Boolean refactorClass = false;
    	String refactorName = name;    	
    	Integer refactorVersion = 1;
    	    	
    	
    	if(refactorName.contains("_"))
    	{
    		String refAux = refactorName;
    		
    		refactorName = refAux.split("_")[0];
    		refactorVersion = Integer.valueOf(refAux.split("_")[1]);
    		refactorClass = true;
    	}
       	
    	try
    	{
    		if(golemClasses != null && golemClasses.containsKey(refactorName))
    		{
    			Stack<Class<?>> stackClass = new Stack<Class<?>>();
    			stackClass.addAll(golemClasses.get(refactorName));
    			
    			if(!refactorClass || (refactorClass && stackClass.size() > refactorVersion))
    			{    			
    				Class<?> class_ = null;
    			
	    			if(!stackClass.isEmpty())
	    			{
	    				class_ = stackClass.pop();
	    			}
	    			
	    			return class_;
    			}
    		}
    		else
    			return super.loadClass(name);
    	}
    	catch (ClassNotFoundException e) {
    		
		}
    	
        try {
        	String pathSep = System.getProperty("file.separator");
			URL main = GolemFactory.class.getResource("GolemFactory.class");
			
			if (!"file".equalsIgnoreCase(main.getProtocol()))
			  throw new IllegalStateException("Main class is not stored in a file.");
			
			File path = new File(main.getPath());				
			
			File dirTemp = null;
			
			if(typeClass == golemClass.BEAN)
				dirTemp = new File(path.getParentFile().getAbsolutePath() + pathSep + "beans" + pathSep);
			else
				dirTemp = new File(path.getParentFile().getAbsolutePath() + pathSep + "persistence" + pathSep + "svc" + pathSep);
        	
            String url = dirTemp.getAbsolutePath() + pathSep + name + ".class";
            url = URLDecoder.decode(url, System.getProperty("file.encoding"));
                             
            File classComp = new File(url);
            
            if(!classComp.exists())
            	return null;
            
            URL myUrl = classComp.toURI().toURL();            
            URLConnection connection = myUrl.openConnection();
            InputStream input = connection.getInputStream();
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            int data = input.read();

            while(data != -1){
                buffer.write(data);
                data = input.read();
            }

            input.close();

            byte[] classData = buffer.toByteArray();

            Class<?> class_ = null;
            
            if(typeClass == golemClass.BEAN)
            	class_ = defineClass("com.codellion.golem.beans." + name, classData, 0, classData.length);
            else
            	class_ = defineClass("com.codellion.golem.persistence.svc." + name, classData, 0, classData.length);
            
            if(golemClasses == null)            	
            	golemClasses = new HashMap<String, Stack<Class<?>>>();
            
            Stack<Class<?>> pstackClass = null;
            
            if(golemClasses.containsKey(refactorName))
            	pstackClass = golemClasses.get(refactorName);
            else
            	pstackClass = new Stack<Class<?>>();
            
            pstackClass.push(class_);            
            golemClasses.put(refactorName, pstackClass);
            
            return class_;

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace(); 
        }

        return null;
    }

}