# Lab Guide 1
This explains the steps for lab 1, either follow them step-by-step or if you eel adventurous read the overview and try to accomplish goals without the help of the step-by-step

## Overview of Lab 1
In lab 1 we will create the backend storage for a mockup application written in CDI, REST and AngularJS. The mockup application is available [here](https://github.com/tqvarnst/jdg-modules-demo/tree/1.0-mockup). The main steps in lab1 is to:

1. Install and build the mock project
2. Add dependencies to the maven project and to the WAR file
3. Inject a local Cache into TaskService class and implement the logic to findAll, create, update. 


## Step-by-Step
The step-by-step guide is dived into 3 different sections matching the main steps in the overview.

The First step over is to verify that the application build and deploy before we do anything else.

### Install and build the mock project

1. Download a ZIP of the project directly from the site or by using the following command

        $  curl https://codeload.github.com/tqvarnst/jdg-modules-demo/zip/master -o lab1.zip

2. Unzip into a suitable directory and change dir to it
        
        $ unzip lab1.zip  -d ~/tmp
        $ cd ~/tmp
       
3. Rename the top directory and change dir to it
        
        $ mv jdg-modules-demo-1.0-mockup lab1
        $ cd lab1
     
4. Copy JBoss EAP 6.2 and JDG 6.3 EAP Modules into installs directory (In the commmand we assumet that the files are in the Download folder in your home directory, you may have to change that to the location where you have download this files)

		$ cp ~/Download/jboss-eap-6.2.0.zip installs/
		$ cp ~/Download/jboss-datagrid-6.3.0-eap-modules-library.zip installs/
		
5. Run the init script

        $ ./init.sh
        
6. Start the JBoss EAP

		$ target/jboss-eap-6.2/bin/standalone.sh
		
7. In another terminal change directory to the project, build and deploy it

        $ cd project/todo
        $ mvn package jboss-as:deploy
8. Verify in a browser that application deployed nice successfully by clicking [this](http://localhost:8080/todo) link. Also notice that if you try to add a task, it doesn't work and if you check a task as completed and reload the browser it shows up as not completed again. This is becuase the application doesn't store the content. 


9. We are now done with the installation and deployment of the Mock project. If you didn't understand each step, don't worry from this point it will only get easier and much more about coding Java. :-) 


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
		
	**Note:** We use a bom file to manage the versions of the dependencies, if you choose not to use the bom file, just specify the version in the dependecies. The version of the bom file is the version used for JDG 6.3 Beta and should be update as soon as JDG 6.3 is fully available.

2. One of the nice things with installing JDG as JBoss EAP modules is that JDG libraries are installed and maintained in the container, which means that we don't have to ship them as part of the WAR file. For example if we need to patch JDG we don't have to patch the application. We do however need to tell the cointainer (JBoss EAP) that our application depends on these modules. This can be done using JBoss specific deployment file called boss-deployment-structure.xml or via adding dependencies to the MANIFEST.MF file that get's genereated when we build the WAR or JAR. We are going to use the later alternative by telling maven to add these. In the ```<build>``` section of pom.xml there are a maven-war-plugin defined, in the ```<configuration>``` section after ```<failOnMissingWebXml>``` add the following:

        <archive>
			<manifestEntries>
				<Dependencies>org.infinispan.cdi:jdg-6.3 meta-inf, org.infinispan:jdg-6.3 services</Dependencies>
			</manifestEntries>
		</archive>

	The full maven-war-plugin ```<plugin>``` section should look like this:

		<plugin>
			<artifactId>maven-war-plugin</artifactId>
			<version>2.4</version>
			<configuration>
				<failOnMissingWebXml>false</failOnMissingWebXml>
				<archive>
					<manifestEntries>
						<Dependencies>org.infinispan.cdi:jdg-6.3 meta-inf, org.infinispan:jdg-6.3 services</Dependencies>
					</manifestEntries>
				</archive>
			</configuration>
		</plugin>

3. Run the build and deploy command again

		$ mvn package jboss-as:deploy
		
4. Make sure that the above command are succesfull and you are done with this section.

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

	

         
        
		
