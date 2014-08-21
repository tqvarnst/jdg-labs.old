package org.jboss.infinispan.demo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.cache.annotation.CacheResult;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.apache.lucene.search.Query;
import org.hibernate.search.query.dsl.QueryBuilder;
import org.infinispan.Cache;
import org.infinispan.query.CacheQuery;
import org.infinispan.query.Search;
import org.infinispan.query.SearchManager;
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
		return cache.values();
	}
	
	public List<Task> filter(String input) {
		SearchManager sm = Search.getSearchManager(cache);
		QueryBuilder qb = sm.buildQueryBuilderForClass(Task.class).get();
		Query q = qb.keyword().onField("title").matching(input).createQuery();
		CacheQuery cq = sm.getQuery(q, Task.class);
		List<Task> tasks = new ArrayList<Task>();
		for (Object object : cq) {
			tasks.add((Task) object);
		}
		return tasks;
		
//		log.info("### Querying the database for filtered tasks!!!!");
//		final CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
//        final CriteriaQuery<Task> criteriaQuery = criteriaBuilder.createQuery(Task.class);
//		
//        Root<Task> root = criteriaQuery.from(Task.class);
//        criteriaQuery.where(
//        		criteriaBuilder.like(
//        				criteriaBuilder.upper(root.get("title").as(String.class)), 
//        				"%" + input.toUpperCase() + "%"));
//        return em.createQuery(criteriaQuery).getResultList();
	}

	public void insert(Task task) {
		if(task.getCreatedOn()==null) {
			task.setCreatedOn(new Date());
		}
		em.persist(task);
		cache.put(task.getId(),task);
	}

	
	public void update(Task task) {
		em.merge(task);
		cache.replace(task.getId(),task);
	}
	
	@PostConstruct
	public void startup() {
		
		log.info("### Querying the database for tasks!!!!");
		final CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
		final CriteriaQuery<Task> criteriaQuery = criteriaBuilder.createQuery(Task.class);
	
		Root<Task> root = criteriaQuery.from(Task.class);
		criteriaQuery.select(root);
		Collection<Task> resultList = em.createQuery(criteriaQuery).getResultList();
		
		for (Task task : resultList) {
			this.insert(task);
		}
		
	}
	
}
