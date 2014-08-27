package org.jboss.infinispan.demo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.ejb.Stateless;

import org.jboss.infinispan.demo.model.Task;

/**
 * This is a mockup class where tasks are stored as a local variable. 
 * @author tqvarnst
 *
 */
@Stateless
public class TaskService {

	
	ArrayList<Task> tasks = new ArrayList<Task>();
	
	Logger log = Logger.getLogger(this.getClass().getName());

	/**
	 * This methods should return all cache entries, currently contains mockup code. 
	 * @return
	 */
	public Collection<Task> findAll() {
		
		return tasks;
	}
	

	public void insert(Task task) {
		if(task.getId()==null) {
			task.setId(tasks.size()+1L); //Ugly way to create unique ID's
		}
		tasks.add(task);
		
	}

	
	public void update(Task task) {
		tasks.remove(task);
		tasks.add(task);
	}
	
	@PostConstruct
	public void startup() {
		Task task1 = new Task();
		task1.setId(1L);
		task1.setTitle("Complete the labs in JDG + EAP");
		task1.setCreatedOn(new Date());
		tasks.add(task1);
		Task task2 = new Task();
		task2.setId(1L);
		task2.setTitle("Sell more JDG");
		task2.setCreatedOn(new Date());
		tasks.add(task2);
		Task task3 = new Task();
		task3.setId(1L);
		task3.setTitle("Sell more EAP");
		task3.setCreatedOn(new Date());
		task3.setDone(true);
		task3.setCompletedOn(new Date());
		tasks.add(task3);
	}
	
}
