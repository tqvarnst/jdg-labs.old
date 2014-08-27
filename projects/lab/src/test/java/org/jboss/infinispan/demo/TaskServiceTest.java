package org.jboss.infinispan.demo;

import java.io.File;
import java.util.Collection;
import java.util.Date;
import java.util.logging.Logger;

import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.infinispan.demo.model.Task;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.importer.ZipImporter;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class TaskServiceTest {
	
	Logger log = Logger.getLogger(this.getClass().getName());

	@Inject
	private TaskService taskservice;
	
	
	@Deployment
	public static WebArchive createDeployment() {
		

		return ShrinkWrap.create(ZipImporter.class, "todo-test.war").importFrom(new File("target/todo.war")).as(WebArchive.class);
		
//		return ShrinkWrap
//				.create(WebArchive.class, "todo-test.war")
//				.addClass(Config.class)
//				.addClass(Task.class)
//				.addClass(TaskService.class)
////				.addAsResource("import.sql")
////				.addAsResource("META-INF/persistence.xml",
////						"META-INF/persistence.xml")
////				.addAsWebInfResource(new File("src/main/webapp/WEB-INF/jboss-deployment-structure.xml"))
//				.addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
	}

	@Test
	@InSequence(1)
	public void should_be_deployed() {
		Assert.assertNotNull(taskservice);
	}

	@Test
	@InSequence(2)
	public void testRetrivingTasks() {
		Collection<Task> tasks = taskservice.findAll();
		Assert.assertEquals(3, tasks.size());
	}

	@Test
	@InSequence(3)
	public void testInsertTask() {
		Task task = new Task();
		task.setTitle("This is a test task");
		task.setCreatedOn(new Date());
		taskservice.insert(task);
		Collection<Task> tasks = taskservice.findAll();
		Assert.assertEquals(4, tasks.size());
	}

	@Test
	@InSequence(4)
	public void testUpdateTask() {
		Task task = new Task();
		task.setTitle("THIS IS A TEST TASK QWERTY!123456");
		task.setCreatedOn(new Date());
		taskservice.insert(task);

		log.info("###### Inserted task with id " + task.getId());
		task.setDone(true);
		task.setCompletedOn(new Date());
		taskservice.update(task);

		Collection<Task> tasks = taskservice.findAll();
		Assert.assertEquals(5,tasks.size());
		
		for (Task listTask : tasks) {
			if("THIS IS A TEST TASK QWERTY!123456".equals(listTask.getTitle())) {
				Assert.assertNotNull(listTask.getCompletedOn());
				Assert.assertEquals(true,listTask.isDone());
			}
			log.info("#### Found Task with id " + listTask.getId() + ", and title " + listTask.getTitle() + ", and version " + listTask.getVersion());
		}
	}
}
