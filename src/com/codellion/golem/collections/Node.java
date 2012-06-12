package com.codellion.golem.collections;

import com.codellion.golem.wrappers.StoneGolem;
import com.codellion.persistence.utils.PersistenceUtils;

public class Node implements Comparable<Node> {
	
	// Enumeraciones
	public static enum Coleccion {
		publica, nuevos, modificados, eliminados, inicial
	}

	public Coleccion tipoNodo;
	public Boolean fetched = false;
	public StoneGolem valor;
	private Boolean nulo = false;

	public Node(Coleccion TipoNodo, StoneGolem valor) {
		this.tipoNodo = TipoNodo;
		this.valor = valor;
	}

	public Node(Coleccion TipoNodo, Boolean nulo) {
		this.tipoNodo = TipoNodo;
		this.nulo = nulo;
	}
	
	public void fetchFullValue()
	{
		PersistenceUtils persistenceSvc = new PersistenceUtils(valor.getGolem().getName());
		valor = persistenceSvc.getById((Long)valor.get("id"));
		
		fetched = true;
	}

	public int compareTo(Node nodo) {
		return valor.compareTo(nodo.valor);
	}

	public boolean equals(Object nodo) {
		boolean result = false;

		if (nodo != null && nodo instanceof Node) {			
			Node nodoAComparar = (Node) nodo;

			if (!nodoAComparar.nulo && !nulo)
				result = (valor.equals(nodoAComparar.valor));
			else
				result = (tipoNodo.equals(nodoAComparar.tipoNodo));
		}

		return result;
	}
}
