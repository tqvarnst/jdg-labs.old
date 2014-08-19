package org.jboss.infinispan.demo;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

import org.jboss.infinispan.demo.model.Task;

@Named
@ApplicationScoped
public class TaskService {


	/**
	 * This methods should return all cache entries, currently contains mockup code. 
	 * @return
	 */
	public List<Task> findAll() {
		//TODO: Replace this mockup code with code that returns a list from JDG
		ArrayList<Task> list = new ArrayList<Task>();
		Calendar calendar = new GregorianCalendar();
		Task t1 = new Task();
		t1.setId(new Long(1));
		t1.setTitle("Send email to Anna about latest requirements");
		calendar.set(2014, 5, 30,14,43,07);
		t1.setCreatedOn(calendar.getTime());
		list.add(t1);
		Task t2 = new Task();
		t2.setId(new Long(2));
		t2.setTitle("Verify that the latest configuration contains the latest patches");
		calendar.set(2014, 6, 3,8,55,14);
		t2.setCreatedOn(calendar.getTime());
		list.add(t2);
		return list;
	}

	public void create(Task task) {
		//TODO
	}


	public void update(Task task) {
		//TODO
	}

}
