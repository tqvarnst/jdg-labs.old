# Lab Guide 1
This explains the steps for lab 1, either follow them step-by-step or if you feel adventurous read the overview and try to accomplish goals without the help of the step-by-step

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
		
2. Change the implementation of the findAll method to look like this:

		public List<Task> findAll() {
			return new ArrayList<Task>(cache.values());
		}
		
3. Change the create method to look like this:

		public void create(Task task) {
			// TODO: Replace the call to cache.size() to improve performance
			Long id = new Long(cache.size() + 1);
			task.setId(id);
			task.setCreatedOn(new Date());
			cache.put(id, task);
		}

4.	Add the implementation of the update method as shown below:

		public void update(Task task) {
			cache.replace(task.getId(), task);
		}

5. We also need to stop the cache manager when we are done with it
		
		@PreDestroy
		public void destory() {
			if (cache != null && cache.getCacheManager()!=null) {
				cache.getCacheManager().stop();
			}
		}

	CacheManager has a definied life cycle that we need to honour. When we inject a instance of the Cache the producer (which is provided by infinispan-cdi package) will create an instance of CacheManager and start it. We will therefor also make sure that we stop it when we are ready with it.

	

         
        
		
