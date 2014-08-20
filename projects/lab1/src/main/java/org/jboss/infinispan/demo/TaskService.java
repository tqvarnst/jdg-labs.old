package org.jboss.infinispan.demo;

import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

import javax.cache.annotation.CacheResult;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.infinispan.Cache;
import org.jboss.infinispan.demo.model.Task;

@Named
@ApplicationScoped
public class TaskService {

	@PersistenceContext
    EntityManager em;
	
	@Inject
	Cache<Long,Task> cache;
	
	Logger log = Logger.getLogger(this.getClass().getName());

	/**
	 * This methods should return all cache entries, currently contains mockup code. 
	 * @return
	 */
	@CacheResult
	public Collection<Task> findAll() {
		Collection<Task> resultList = cache.values();
		if(resultList==null) { 
			final CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
			final CriteriaQuery<Task> criteriaQuery = criteriaBuilder.createQuery(Task.class);
		
			Root<Task> root = criteriaQuery.from(Task.class);
			criteriaQuery.select(root);
			resultList = em.createQuery(criteriaQuery).getResultList();
		}
		 return resultList;
	}
	
//	public List<Task> filter(String input) {
//		final CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
//        final CriteriaQuery<Task> criteriaQuery = criteriaBuilder.createQuery(Task.class);
//		
//        Root<Task> root = criteriaQuery.from(Task.class);
//        criteriaQuery.where(
//        		criteriaBuilder.like(
//        				criteriaBuilder.upper(root.get("title").as(String.class)), 
//        				"%" + input.toUpperCase() + "%"));
//        return em.createQuery(criteriaQuery).getResultList();
//	}

	public void create(Task task) {
		em.persist(task);
		cache.put(task.getId(),task);
	}

	
	public void update(Task task) {
		em.merge(task);
		cache.replace(task.getId(),task);
	}
	
}
