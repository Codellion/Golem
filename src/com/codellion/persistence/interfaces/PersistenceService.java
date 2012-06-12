package com.codellion.persistence.interfaces;

import java.util.List;

import javax.persistence.EntityManager;

public abstract interface PersistenceService<T> {

	public T getById(final Long id);

	public List<T> getAll();

	public T findByObject(T entity);
	
	public List<T> findRelationEntities(String relEntityName, String relPropName, Long relEntityId);
	
	public void create(final T entity);

	public void update(final T entity);

	public void delete(final T entity);

	public void deleteById(final Long entityId);
	
	public void setEntityManager(EntityManager em);
	
	public void setClazz(Class<? extends Object> clazz);	

}
