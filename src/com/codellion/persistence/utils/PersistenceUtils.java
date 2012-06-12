package com.codellion.persistence.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.persistence.PersistenceException;

import org.springframework.stereotype.Repository;

import com.codellion.golem.GolemFactory;
import com.codellion.golem.wrappers.Golem;
import com.codellion.golem.wrappers.StoneGolem;
import com.codellion.persistence.interfaces.PersistenceService;
import com.codellion.utils.StringUtils;

public class PersistenceUtils {

	protected PersistenceService<Object> persistenceSvc;
	protected Golem golem;
	protected String lastGolemName;
		
	public PersistenceUtils(String golemBean)
	{
		golem = GolemFactory.getGolemBean(golemBean);
	}
	
	public PersistenceUtils(Golem golem)
	{
		this.golem = golem;
	}
	
	public String getLastGolemName()
	{
		if(lastGolemName == null)
		{		
			Class<?> class_ = GolemFactory.getGolemClass(golem.getName());
			
			lastGolemName = StringUtils.deCapitalizeFirst(class_.getSimpleName());
		}
		
		return lastGolemName;
	}
	
	public PersistenceService<Object> getPersistenceSvc() {
		
		if(persistenceSvc == null)
		{
			Map<String, Object> services = GolemFactory.getGolemContext().getApplicationContext()
					.getBeansWithAnnotation(Repository.class);
			
			persistenceSvc = (PersistenceService<Object>) services.get(getLastGolemName() + "DaoImpl");
		}
		
		return persistenceSvc;
	}
	
	
	public StoneGolem getById(final Long id)
	{
		return new StoneGolem(getPersistenceSvc().getById(id), golem);
	}

	public List<StoneGolem> getAll()
	{		
		List<Object> res = getPersistenceSvc().getAll();
		
		if(res == null)
			return new ArrayList<StoneGolem>();
		
		List<StoneGolem> resG = new ArrayList<StoneGolem>(res.size());
		
		for(Object resObj : res)
		{
			resG.add(new StoneGolem(resObj, golem));
		}
		
		return resG;
	}
	
	public StoneGolem findByObject(StoneGolem entity)
	{
		 return new StoneGolem(getPersistenceSvc().findByObject(entity.getSoul()), golem);
	}
	
	public List<StoneGolem> findRelationEntities(String relEntityName, String relPropName, Long relEntityId)
	{	
		List<Object> res = getPersistenceSvc().findRelationEntities(relEntityName, relPropName, relEntityId);
		
		if(res == null)
			return new ArrayList<StoneGolem>();
		
		List<StoneGolem> resG = new ArrayList<StoneGolem>(res.size());
		
		for(Object resObj : res)
		{
			resG.add(new StoneGolem(resObj, golem));
		}
		
		return resG;
	}

	public void create(StoneGolem entity)
	{
		try
		{
			getPersistenceSvc().create(entity.getSoul());	
		}		
		catch(PersistenceException ex)
		{
			//Se utiliza el update (merge) para las entidades detached de hibernate
			getPersistenceSvc().update(entity.getSoul());
		}
	}

	public void update(StoneGolem entity)
	{
		getPersistenceSvc().update(entity.getSoul());
	}

	public void delete(StoneGolem entity)
	{
		getPersistenceSvc().delete(entity.getSoul());
	}

	public void deleteById(final Long entityId)
	{
		getPersistenceSvc().deleteById(entityId);
	}
	
}
