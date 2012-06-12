package com.codellion.collections;

import static com.codellion.collections.Node.Coleccion.eliminados;
import static com.codellion.collections.Node.Coleccion.nuevos;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.collections.PredicateUtils;

import com.codellion.collections.Node.Coleccion;

public class ManagedList<T extends Comparable<T>> implements List<T> {

	public ManagedList() {
		_lista = new LinkedList<Node<T>>();
		_inicial = new LinkedList<T>();
	}

	public ManagedList(Collection<T> lista) {

		_lista = new LinkedList<Node<T>>(); // A�adimos la lista incial
		addAll(lista); // Actualizamos el estado de todos los elementos
		_inicial = new LinkedList<T>(lista);
		commit();
	}

	// Atributos
	private final List<Node<T>> _lista;
	private List<T> _inicial;

	// Propiedades
	public LinkedList<T> listaActual() {
		Collection<Coleccion> tipos = new ArrayList<Coleccion>();
		tipos.add(Coleccion.publica);
		tipos.add(nuevos);
		tipos.add(Coleccion.modificados);

		return getLista(tipos);
	}

	// / <summary>
	// / Lista P�blica
	// / </summary>
	public List<T> publica() {
		return getLista(Coleccion.publica);

	}

	// / <summary>
	// / Lista de elementos insertados
	// / </summary>
	public List<T> insertados() {

		return getLista(nuevos);

	}

	// / <summary>
	// / Lista de elementos modificados
	// / </summary>
	public List<T> modificados() {

		return getLista(Coleccion.modificados);

	}

	// / <summary>
	// / Lista de elementos eliminados
	// / </summary>
	public List<T> eliminados() {

		return getLista(eliminados);

	}

	private LinkedList<T> getLista(Coleccion tipo) {
		Predicate testPredicate = PredicateUtils.equalPredicate(new Node<T>(
				tipo, true));
		@SuppressWarnings("unchecked")
		Collection<Node<T>> subLista = (Collection<Node<T>>)CollectionUtils.select(_lista,
				testPredicate);
		LinkedList<T> listaTipada = new LinkedList<T>();

		for (Node<T> eltoNodo : subLista)
			listaTipada.add(eltoNodo.valor);

		return listaTipada;
	}

	private LinkedList<T> getLista(Collection<Coleccion> tipos) {
		Collection<Predicate> ctipos = new ArrayList<Predicate>();
		for (Coleccion tipo : tipos)
			ctipos.add(PredicateUtils.equalPredicate(new Node<T>(tipo, true)));

		Predicate testPredicate = PredicateUtils.anyPredicate(ctipos);

		@SuppressWarnings("unchecked")
		Collection<Node<T>> subLista = (Collection<Node<T>>)CollectionUtils.select(_lista,
				testPredicate);
		LinkedList<T> listaTipada = new LinkedList<T>();

		for (Node<T> eltoNodo : subLista)
			listaTipada.add(eltoNodo.valor);

		return listaTipada;
	}

	private Boolean Contiene(Collection<T> listaAExaminar, T elto) {
		return listaAExaminar.contains(elto);
	}

	public Boolean contiene(T elto) {
		return listaActual().contains(elto);
	}

	@SuppressWarnings("unchecked")
	private void establecerTipo(T elto, Coleccion tipo) {
		int ind;

		Node<T> nodoBuscado = new Node<T>(tipo, elto);

		Predicate predicado = PredicateUtils.equalPredicate(nodoBuscado);
		nodoBuscado = (Node<T>) CollectionUtils.find(_lista, predicado);
		nodoBuscado.tipoNodo = tipo;

		ind = _lista.indexOf(nodoBuscado);

		_lista.set(ind, nodoBuscado);
	}

	@SuppressWarnings("unchecked")
	private void establecerValor(T elto) {
		int ind;

		Node<T> nodoBuscado = new Node<T>(Coleccion.inicial, elto);

		Predicate predicado = PredicateUtils.equalPredicate(nodoBuscado);
		nodoBuscado = (Node<T>) CollectionUtils.find(_lista, predicado);
		nodoBuscado.valor = elto;

		ind = _lista.indexOf(nodoBuscado);

		_lista.set(ind, nodoBuscado);
	}

	public boolean add(T item) {

		if (!Contiene(listaActual(), item)) {
			if (Contiene(eliminados(), item))
				establecerTipo(item, Coleccion.publica);
			else {
				Node<T> nodo = new Node<T>(nuevos, item);
				_lista.add(nodo);
			}

			return true;
		} else {

			// throw new Exception("Ya existe el elemento en la lista.");
			return false;
		}
	}

	
	public void add(int index, T eltoNodo) {
		Node<T> nodoABuscar = new Node<T>(nuevos, listaActual().get(index));

		int indexNodo = CollectionUtils.cardinality(nodoABuscar, _lista);

		_lista.add(indexNodo, new Node<T>(nuevos, eltoNodo));
	}

	
	public boolean addAll(Collection<? extends T> lista) {
		try {
			for (T elto : lista) {
				if (!add(elto))
					return false;
			}

			return true;
		} catch (Exception ex) {
			return false;
		}

	}

	
	public boolean addAll(int index, Collection<? extends T> lista) {
		try {
			Node<T> nodoABuscar = new Node<T>(nuevos, listaActual().get(index));
            			
			int indexNodo = CollectionUtils.cardinality(nodoABuscar, _lista);

			for (T eltoLista : lista) {
				_lista.add(indexNodo, new Node<T>(nuevos, eltoLista));
				indexNodo++;
			}

			return true;
		} catch (Exception ex) {
			return false;
		}

	}

	
	public void clear() {
		_lista.clear();

	}

	
	public boolean contains(Object elto) {
		Predicate pred = PredicateUtils.equalPredicate(elto);

		return CollectionUtils.exists(listaActual(), pred);
	}

	
	public boolean containsAll(Collection<?> lista) {

		for (Object elto : lista) {
			if (Object.class.equals(Node.class)) {
				if (!contains(elto))
					return false;
			} else
				return false;
		}

		return true;
	}

	
	public T get(int index) {
		if (index < -1 || index >= listaActual().size())
			return null;

		return listaActual().get(index);
	}

	
	public int indexOf(Object elto) {
		return listaActual().indexOf(elto);
	}

	
	public boolean isEmpty() {
		return listaActual().isEmpty();
	}

	
	public Iterator<T> iterator() {
		return listaActual().iterator();
	}

	
	public int lastIndexOf(Object elto) {
		return listaActual().lastIndexOf(elto);
	}

	
	public ListIterator<T> listIterator() {
		return listaActual().listIterator();
	}

	
	public ListIterator<T> listIterator(int index) {
		return listaActual().listIterator(index);
	}

	
	@SuppressWarnings("unchecked")
	public boolean remove(Object elto) {
		try {
			if (Contiene(insertados(), (T) elto)) {
				_lista.remove(new Node<T>(nuevos, (T) elto));
			} else {
				if (Contiene(eliminados(), (T) elto))
					return false;
				else
					establecerTipo((T) elto, eliminados);
			}

			return true;
		} catch (Exception ex) {
			return false;
		}
	}

	
	public T remove(int index) {
		T elto = get(index);

		remove(elto);

		return elto;
	}

	
	public boolean removeAll(Collection<?> lista) {
		for (Object elto : lista)
			if (!remove(elto))
				return false;

		return true;
	}

	
	public boolean retainAll(Collection<?> lista) {

		for (Object elto : lista) {
			if (!contains(elto))
				if (!remove(elto))
					return false;
		}

		return true;
	}

	
	public T set(int index, T elto) {
		T eltoOld = get(index);

		if (!Contiene(listaActual(), elto))
			return null;
		else {
			if (!Contiene(insertados(), elto))
				establecerTipo(elto, Coleccion.modificados);

			establecerValor(elto);
		}

		return eltoOld;
	}

	public void update(T elto) {
		int index = indexOf(elto);
        set(index, elto);
    }

	
	public int size() {
		return listaActual().size();
	}

	
	public List<T> subList(int index1, int index2) {
		return listaActual().subList(index1, index2);
	}

	
	public Object[] toArray() {
		return listaActual().toArray();
	}
    
	@SuppressWarnings("hiding")
	public <T> T[] toArray(T[] lista) {
        return listaActual().toArray(lista);
	}

	public void commit() {
		for (T elto : insertados())
			establecerTipo(elto, Coleccion.publica);
		for (T elto : modificados())
			establecerTipo(elto, Coleccion.publica);
		for (T elto : eliminados())
			remove(elto);

		_inicial.clear();
		_inicial = new LinkedList<T>(listaActual());
	}

	public void rollBack() {
		for (T elto : _inicial) {
			if (!Contiene(listaActual(), elto))
				add(elto);
			else
				update(elto);
		}
	}
}
