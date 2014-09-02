# Lab Guide 1
This explains the steps for lab 1, either follow them step-by-step or if you feel adventurous read the overview and try to accomplish goals without the help of the step-by-step

![Image of Yaktocat](https://octodex.github.com/images/yaktocat.png)


## Overview of Lab 1
In lab 1 we will introduce JDG as a cache for a mockup application written in CDI, REST and AngularJS. The main steps in lab1 is to:

1. Run the JUnit/Arquillian tests
1. Install the mockup application and verify that is working
1. Add dependencies to the maven project and to the WAR file for JDG
1. Add dependencies to the JDG modules in EAP via jboss-deployment-structure.xml
1. Inject a local Cache into TaskService class and implement the logic to cache findAll.


## Step-by-Step
The step-by-step guide is dived into 3 different sections matching the main steps in the overview.

The First step over is to verify that the application build and deploy before we do anything else.

### Install and build the mock project

1. Start the JBoss EAP if not already started in a terminal.

		$ target/jboss-eap-6.3/bin/standalone.sh
		
1. In another terminal (on the dev host) change directory to the project

        $ cd project/todo
        
1. Run the JUnit test either in JBDS or by using command line. To run the test the ```arquillian-jbossas-remote-7``` profile will have to be activated.

		$ mvn -P arquillian-jbossas-remote-7 test
		
1. Build and deploy the project

        $ mvn package jboss-as:deploy
        
1. Verify in a browser that application deployed nice successfully by opening [http://localhost:8080/todo](http://localhost:8080/todo) in a browser. 

1. Click around and verify that you can add tasks and complete tasks etc.

	The Mock application is simple todo application that uses a database to store tasks. It uses angular.js on the client and the server side consists of REST services to list, create and update these tasks.
	
1. Go thourgh the code a bit to understand the application. 


### Add dependencies to the maven project
In this step-by-step section we will add dependecies to the maven project so that we can later on add the code to store tasks in JDG. 

1. Open the maven pom.xml file in project/todo in an editor or IDE and add the following in the dependencyManagement section

		<dependencyManagement>
			...
			...
			...
			<dependency>
				<groupId>org.infinispan</groupId>
				<artifactId>infinispan-bom</artifactId>
				<version>6.1.0.Final-redhat-4</version>
				<type>pom</type>
				<scope>import</scope>
			</dependency>
		</dependencyManagement>

	And add the following dependencies.

		<dependency>
			<groupId>org.infinispan</groupId>
			<artifactId>infinispan-core</artifactId>
			<scope>provided</scope>
		</dependency>
		<dependency>
			<groupId>org.infinispan</groupId>
			<artifactId>infinispan-cdi</artifactId>
			<scope>provided</scope>
		</dependency>
		
	**Note:** We use a bom file to manage the versions of the dependencies, if you choose not to use the bom file, just specify the version directly in each dependency instead. 

2. Now we need fix the class loading so that we are using the ccorrect JDG library in the container. 
   
   JBoss EAP ships with infinispan libraries internally, but since are using JDG 6.3 we must make sure that we use the correct infinispan libraries/modules. One solution is to ship the JDG libraries in the WEB-INF/lib folder but that makes the WAR grow allot in since effecting not only deploymenttime, but we also have to create a new release to patch or update JDG. The other solution is to use the JDG modules new as of JDG 6.3.

   The setup script that we run to setup the environment installs JDG as JBoss EAP modules, which means that we don't have to ship them as part of the WAR file. For example if we need to patch JDG we don't have to patch the application. We do however need to tell the cointainer (JBoss EAP) that our application depends on these modules. This can be done via adding dependencies to the ```MANIFEST.MF``` file (can be created as part of the maven built) or by using ```jboss-deployment-structure.xml```. We are going to use the later since it works better with Arquillian testing. 

   Create a file called ```jboss-deployment-structure.xml``` under ```src/main/webapp/WEB-INF``` that looks like this:

		<jboss-deployment-structure>
			<deployment>
				<dependencies>
					<module name="org.infinispan" slot="jdg-6.3" services="import"/>
					<module name="org.infinispan.cdi" slot="jdg-6.3" meta-inf="import"/>
				</dependencies>
			</deployment>
		</jboss-deployment-structure>

1. Run the build and deploy command again

		$ mvn package jboss-as:deploy
		
1. Make sure that the above command are succesfull and you are done with this section.

###Inject a local Cache into TaskService class and implement the logic to findAll, create, update. 

1. Open TaskSevice.java in an editor or IDE and add the following as a field 
to the class

		@Inject
		Cache<Long, Task> cache;
		
	you also need to add the follwing import statement if you IDE doesn't fix that
	
		import org.infinispan.Cache;
		import org.jboss.infinispan.demo.model.Task;
		
1. Change the implementation of the findAll method to look like this:

		public Collection<Task> findAll() {
			return cache.values();
		}
		
1. Change the create method to look like this:

		public void insert(Task task) {
			if(task.getCreatedOn()==null) {
				task.setCreatedOn(new Date());
			}
			em.persist(task);
			cache.put(task.getId(),task);
		}

1.	Add the implementation of the update method as shown below:

		public void update(Task task) {
			em.merge(task);
			cache.replace(task.getId(),task);
		}

1. We also need fill the cache with the existing values in the database using by adding the following method:
		
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

1. Next update the TaskServiceTest class to make use of the jboss-deployment-structure.xml by uncomment the following line:

		.addAsWebInfResource(new File("src/main/webapp/WEB-INF/jboss-deployment-structure.xml"))

1. Run the JUnit test to see that everything works as expected

1. Your TaskService.java implementation should look something like this:

		package org.jboss.infinispan.demo;
		
		import java.util.Collection;
		import java.util.Date;
		import java.util.logging.Logger;
		
		import javax.annotation.PostConstruct;
		import javax.ejb.Stateless;
		import javax.inject.Inject;
		import javax.persistence.EntityManager;
		import javax.persistence.PersistenceContext;
		import javax.persistence.criteria.CriteriaBuilder;
		import javax.persistence.criteria.CriteriaQuery;
		import javax.persistence.criteria.Root;
		
		import org.infinispan.Cache;
		import org.jboss.infinispan.demo.model.Task;
		
		@Stateless
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
			public Collection<Task> findAll() {
				return cache.values();
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

1. Hold on with deploy to the application server. There are one issue with the current setup that we will solve in the next

###Configure the cache programatically
What just happend is that we have implemented a local cache solution where we can offload the database based on the default configuraiton. We haven't yet configured any setting with the cache. There are allot of different possibilities to tweak the JDG library mode settings, but at the moment we will only do some basic configuration settings. Settings can be done in XML or in code. In this example we will use the code API, but later we will use the XML to configure JDG in standalone mode.

Below is a code snipped that shows how to create configuration objects for the cache.

		GlobalConfiguration glob = new GlobalConfigurationBuilder()
				.globalJmxStatistics().allowDuplicateDomains(true).enable() // This
				// method enables the jmx statistics of the global
				// configuration and allows for duplicate JMX domains
				.build();
		Configuration loc = new ConfigurationBuilder().jmxStatistics()
				.enable() // Enable JMX statistics
				.eviction().strategy(EvictionStrategy.NONE) // Do not evic objects
				.build();
		DefaultCacheManager manager = new DefaultCacheManager(glob, loc, true);
		
There are two main configuration object: ```GlobalConfiguration``` for the Global configuration if we use for example multiple clustred configurations and ```Configuration``` to hold the local configuration. In this example we allow muliple domains since otherwise we get a nasty exception saying that the cache allready exists. In the local configuration we enable JMX statistics (need for JON for example) and we set the eviction.strategy to NONE, meaning that no objects are evicted. 

We can then create a cache manager object using these configuration and pass it true to also start it.

Since we are using CDI in our example we can actually override the cache manager that is used when someone injects a cache with ```@Inject Cache<?,?> cache;``` like we do in TaskService class. This can be done using something called Producer in CDI. So all we have to do is crate a method that looks like this:

		@Produces
		@ApplicationScoped
		@Default
		public EmbeddedCacheManager defaultEmbeddedCacheConfiguration() { ... }
		
Then we put this class somewhere in our classpath (or even better in our source) and add the configuration code from above in it. 

1. Add a Config class in package org.jboss.infinispan.demo that looks loke this:

		package org.jboss.infinispan.demo;
		
		import javax.annotation.PreDestroy;
		import javax.enterprise.context.ApplicationScoped;
		import javax.enterprise.inject.Default;
		import javax.enterprise.inject.Produces;
		
		import org.infinispan.configuration.cache.Configuration;
		import org.infinispan.configuration.cache.ConfigurationBuilder;
		import org.infinispan.configuration.global.GlobalConfiguration;
		import org.infinispan.configuration.global.GlobalConfigurationBuilder;
		import org.infinispan.eviction.EvictionStrategy;
		import org.infinispan.manager.DefaultCacheManager;
		import org.infinispan.manager.EmbeddedCacheManager;
		
		public class Config {
		
			private EmbeddedCacheManager manager;
		
			@Produces
			@ApplicationScoped
			@Default
			public EmbeddedCacheManager defaultEmbeddedCacheConfiguration() {
				if (manager == null) {
					GlobalConfiguration glob = new GlobalConfigurationBuilder()
							.globalJmxStatistics().allowDuplicateDomains(true).enable() // This
							// method enables the jmx statistics of the global
							// configuration and allows for duplicate JMX domains
							.build();
					Configuration loc = new ConfigurationBuilder().jmxStatistics()
							.enable() // Enable JMX statistics
							.eviction().strategy(EvictionStrategy.NONE) // Do not evic objects
							.build();
					manager = new DefaultCacheManager(glob, loc, true);
				}
				return manager;
			}
		
			@PreDestroy
			public void cleanUp() {
				manager.stop();
				manager = null;
			}
		}
		
1. Soon we are ready to deploy the application, but first we should add the Config class to Arquillian by uncomment the following line in TaskServiceTest.java

		.addClass(Config.class)
		
1. Run the JUnit test by right clicking TaskServiceTest.java and select Run As ... -> JUnit Test

1. If everything is green we are ready to deploy the application with the following command in a terminal

		$ mvn package jboss-as:deploy
		
1. Test the application by opening a browser window to [http://localhost:8080/todo](http://localhost:8080/todo)

1. Congratulations you are done with lab1.
