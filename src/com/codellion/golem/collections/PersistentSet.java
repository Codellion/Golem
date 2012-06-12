package com.codellion.golem.collections;

import static com.codellion.golem.collections.Node.Coleccion.eliminados;
import static com.codellion.golem.collections.Node.Coleccion.nuevos;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.collections.PredicateUtils;

import com.codellion.golem.GolemFactory;
import com.codellion.golem.collections.Node.Coleccion;
import com.codellion.golem.wrappers.Golem;
import com.codellion.golem.wrappers.GolemChild;
import com.codellion.golem.wrappers.StoneGolem;
import com.codellion.persistence.utils.PersistenceUtils;

public class PersistentSet extends HashSet<StoneGolem> implements Set<StoneGolem> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4883483764808935690L;
	private PersistentIterator persistIt;
	
	
	public PersistentSet() {
		_lista = new HashSet<Node>();
		_inicial = new HashSet<StoneGolem>();
	}
	
	public PersistentSet(Object parentValue, String parentName, String childName)
	{				
		Golem parentGolem = GolemFactory.getGolemBean(parentName);
		StoneGolem parent = new StoneGolem(parentValue, parentGolem);				
		Golem child = GolemFactory.getGolemBean(childName);
		
		_lista = new HashSet<Node>();
		_inicial = new HashSet<StoneGolem>();
		
		PersistenceUtils persistenceSvc = new PersistenceUtils(child.getName());
		
		GolemChild parentRef = null;
		
		for(GolemChild gCh : child.getGolemChilds())
		{
			if(gCh.getType().equals(parent.getGolem().getName()))
			{
				parentRef = gCh;
				
				break;
			}
		}		
		
		List<StoneGolem> childs = persistenceSvc.findRelationEntities(parentRef.getType(), parentRef.getName(), (Long) parent.get("id"));
		
		for (StoneGolem child_ : childs)
		{	
			StoneGolem auxChild = GolemFactory.emet(child.getName());
			auxChild.set("id", child_.get("id"));
			Node eltoNodo = new Node(Coleccion.publica, auxChild);
			
			_lista.add(eltoNodo);
			_inicial.add(auxChild);
		}
	}

	public PersistentSet(Collection<StoneGolem> lista, Object parentValue, String parentName, String childName) {

		_lista = new HashSet<Node>(); // Anadimos la lista incial
		addAll(lista); // Actualizamos el estado de todos los elementos
		_inicial = new HashSet<StoneGolem>(lista);
		commit();
	}

	// Atributos
	private final Set<Node> _lista;
	private Set<StoneGolem> _inicial;
	
	// Propiedades
	public PersistentSet listaActual() {
		Collection<Coleccion> tipos = new HashSet<Coleccion>();
		tipos.add(Coleccion.publica);
		tipos.add(nuevos);
		tipos.add(Coleccion.modificados);

		return getLista(tipos);
	}

	// / <summary>
	// / Lista Pública
	// / </summary>
	public Set<StoneGolem> publica() {
		return getLista(Coleccion.publica);

	}

	// / <summary>
	// / Lista de elementos insertados
	// / </summary>
	public Set<StoneGolem> insertados() {

		return getLista(nuevos);

	}

	// / <summary>
	// / Lista de elementos modificados
	// / </summary>
	public Set<StoneGolem> modificados() {

		return getLista(Coleccion.modificados);

	}

	// / <summary>
	// / Lista de elementos eliminados
	// / </summary>
	public Set<StoneGolem> eliminados() {

		return getLista(eliminados);

	}

	private PersistentSet getLista(Coleccion tipo) {
		Predicate testPredicate = PredicateUtils.equalPredicate(new Node(
				tipo, true));
		@SuppressWarnings("unchecked")
		Collection<Node> subLista = (Collection<Node>)CollectionUtils.select(_lista,
				testPredicate);
		
		PersistentSet listaTipada = new PersistentSet();

		for (Node eltoNodo : subLista)
			listaTipada.addInternal(eltoNodo.valor);

		return listaTipada;
	}

	private PersistentSet getLista(Collection<Coleccion> tipos) {
		Collection<Predicate> ctipos = new ArrayList<Predicate>();
		for (Coleccion tipo : tipos)
			ctipos.add(PredicateUtils.equalPredicate(new Node(tipo, true)));

		Predicate testPredicate = PredicateUtils.anyPredicate(ctipos);

		@SuppressWarnings("unchecked")
		Collection<Node> subLista = (Collection<Node>)CollectionUtils.select(_lista,
				testPredicate);
		PersistentSet listaTipada = new PersistentSet();

		for (Node eltoNodo : subLista)
			listaTipada.addInternal(eltoNodo.valor);

		return listaTipada;
	}

	private Boolean Contiene(Collection<StoneGolem> listaAExaminar, StoneGolem elto) {
		return listaAExaminar.contains(elto);
	}

	public Boolean contiene(StoneGolem elto) {
		return listaActual().contains(elto);
	}
	
	private void establecerTipo(StoneGolem elto, Coleccion tipo) {
		
		Node nodoBuscado = new Node(tipo, elto);

		Predicate predicado = PredicateUtils.equalPredicate(nodoBuscado);
		nodoBuscado = (Node) CollectionUtils.find(_lista, predicado);
		nodoBuscado.tipoNodo = tipo;
	
		_lista.add(nodoBuscado);
	}

	private void establecerValor(StoneGolem elto) {
		
		Node nodoBuscado = new Node(Coleccion.inicial, elto);

		Predicate predicado = PredicateUtils.equalPredicate(nodoBuscado);
		nodoBuscado = (Node) CollectionUtils.find(_lista, predicado);
		nodoBuscado.valor = elto;

		_lista.add(nodoBuscado);
	}

	@Override
	public boolean add(StoneGolem item) {

		if (!Contiene(listaActual(), item)) {
			if (Contiene(eliminados(), item))
				establecerTipo(item, Coleccion.publica);
			else {
				Node nodo = new Node(nuevos, item);
				_lista.add(nodo);
			}

			return true;
		} else {

			// throw new Exception("Ya existe el elemento en la lista.");
			return false;
		}
	}
	
	protected boolean addInternal(StoneGolem item) {		
		Node nodo = new Node(nuevos, item);
		_lista.add(nodo);

		return true;
	}
	
	@Override
	public boolean addAll(Collection<? extends StoneGolem> lista) {
		try {
			for (StoneGolem elto : lista) {
				if (!add(elto))
					return false;
			}

			return true;
		} catch (Exception ex) {
			return false;
		}

	}

	@Override
	public void clear() {
		_lista.clear();

	}

	@Override
	public boolean contains(Object elto) {
		Predicate pred = PredicateUtils.equalPredicate(elto);

		return CollectionUtils.exists(listaActual(), pred);
	}

	@Override
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

	@Override
	public boolean isEmpty() {
		return listaActual().isEmpty();
	}

	@Override
	public Iterator<StoneGolem> iterator() {
		if(persistIt == null)
			persistIt = new PersistentIterator(listaActual()._lista.iterator());
		return persistIt;
	}

	
	@Override
	public boolean remove(Object elto) {
		try {
			if (Contiene(insertados(), (StoneGolem) elto)) {
				_lista.remove(new Node(nuevos, (StoneGolem) elto));
			} else {
				if (Contiene(eliminados(), (StoneGolem) elto))
					return false;
				else
					establecerTipo((StoneGolem) elto, eliminados);
			}

			return true;
		} catch (Exception ex) {
			return false;
		}
	}
	
	@Override
	public boolean removeAll(Collection<?> lista) {
		for (Object elto : lista)
			if (!remove(elto))
				return false;

		return true;
	}

	@Override
	public boolean retainAll(Collection<?> lista) {

		for (Object elto : lista) {
			if (!contains(elto))
				if (!remove(elto))
					return false;
		}

		return true;
	}
	

	public void update(StoneGolem elto) {
		add(elto);
    }

	@Override
	public int size() {
		return listaActual().size();
	}

	@Override
	public Object[] toArray() {
		return listaActual().toArray();
	}
    
	@Override
	public <StoneGolem> StoneGolem[] toArray(StoneGolem[] lista) {
        return listaActual().toArray(lista);
	}

	public void commit() {
		for (StoneGolem elto : insertados())
			establecerTipo(elto, Coleccion.publica);
		for (StoneGolem elto : modificados())
			establecerTipo(elto, Coleccion.publica);
		for (StoneGolem elto : eliminados())
			remove(elto);

		_inicial.clear();
		_inicial = new HashSet<StoneGolem>(listaActual());
	}

	public void rollBack() {
		for (StoneGolem elto : _inicial) {
			if (!Contiene(listaActual(), elto))
				add(elto);
			else
				update(elto);
		}
	}
}
