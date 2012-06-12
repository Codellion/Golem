package com.codellion.collections;

public class Node<T extends Comparable<T>> implements Comparable<Node<T>> {
	
	// Enumeraciones
	public static enum Coleccion {
		publica, nuevos, modificados, eliminados, inicial
	}

	public Coleccion tipoNodo;
	public Boolean dirty = true;
	public T valor;
	private Boolean nulo = false;

	public Node(Coleccion TipoNodo, T valor) {
		this.tipoNodo = TipoNodo;
		this.valor = valor;
	}

	public Node(Coleccion TipoNodo, Boolean nulo) {
		this.tipoNodo = TipoNodo;
		this.nulo = nulo;
	}

	public int compareTo(Node<T> nodo) {
		return valor.compareTo(nodo.valor);
	}

	public boolean equals(Object nodo) {
		boolean result = false;

		if (nodo != null && nodo instanceof Node<?>) {
			@SuppressWarnings("unchecked")
			Node<T> nodoAComparar = (Node<T>) nodo;

			if (!nodoAComparar.nulo && !nulo)
				result = (valor.equals(nodoAComparar.valor));
			else
				result = (tipoNodo.equals(nodoAComparar.tipoNodo));
		}

		return result;
	}
}
