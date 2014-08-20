package org.jboss.infinispan.demo;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.jboss.infinispan.demo.model.Task;

@Named
@ApplicationScoped
public class TaskService {

	@PersistenceContext
    EntityManager em;

	/**
	 * This methods should return all cache entries, currently contains mockup code. 
	 * @return
	 */
	public List<Task> findAll() {
		final CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
        final CriteriaQuery<Task> criteriaQuery = criteriaBuilder.createQuery(Task.class);
		
        Root<Task> root = criteriaQuery.from(Task.class);
        criteriaQuery.select(root);
        return em.createQuery(criteriaQuery).getResultList();
	}
	
	public List<Task> filter(String input) {
		final CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
        final CriteriaQuery<Task> criteriaQuery = criteriaBuilder.createQuery(Task.class);
		
        Root<Task> root = criteriaQuery.from(Task.class);
        criteriaQuery.where(
        		criteriaBuilder.like(
        				criteriaBuilder.upper(root.get("title").as(String.class)), 
        				"%" + input.toUpperCase() + "%"));
        return em.createQuery(criteriaQuery).getResultList();
	}

	public void create(Task task) {
		em.persist(task);
	}


	public void update(Task task) {
		em.merge(task);
	}

}
