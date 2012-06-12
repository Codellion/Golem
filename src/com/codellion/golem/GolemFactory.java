package com.codellion.golem;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Stack;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import org.aspectj.weaver.patterns.ThisOrTargetAnnotationPointcut;

import com.codellion.golem.wrappers.AttributeGolem;
import com.codellion.golem.wrappers.Golem;
import com.codellion.golem.wrappers.GolemChild;
import com.codellion.golem.wrappers.StoneGolem;
import com.codellion.schemas.Schema;
import com.codellion.utils.spring.ApplicationContextLoader;
import com.codellion.utils.xml.XmlSchemaReader;

public class GolemFactory {
	
	private static Map<String, Golem> golems;
	private static Properties configuration;
	private static Schema schema;		
	private static GolemClassLoader forge;
	private static Stack<Golem> dependencyCompiler;
	private static ApplicationContextLoader golemContext; 
	private static Object rootClass;
	
	
	public static ApplicationContextLoader getGolemContext() {
		return golemContext;
	}

	public static GolemClassLoader getForge() {
		return forge;
	}

	public static Schema getSchema() {
		if(schema == null)
			schema = XmlSchemaReader.getSchema("com/codellion/schemas/xml/golem.xml");
		
		return schema;
	}

	public static Properties getConfiguration() {
		configuration = reloadProperties();
		
		return configuration;
	}

	public static void setConfiguration(Properties configuration) {
		GolemFactory.configuration = configuration;
	}

	public static Map<String, Golem> getGolems() {
		return golems;
	}
	
	public static Golem getGolemBean(String name)
	{		
		if(golems != null && golems.containsKey(name))
			return golems.get(name);
		else
			return null;
	}
	
	public static void initForge(Object rootClass_)
	{					
		loadGolemShapes();
		invokeGolems();
		
		rootClass = rootClass_;
		
		golemContext = new ApplicationContextLoader();
		golemContext.load(rootClass, "applicationContext.xml");
	}
	
	public static void reloadGolemContext()
	{					
		for(Golem golem : dependencyCompiler)
		{
			getGolemClass(getNumExistClass(golem.getName()));			
		}
		
		golemContext.reloadContext();
	}
	
	public static void reloadGolemShape()
	{
		String pathGolems = getConfiguration().getProperty("reloadPath");
		loadGolemShapes(pathGolems);
		invokeGolems();
		
		reloadGolemContext();
	}
	
	public static void reloadGolemShape(String name)
	{
		String pathGolems = getConfiguration().getProperty("reloadPath");
		String pathSep = System.getProperty("file.separator");
		
		if(!pathGolems.endsWith(pathSep))
			pathGolems += pathSep;
		
		Golem golem = getGolemBean(name);
		
		String pGolem =  pathGolems + golem.getName() + ".xml";
		Golem newGolem = XmlSchemaReader.readXML(pGolem, getSchema());
			
		golems.put(newGolem.getName(), newGolem);
		
		invokeGolem(name);
		
		reloadGolemContext();
	}
	
	public static void loadGolemShapes()
	{
		String pathGolems = getConfiguration().getProperty("path");
		loadGolemShapes(pathGolems);
	}
	
	public static void loadGolemShapes(String pathGolems)
	{	
		List<String> golemNames = new ArrayList<String>();
		
		String pathSep = System.getProperty("file.separator");
				
		if(!pathGolems.endsWith(pathSep))
			pathGolems += pathSep;
		
		golems = new HashMap<String, Golem>();		
							
		File dirGolems = null;
		
		try {
			dirGolems = getDirGolemShape(pathGolems);
		} catch (IOException e) { 
			e.printStackTrace();
			
			return;
		}
			
		for(File filGolem : dirGolems.listFiles())
		{
			if(filGolem.isDirectory())
				continue;
			
			String golem = filGolem.getName();
			String pGolem = pathGolems + golem;
			
			Golem newGolem = XmlSchemaReader.readXML(pGolem, getSchema());		
			
			if(newGolem != null && newGolem.getName() != null)
			{
				golems.put(newGolem.getName(), newGolem);
				golemNames.add(newGolem.getName());
			}
		}
		
		recalculeDepedencies(golemNames);
	}
	
	public static void loadGolemShapesWithConfiguration()
	{	
		List<String> golemNames = new ArrayList<String>();
		
		String pathGolems = getConfiguration().getProperty("path");
		String pathSep = System.getProperty("file.separator");
				
		if(!pathGolems.endsWith(pathSep))
			pathGolems += pathSep;
		
		golems = new HashMap<String, Golem>();
		
		for(String golem : getConfiguration().getProperty("golems").split(";"))
		{
			String pGolem = pathGolems + golem + ".xml";
			Golem newGolem = XmlSchemaReader.readXML(pGolem, getSchema());
			
			if(newGolem != null && newGolem.getName() != null)
			{
				golems.put(newGolem.getName(), newGolem);
				golemNames.add(newGolem.getName());
			}
		}
		
		recalculeDepedencies(golemNames);
	}
	
	private static void recalculeDepedencies(List<String> golemNames)
	{
		dependencyCompiler = new Stack<Golem>();
		
		for(String name : golems.keySet())
		{
			Golem golem = golems.get(name);
			Boolean found = false;		
			
			for(GolemChild att : golem.getGolemChilds())
			{
				String type = att.getType();
				
				if(golemNames.contains(type) && !existGolemClass(type))
				{						
					Golem golemDep = golems.get(type);
					
					if(dependencyCompiler.contains(golemDep))
					{
						do {
							Golem golemAux = dependencyCompiler.pop();
							dependencyCompiler.push(golem);
							dependencyCompiler.push(golemAux);
						}while(!dependencyCompiler.peek().equals(golemDep));							
					}
					else if(!dependencyCompiler.contains(golem))
					{						
						dependencyCompiler.push(golem);
					}
					
					found = true;				
				}
			}
			
			if(!found)
			{
				dependencyCompiler.push(golem);
			}
		}
	}
	
	private static void recalculeDepedencies(String golemName)
	{
		dependencyCompiler = new Stack<Golem>();		
	
		Golem golem = golems.get(golemName);

		List<String> golemNames = new ArrayList<String>();
		
		for(String golemN : golems.keySet())
		{
			golemNames.add(golemN);
		}
		
		dependencyCompiler.push(golem);
					
		for(GolemChild att : golem.getGolemChilds())
		{
			String type = att.getType();
					
			if(golemNames.contains(type) && !existGolemClass(type))
			{
				Golem golemDep = golems.get(type);
				
				if(!dependencyCompiler.contains(golemDep))
				{					
					dependencyCompiler.push(golemDep);
				}
			}
		}
	}
	
	protected static File forgeGolem(Golem golem) throws IOException
	{
		String pathSep = System.getProperty("file.separator");				
		File dirTemp = getDirGolem("beans");
		
		String suffix = getNumExistClass(golem.getName());
		
		File golemShape = new File(dirTemp.getAbsolutePath() + pathSep + suffix + ".java");		
		Writer output = null;
		
		List<String> lines = new ArrayList<String>();
		
		String package_ = "package com.codellion.golem.beans;";
		lines.add(package_);
		
		try {
			output = new BufferedWriter(new FileWriter(golemShape));
		
			StringBuilder contents = new StringBuilder();
			
			List<String> importedTypes = new ArrayList<String>();
			
			for(AttributeGolem att : golem.getAttributes())
			{
				if(!importedTypes.contains(att.getType()))
				{
					lines.add("import " +  att.getType() + ";\n");
					importedTypes.add(att.getType());
				}
			}
			
			Boolean dataBeetle = golem.getDataBeetle().toLowerCase().equals("true");
			
			if(dataBeetle)
			{
				if(golem.getImplementations() == null)
					golem.setImplementations(new ArrayList<String>());
					
				golem.getImplementations().add("Serializable");
				golem.getImplementations().add("IMutable");
				golem.getImplementations().add("Comparable<" + suffix + ">");
				
				lines.add("import javax.persistence.Entity;");
				lines.add("import javax.persistence.Column;");
				lines.add("import javax.persistence.GeneratedValue;");
				lines.add("import javax.persistence.Id;");
				lines.add("import javax.persistence.Table;");
				lines.add("import javax.persistence.Transient;");
				lines.add("import javax.persistence.InheritanceType;");
				lines.add("import javax.persistence.Inheritance;");
				lines.add("import javax.persistence.CascadeType;");
				lines.add("import javax.persistence.FetchType;");
				lines.add("import javax.persistence.JoinTable;");				
				lines.add("import javax.persistence.OneToOne;");
				lines.add("import javax.persistence.ManyToOne;");
				lines.add("import javax.persistence.ManyToMany;");
				lines.add("import javax.persistence.OneToMany;\n");
				lines.add("import com.codellion.golem.mutation.IMutable;");
				lines.add("import com.codellion.golem.mutation.GenericMutation;");
				lines.add("import com.codellion.golem.collections.PersistentSet;\n");
				
				lines.add("import java.io.Serializable;\n");
							
				lines.add("@Table(name = \"" + golem.getName().toUpperCase() + "\")");
				lines.add("@Inheritance(strategy = InheritanceType.JOINED)");
				
				lines.add("@Entity");
			}			
			
			String preClass = "public class " + suffix;
						
			if(golem.getExtension() != null)
			{
				preClass = preClass.concat(" extends " + golem.getExtension());
			}
			
			if(golem.getImplementations() != null)
			{
				preClass = preClass.concat(" implements ");
				int count = 1;
				for(String impl : golem.getImplementations())
				{
					preClass = preClass.concat(impl);
					
					if(golem.getImplementations().size() > 1 && count < golem.getImplementations().size())
						preClass = preClass.concat(", ");
					
					count++;
				}
			}
			
			preClass = preClass.concat(" {");
			lines.add(preClass);
			
			if(dataBeetle)
			{
				lines.add("@Id");
				lines.add("@GeneratedValue");
				lines.add("@Column(name = \"ID\")");
				lines.add("private Long id;");
				lines.add("public Long getId() { return id; } ;\n");
				lines.add("public void setId(Long id) { this.id = id; } ;\n");
			}
			
			for(AttributeGolem att : golem.getAttributes())
			{
				if(att.getDataBeetle().toLowerCase().equals("false"))
					lines.add("@Transient");
					
				lines.add("private " + att.getType() + " " +  att.getName() + ";");
			}
			
			for(GolemChild att : golem.getGolemChilds())
			{
				if(att.getDataBeetle().toLowerCase().equals("false"))
					lines.add("@Transient");
				else
				{										
					lines.add(getRelationJPA(att, golem));				
				}
				
				String typeSGolem = att.getType();
				
				Boolean multi = false;
				
				if(att.getRelation().toLowerCase().equals("mtm")
						|| att.getRelation().toLowerCase().equals("mtmo") 
						|| att.getRelation().toLowerCase().equals("otm") )
				{
					typeSGolem = String.format("java.util.Set<%s>", typeSGolem);
					multi = true;
				}
				
				lines.add("private " + typeSGolem + " " +  att.getName() + ";");
				
				if(multi)
				{
					lines.add("@Transient");
					lines.add("private PersistentSet " + att.getName() + "Transient;");
				}
			}
			
			String toString = "\"" + golem.getName() + ": \" + ";
			
			for(AttributeGolem att : golem.getAttributes())
			{
				String senCase = att.getName().substring(0,1).toUpperCase() 
						+ att.getName().substring(1, att.getName().length());
				
				Boolean canRead = false;
				Boolean canWrite = false;
				
				canRead = att.getAccessor().equalsIgnoreCase("all") || att.getAccessor().equalsIgnoreCase("read");
				canWrite = att.getAccessor().equalsIgnoreCase("all") || att.getAccessor().equalsIgnoreCase("write");
				
				
				if(canWrite)
				{
					lines.add("public void set" + senCase + "( " +  att.getType() + " value){");
					lines.add(att.getName() + " = value;}");
				}
				
				if(canRead)
				{
					lines.add("public " + att.getType() + " get" + senCase + "(){");
					lines.add("return " + att.getName() + ";}");
				}
				
				toString += "\"" +  att.getName() + " -> \" + ("+ att.getName() + " != null? " + att.getName() + ".toString() : \"\" ) + \", \" + ";
			}
			
			for(GolemChild att : golem.getGolemChilds())
			{
				String senCase = att.getName().substring(0,1).toUpperCase() 
						+ att.getName().substring(1, att.getName().length());
				
				Boolean canRead = false;
				Boolean canWrite = false;
				
				canRead = att.getAccessor().equalsIgnoreCase("all") || att.getAccessor().equalsIgnoreCase("read");
				canWrite = att.getAccessor().equalsIgnoreCase("all") || att.getAccessor().equalsIgnoreCase("write");
				
				String typeSGolem = att.getType();
				Boolean multi = false;
				
				if(att.getRelation().toLowerCase().equals("mtm")
						|| att.getRelation().toLowerCase().equals("mtmo") 
						|| att.getRelation().toLowerCase().equals("otm") )
				{
					typeSGolem = String.format("java.util.Set<%s>", typeSGolem);
					multi = true;
				}
				
				
				if(canWrite)
				{
					lines.add("public void set" + senCase + "( " +  typeSGolem + " value){");
					lines.add(att.getName() + " = value;}");
				}
				
				if(canRead)
				{
					lines.add("public " + typeSGolem + " get" + senCase + "(){");					
					lines.add("return " + att.getName() + ";}");
					
					if(multi)
					{
						lines.add("public PersistentSet get" + senCase + "Childs(){");
						lines.add("if(" + att.getName() + "Transient == null) " 
								+ att.getName() + "Transient = new PersistentSet(this, \"" + golem.getName() + "\",\"" + att.getType() + "\");" );
						lines.add("return " + att.getName() + "Transient; }" );
					}
				}
				
				if(!multi)
					toString += "\"" +  att.getName() + " -> \" + ("+ att.getName() + " != null? " + att.getName() + ".toString() : \"\" ) + \", \" + ";
			}
			
			if(golem.getExtension() != null)
			{
				try
				{
					Class<?> type = null;
					
					if(forge != null)
						type = forge.loadClass(golem.getName());
					else
						type = Class.forName(golem.getExtension());
					
					if(type != null)
					{
						StringBuffer buf = new StringBuffer(toString);

						
						for(Field field : type.getFields())
						{
							buf.append("\"" +  field.getName() + " -> \" + ("+ field.getName() + " != null? " + field.getName() + ".toString() : \"\" ) + \", \" + ");
						}
						
						toString = buf.toString();
					}
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}
			
			//toString
			toString = "public String toString() { return " + toString.substring(0, toString.length()- 10) + "; }";
			lines.add(toString);
			
			//Comparable
			lines.add("public int compareTo(" + suffix + " obj) { return this.getId().compareTo(obj.getId());}");
			
			//IMutable
			lines.add("public Object mutateTo(Class<?> classMutate) { return GenericMutation.mutateTo(this, classMutate);}");
			
			lines.add("}");
			
			String sep = System.getProperty("line.separator");
			
			for(String line_ : lines)			
				contents.append(line_ + sep);
						 
			output.write(contents.toString());
			output.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return golemShape;
	}
	
	protected static File forgeTransientGolem(Golem golem) throws IOException
	{
		String pathSep = System.getProperty("file.separator");				
		File dirTemp = getDirGolem("beans");
		
		String suffix = getNumExistClass(golem.getName());
		
		File golemShape = new File(dirTemp.getAbsolutePath() + pathSep + suffix + ".java");		
		Writer output = null;
		
		List<String> lines = new ArrayList<String>();
		
		String package_ = "package com.codellion.golem.beans;";
		lines.add(package_);
		
		try {
			output = new BufferedWriter(new FileWriter(golemShape));
		
			StringBuilder contents = new StringBuilder();						
			
			Boolean dataBeetle = golem.getDataBeetle().toLowerCase().equals("true");
			
			if(dataBeetle)
			{
				if(golem.getImplementations() == null)
					golem.setImplementations(new ArrayList<String>());
					
				golem.getImplementations().add("Serializable");
				
				lines.add("import javax.persistence.Entity;");
				lines.add("import javax.persistence.Column;");
				lines.add("import javax.persistence.GeneratedValue;");
				lines.add("import javax.persistence.Id;");
				lines.add("import javax.persistence.Table;");
				lines.add("import javax.persistence.Transient;");
				lines.add("import javax.persistence.InheritanceType;");
				lines.add("import javax.persistence.Inheritance;");
				lines.add("import javax.persistence.CascadeType;");
				lines.add("import javax.persistence.FetchType;");
				lines.add("import javax.persistence.JoinTable;");				
				lines.add("import javax.persistence.OneToOne;");
				lines.add("import javax.persistence.ManyToOne;");
				lines.add("import javax.persistence.ManyToMany;");
				lines.add("import javax.persistence.OneToMany;\n");
				
				lines.add("import java.io.Serializable;\n");
							
				lines.add("@Table(name = \"" + golem.getName().toUpperCase().replace("TRANSIENT", "") + "\")");
				lines.add("@Inheritance(strategy = InheritanceType.JOINED)");
				
				lines.add("@Entity");
			}			
			
			String preClass = "public class " + suffix + " implements Serializable";
					
			preClass = preClass.concat(" {");
			lines.add(preClass);
			
			if(dataBeetle)
			{
				lines.add("@Id");
				lines.add("@GeneratedValue");
				lines.add("@Column(name = \"ID\")");
				lines.add("private Long id;");
				lines.add("public Long getId() { return id; } ;\n");
				lines.add("public void setId(Long id) { this.id = id; } ;\n");
			}			
			
			for(GolemChild att : golem.getGolemChilds())
			{
				if(att.getDataBeetle().toLowerCase().equals("false"))
					lines.add("@Transient");
				else
				{										
					lines.add(getRelationJPA(att, golem));				
				}
				
				String typeSGolem = att.getType() + "Transient";
				
				if(att.getRelation().toLowerCase().equals("mtm")
						|| att.getRelation().toLowerCase().equals("mtmo") 
						|| att.getRelation().toLowerCase().equals("otm") )
				{
					typeSGolem = String.format("java.util.Set<%s>", typeSGolem);
				}
				
				lines.add("private " + typeSGolem + " " +  att.getName() + ";");
		
				String senCase = att.getName().substring(0,1).toUpperCase() 
						+ att.getName().substring(1, att.getName().length());
				
				Boolean canRead = false;
				Boolean canWrite = false;
				
				canRead = att.getAccessor().equalsIgnoreCase("all") || att.getAccessor().equalsIgnoreCase("read");
				canWrite = att.getAccessor().equalsIgnoreCase("all") || att.getAccessor().equalsIgnoreCase("write");
											
				if(canWrite)
				{
					lines.add("public void set" + senCase + "( " +  typeSGolem + " value){");
					lines.add(att.getName() + " = value;}");
				}
				
				if(canRead)
				{
					lines.add("public " + typeSGolem + " get" + senCase + "(){");					
					lines.add("return " + att.getName() + ";}");					
				}
			}
			
			String toString = "public String toString() { return super.toString(); }";
			lines.add(toString);
			
			lines.add("}");
			
			String sep = System.getProperty("line.separator");
			
			for(String line_ : lines)			
				contents.append(line_ + sep);
						 
			output.write(contents.toString());
			output.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return golemShape;
	}
	
	protected static File createISvcDao(Golem golem) throws IOException
	{		
		String pathSep = System.getProperty("file.separator");
				
		File dirTemp = getDirGolem("persistence" + pathSep + "svc");
		
		String suffix = getNumExistClass(golem.getName());
				
		File golemShape = new File(dirTemp.getAbsolutePath() + pathSep + "I" + suffix+ "Dao.java");		
		Writer output = null;
		
		List<String> lines = new ArrayList<String>();
		
		String package_ = "package com.codellion.golem.persistence.svc;\n";
		lines.add(package_);
		
		try {
			output = new BufferedWriter(new FileWriter(golemShape));
		
			lines.add("import com.codellion.persistence.interfaces.PersistenceService;");
			lines.add("import com.codellion.golem.beans." +  suffix + ";\n");
			lines.add("public interface I" + suffix + "Dao extends PersistenceService<"+ suffix + "> { }");
			
			String res = "";
			String sep = System.getProperty("line.separator");
						
			StringBuffer buf = new StringBuffer();
			
			for(String line_ : lines)
				buf.append(line_ + sep);
							
			res = buf.toString();
				
			output.write(res);
			output.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return golemShape;
	}
	
	protected static File createSvcDao(Golem golem) throws IOException
	{
		String pathSep = System.getProperty("file.separator");
				
		File dirTemp = getDirGolem("persistence" + pathSep + "svc");
		
		String suffix = getNumExistClass(golem.getName());
		
		File golemShape = new File(dirTemp.getAbsolutePath() + pathSep + suffix + "DaoImpl.java");		
		Writer output = null;
		
		List<String> lines = new ArrayList<String>();
		
		String package_ = "package com.codellion.golem.persistence.svc;\n";
		lines.add(package_);
		
		try {
			output = new BufferedWriter(new FileWriter(golemShape));
			
			lines.add("import org.springframework.transaction.annotation.Transactional;");
			lines.add("import org.springframework.stereotype.Repository;");
			lines.add("import com.codellion.persistence.svc.jpa.PersistenceJPAImpl;");
			lines.add("import com.codellion.golem.beans." +  suffix + ";");
			lines.add("import com.codellion.golem.persistence.svc.I" +  suffix + "Dao;\n");
			lines.add("@Transactional"); 
			lines.add("@Repository");
			lines.add("public class " + suffix + "DaoImpl extends PersistenceJPAImpl<"+ suffix+ "> ");
			lines.add("implements I" + suffix + "Dao { " + 
					"public " + suffix + "DaoImpl() { setClazz(" + suffix +".class); } }");
			
			String res = "";
			String sep = System.getProperty("line.separator");
			
			StringBuffer buf = new StringBuffer();

			
			for(String line_ : lines)			
				buf.append(line_ + sep);

			res = buf.toString();
			
			output.write(res);
			output.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return golemShape;
	}
	
	public static void invokeGolems()
	{
		if(getGolems() == null)
			return;
		
		invokeGolem();
	}
	
	public static void invokeGolem(String name)
	{	
		recalculeDepedencies(name);
		
		invokeGolem();
	}
	
	public static void invokeGolem()
	{			
		List<File> files = new ArrayList<File>();
		
		try
		{
			for(Golem golem_ : dependencyCompiler.toArray(new Golem[]{}))
			{				
				files.add(forgeGolem(golem_));
				
				if(golem_.getDataBeetle().toLowerCase().equals("true"))
				{				
					files.add(createISvcDao(golem_));
					files.add(createSvcDao(golem_));
					
					Golem transientGolem = new Golem();
					
					transientGolem.setDataBeetle(golem_.getDataBeetle());
					transientGolem.setName(golem_.getName() + "Transient");
					transientGolem.setGolemChilds(golem_.getGolemChilds());
					
					files.add(forgeTransientGolem(transientGolem));
					files.add(createISvcDao(transientGolem));
					files.add(createSvcDao(transientGolem));
				}
			}			
		}
		catch (IOException ioe) {
			ioe.printStackTrace();
		}
		
		
		invokeGolem(files);
	}
	
	private static void invokeGolem(List<File> files)
	{	
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();
		StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, null, null);
	    
		Iterable<? extends JavaFileObject> compilationUnits =
		           fileManager.getJavaFileObjectsFromFiles(files);
		
		List<String> optionList = new ArrayList<String>();	
		optionList.addAll(Arrays.asList("-classpath",System.getProperty("java.class.path")));
		
	    compiler.getTask(null, fileManager, diagnostics, optionList, null, compilationUnits).call();

	    for (Diagnostic<? extends JavaFileObject>  diagnostic : diagnostics.getDiagnostics())
	           System.out.println(diagnostic.toString());

	    try {
			fileManager.close();
			
			if(forge == null)
				forge = new GolemClassLoader(GolemFactory.class.getClassLoader());
			
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}
	
	public static StoneGolem emet(String name)
	{
		StoneGolem res = null;
		Golem golem = getGolemBean(name);
		
		Class<?> cls = null;
		
		try {			
			cls = forge.loadClass(golem.getName());
			
			if(cls != null)
			{			
				Object soul = cls.newInstance();			
				res = new StoneGolem(soul, golem);
			}			
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		
		return res;
	}
	
	public static String getNumExistClass(String className)
	{	
		String res = className;
		
		if(forge == null || !forge.getGolemClasses().containsKey(className))
			return res;
					
		Stack<Class<?>> stackClass = forge.getGolemClasses().get(className);
		res += "_" + String.valueOf(stackClass.size());
							
		return res;
	}
	
	public static Class<?> getGolemClass(String className)
	{			
		Class<?> cls = null;
		
		try {			
			
			cls = forge.loadClass(className);			
			
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		
		return cls;
	}
	
	public static Boolean existGolemClass(String className)
	{
		Boolean res = false;
		
		if(forge != null && forge.getGolemClasses().containsKey(className))
			return true;
		
		return res;
	}
	
	public static File getDirGolem(String subDir) throws IOException
	{			
		String pathSep = System.getProperty("file.separator");
		String classPath = System.getProperty("user.dir");
		String sdirTempGolem = classPath + pathSep + "com" + pathSep + "codellion" + pathSep + "golem" + pathSep + subDir;
		
		File dirTempGolem  = new File(sdirTempGolem);
		
		if(!dirTempGolem .exists())
		{
			sdirTempGolem = classPath + pathSep  + "bin" + pathSep 
				+ "com" + pathSep + "codellion" + pathSep + "golem" + pathSep + subDir;
			dirTempGolem  = new File(sdirTempGolem);
		}
		
		if(!dirTempGolem.exists())
			if(!dirTempGolem.mkdirs())
				throw new IOException("No se ha podido crear la carpeta de los golems binarios");
		
		return dirTempGolem;
	}
	
	public static File getDirGolemShape(String subDir) throws IOException
	{		
		String pathSep = System.getProperty("file.separator");
		String classPath = System.getProperty("user.dir");
		String sdirTempGolem = classPath + pathSep + subDir;
		
		File dirTempGolemShape = new File(sdirTempGolem);
		
		if(!dirTempGolemShape.exists())
		{
			sdirTempGolem = classPath + pathSep  + "bin" + pathSep + subDir;
			dirTempGolemShape = new File(sdirTempGolem);
		}
		
		if(!dirTempGolemShape.exists())
			if(!dirTempGolemShape.mkdirs())
				throw new IOException("No se ha podido crear la carpeta de los golems shapes");
	
		
		return dirTempGolemShape;
	}
	
	private static Properties reloadProperties() {
		Properties props_ = new Properties();
				
		try {
			InputStream ficheroProp = GolemFactory.class.getClassLoader().getResourceAsStream("golem-cfg.properties");

			props_.load(ficheroProp);
		} catch (IOException e) {
			e.printStackTrace();
		}

		return props_;
	}
	
	private static String getRelationJPA(GolemChild gChild, Golem golem)
	{		
		String rel = gChild.getRelation();
		
		if(rel.toLowerCase().equals("otm"))			
			return String.format("@OneToMany(mappedBy=\"%s\", cascade = CascadeType.ALL, fetch = FetchType.LAZY)", 
					 gChild.getMappedBy());
		if(rel.toLowerCase().equals("mto"))
			return "@ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)";
		if(rel.toLowerCase().equals("mtmo"))		
			return "@ManyToMany\n@JoinTable(name=\"" + golem.getName().toUpperCase().replace("TRANSIENT", "") +  "_" + gChild.getType().toUpperCase() + "\")";
		if(rel.toLowerCase().equals("mtm"))
			return "@ManyToMany(mappedBy=\"" + gChild.getMappedBy() + "\")";
		
		
		return "@OneToOne";
	}
	
}

