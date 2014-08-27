package org.jboss.infinispan.demo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Set;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.inject.Inject;

import org.infinispan.client.hotrod.RemoteCache;
import org.jboss.infinispan.demo.model.Task;


@Stateless 
public class TaskService {

	
	public static final String KEYLIST = "TASKKEYS";

	Logger log = Logger.getLogger(this.getClass().getName());
	
	@Inject
	RemoteCache<Long, Task> cache;

	/**
	 * This methods should return all cache entries, currently contains mockup code. 
	 * @return
	 */
	public Collection<Task> findAll() {
		Set<Long> keySet = cache.keySet();
		ArrayList<Task> tasks = new ArrayList<Task>();
		for (Long key : keySet) {
			Task task = cache.get(key);
			tasks.add(task);
		}
		return tasks;
	}
	

	public void insert(Task task) {
		if(task.getCreatedOn()==null) {
			task.setCreatedOn(new Date());
		}
		int nextKey = cache.size() + 1;
		task.setId(new Long(nextKey));
		cache.put(task.getId(), task);
	}

	
	public void update(Task task) {
		cache.put(task.getId(), task);
			
	}
	
	public void delete(Task task) {
		this.delete(task.getId());
	}
	
	public void delete(Long id) {
		cache.remove(id);
	}
	
	@PostConstruct
	public void startup() {
	
	}
	
}
