package com.example.testMain;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import sun.applet.AppletClassLoader;

import com.codellion.golem.GolemFactory;
import com.codellion.golem.collections.PersistentSet;
import com.codellion.golem.wrappers.StoneGolem;
import com.codellion.persistence.utils.PersistenceUtils;

public class Main {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Main main = new Main();
				
		GolemFactory.initForge(main);
		        
		StoneGolem michelin = GolemFactory.emet("Rueda");
		michelin.set("diametro", new Float(17.5));
		michelin.set("marca", "Michelin");
		
		StoneGolem dunlop = GolemFactory.emet("Rueda");
		dunlop.set("diametro", new Float(15));		
		dunlop.set("marca", "Dunlop");
		
		PersistenceUtils persistenceSvc = new PersistenceUtils("Rueda");
		persistenceSvc.create(dunlop);
		persistenceSvc.create(michelin);
		
		StoneGolem mercedes = GolemFactory.emet("MarcaCoche");
		mercedes.set("nombre", "Mercedes");
		mercedes.set("descripcion", "Marca alemana de coches de alta gama");
		
		StoneGolem opel = GolemFactory.emet("MarcaCoche");
		opel.set("nombre", "opel");
		opel.set("descripcion", "Marca alemana de utilitarios");
		
		persistenceSvc = new PersistenceUtils("MarcaCoche");
		persistenceSvc.create(opel);
		persistenceSvc.create(mercedes);
		
		
	    StoneGolem copel = GolemFactory.emet("Coche");		
	    copel.set("marca", opel.getSoul());
	    copel.set("anno", 1998);
	    copel.set("color", "verde");
	    copel.set("cv", 1700);
				
	    Set<Object> ruedasOpel = new HashSet<Object>();
	    ruedasOpel.add(dunlop.getSoul());
	    ruedasOpel.add(michelin.getSoul());
	    
	    copel.set("ruedas", ruedasOpel);
	    
	    persistenceSvc = new PersistenceUtils("Coche");
	    persistenceSvc.create(copel);
							
		//GolemFactory.reloadGolemShape("Coche");
		//persistenceSvc = new PersistenceUtils("Coche");	
		
		StoneGolem cmercedes = GolemFactory.emet("Coche");		
		cmercedes.set("marca", mercedes.getSoul());
		cmercedes.set("anno", 2008);
		cmercedes.set("color", "plata");
		cmercedes.set("cv", 2200);
		cmercedes.set("puertas", 5);
		
		Set<Object> ruedasMercedes = new HashSet<Object>();		
		ruedasMercedes.add(michelin.getSoul());
		ruedasMercedes.add(dunlop.getSoul());
		
		cmercedes.set("ruedas", ruedasMercedes);
			
		persistenceSvc.create(cmercedes);		
		Set<StoneGolem> ruedas = copel.getChilds("ruedas");
		
//		Iterator<StoneGolem> it = ruedas.iterator();
//		
//		while(it.hasNext())
//		{
//			StoneGolem rueda = it.next();
//			System.out.println(rueda.toString());
//		}
		
		for(StoneGolem rueda : ruedas)
		{	
			System.out.println(rueda.toString());
		}
		
//		MutationUtils.addProperty(Prueba.class, "b", String.class);
//		StoneGolem prueba1 = GolemFactory.emet("Prueba");
//		
//		MutationUtils.addProperty(prueba1.getGolem(), "c", String.class);
//		StoneGolem prueba2 = GolemFactory.emet("Prueba");
//		
//		prueba1.set("a", "A");
//		prueba1.set("b", "B");
//		
//		prueba2.set("a", "1");
//		prueba2.set("b", "2");
//		prueba2.set("c", "3");
//		
//		System.out.println(coche1.getDefinition());
//		System.out.println(coche1.toString());
//		System.out.println(coche2.toString());
//		System.out.println(coche3.toString());
//		System.out.println();

	}

}
