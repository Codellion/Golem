package com.codellion.persistence.svc.jpa;

import java.io.Serializable;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.springframework.transaction.annotation.Transactional;

import com.codellion.golem.GolemFactory;
import com.codellion.persistence.interfaces.PersistenceService;


public class PersistenceJPAImpl<T extends Serializable> implements
		PersistenceService<T> {

	private static Log logger = LogFactory.getFactory().getInstance(
			PersistenceJPAImpl.class);

	private Class<T> clazz;
	private Class<?> transientClazz;
		
	@PersistenceContext(unitName="simple-jpa") 
	protected EntityManager entityManager;
	
	public EntityManager getEntityManager() {
		return entityManager;
	}

	public void setEntityManager(EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	public void setClazz(final Class<? extends Object> clazzToSet) {
		this.clazz = (Class<T>) clazzToSet;
	}
	
	public Class<T> getClazz()
	{
		return clazz;
	}
	
	public Class<?> getTransientClazz()
	{
		if(transientClazz == null)		
			transientClazz = GolemFactory.getGolemClass(clazz.getName() + "Transient");
		
		return transientClazz;
	}

	public T getById(final Long id) {
		return this.entityManager.find(this.clazz, id);
	}

	public List<T> getAll() {
		return this.entityManager.createQuery("from " + this.clazz.getName())
				.getResultList();
	}

	public T findByObject(T entity) {
		return this.entityManager.find(clazz, entity);
	}
	
	public List<T> findRelationEntities(String relEntityName, String relPropName, Long relEntityId) {
	
		Query q = this.entityManager.createQuery("select c1 from " +  getTransientClazz().getSimpleName() + " c1, " 
				+ relEntityName + " c2 where c2 member of c1." + relPropName + " and c2.id = :identifier");
		
		q.setParameter("identifier", relEntityId);				
		List<T> res = q.getResultList();
				
		return res;
	}
	
	
	@Transactional
	public void create(final T entity) {	
		this.entityManager.persist(entity);
	
	}

	@Transactional
	public void update(final T entity) {
		this.entityManager.merge(entity);
	}

	@Transactional
	public void delete(final T entity) {
		this.entityManager.remove(entity);
	}

	@Transactional
	public void deleteById(final Long entityId) {
		final T entity = this.getById(entityId);

		this.delete(entity);
	}
}