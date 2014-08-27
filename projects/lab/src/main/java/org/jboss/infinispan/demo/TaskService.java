package org.jboss.infinispan.demo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Stateless;
import javax.inject.Inject;

import org.infinispan.client.hotrod.RemoteCache;
import org.jboss.infinispan.demo.model.Task;

/**
 * This is a mockup class where tasks are stored as a local variable. 
 * @author tqvarnst
 * 
 * TODO: implement a JDG hotrod client to store the task in a data grid
 * TODO: Change from @Singleton to @Stateless since singelton will not scale
 *
 */
@Stateless 
public class TaskService {

	
	Logger log = Logger.getLogger(this.getClass().getName());
	
	@Inject
	RemoteCache<String, Object> cache;

	/**
	 * This methods should return all cache entries, currently contains mockup code. 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public Collection<Task> findAll() {
		List<String> taskKeys = (List<String>) cache.get("TASKKEYS");
		log.info("### Found " + taskKeys.size() + " keys.");
		ArrayList<Task> tasks = new ArrayList<Task>();
		for (Object taskKey : taskKeys) {
			log.info("Found object of type " + taskKey.getClass() + " That looks loke this " + taskKey.toString());
			tasks.add((Task) cache.get(taskKey));
		}
		return tasks;
	}
	

	@SuppressWarnings("unchecked")
	public void insert(Task task) {
		int nextKey = cache.size() + 1;
		task.setId(new Long(nextKey));
		String strNextKey = Integer.toString(nextKey);
		List<String> taskKeys = (List<String>) cache.get("TASKKEYS");
		cache.put(strNextKey, task);
		taskKeys.add(strNextKey);
		cache.put("TASKKEYS", taskKeys);
	}

	
	public void update(Task task) {
		cache.replace(Long.toString(task.getId()), task);
	}
	
	@SuppressWarnings("unchecked")
	@PostConstruct
	public void startup() {
		
		// Make sure that the cache contains a object with teamkeys
		List<String> taskKeys = new ArrayList<String>();
		cache.putIfAbsent("TASKKEYS", taskKeys);
		
		taskKeys = (List<String>) cache.get("TASKKEYS");
		
		
		// If nothing in the list prefill with some content
		if (taskKeys.size()==0) {
			Task task1 = new Task();
			task1.setId(1L);
			task1.setTitle("Complete the labs in JDG + EAP");
			task1.setCreatedOn(new Date());
			cache.put("1", task1);
			taskKeys.add("1");
			Task task2 = new Task();
			task2.setId(2L);
			task2.setTitle("Sell more JDG");
			task2.setCreatedOn(new Date());
			cache.put("2", task2);
			taskKeys.add("2");
			Task task3 = new Task();
			task3.setId(3L);
			task3.setTitle("Sell more EAP");
			task3.setCreatedOn(new Date());
			task3.setDone(true);
			task3.setCompletedOn(new Date());
			cache.put("3", task3);
			taskKeys.add("3");
			cache.replace("TASKKEYS", taskKeys);
		}
		
		
	}
	
}
