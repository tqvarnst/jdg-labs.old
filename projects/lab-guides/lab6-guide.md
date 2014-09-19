# JDG + EAP Lab 6 Guide
This explains the steps for lab 6, either follow them step-by-step or if you 
feel adventurous try to accomplish goals without the help of the step-by-step guide.

## Background 
When the security department review the new solution with client server mode, they expressed worries about the fact that clients are not authenticated. To go live with Client server mode we need to implement authentification using simple username password. 

## Use-case
Increase the security by addning authentification

## These are the main steps of lab 5

1. Setup security for the JDG lab
2. Implement a simple call back login handler, used by the HR client.

### Setup the lab environment
To assist with setting up the lab environment we have provided a shell script that does this. 

**Note:** _If you previously setup up lab 5 using this script there is no need to do this for lab 6_
  
1. Run the shell script by standing in the jdg lab root directory (~/jdg-labs) execute a command like this

		$ sh init-lab.sh --lab=6

    **Note:** _If the EAP and JDG servers are running stop them_
	
## Step-by-Step
1. Open `target/jboss-datagrid-6.3.0-server/standalone/configuration/standalone.xml` using vi or text editor of choice
1. Add authentification to hotrod endpoint in subsystem `urn:infinispan:server:endpoint:...` like this:

		<subsystem xmlns="urn:infinispan:server:endpoint:6.1">
            **<hotrod-connector socket-binding="hotrod" cache-container="local">
				<topology-state-transfer lazy-retrieval="false" lock-timeout="1000" replication-timeout="5000"/>
				<authentication security-realm="ApplicationRealm">
					<sasl server-name="tasks" mechanisms="DIGEST-MD5" qop="auth">
						<policy>
							<no-anonymous value="true"/>
						</policy>
						<property name="com.sun.security.sasl.digest.utf8">true</property>
					</sasl>
				</authentication>
			</hotrod-connector>**
            <memcached-connector socket-binding="memcached" cache-container="local"/>
            <rest-connector virtual-server="default-host" cache-container="local" security-domain="other" auth-method="BASIC"/>
        </subsystem>
	
1. Add security to the `urn:infinispan:server:core:...` subsystem, like this: 

		<subsystem xmlns="urn:infinispan:server:core:6.1" default-cache-container="local">
	        <cache-container name="local" default-cache="default" statistics="true">
	        	**<security>
				  <authorization>
					  <identity-role-mapper/>
					  <role name="taskusers" permissions="READ WRITE BULK_READ"/>
				  </authorization>
				</security>**
			...
			</cache-container>
		</subsystem>
				
1. Later in the same subsystem configuraiton add the following:

		<local-cache name="tasks" start="EAGER">
			<locking acquire-timeout="30000" concurrency-level="1000" striping="false"/>
			<transaction mode="NONE"/>
			<security>
			  <authorization roles="taskusers"/>
		  </security>
		</local-cache>

1. Start the servers runing the following commands from different console windows.
	EAP Server:	
		
		$ ./target/jboss-eap-6.3/bin/standalone.sh
		
	JDG Server:
		
		$ ./target/jboss-datagrid-6.3.0-server/bin/standalone.sh -Djboss.socket.binding.port-offset=100
		

1. Before we rewrite the `TaskService` we need to configure the HotRod client using CDI to produce a `RemoteCache` object. Open `Config` class and add the following to it:

		@Produces
		public RemoteCache<Long, Task> getRemoteCache() {
			ConfigurationBuilder builder = new ConfigurationBuilder();
			builder.addServer().host("localhost").port(11322);
			return new RemoteCacheManager(builder.build(), true).getCache("default");
		}

	**Note:** We are reusing the default cache in this lab, in lab 6 we will configure our own cache instead.
	
1. Open the `TaskSerivce` class
1. Inject a `RemoteCache` object like this:

		@Inject
		RemoteCache<Long, Task> cache;
		
1. Implement the `findAll()` method like this:

		public Collection<Task> findAll() {
			return cache.getBulk().values();
		}
		
1. Implement the `insert(Task)` method like this:

		public void insert(Task task) {
			if(task.getCreatedOn()==null) {
				task.setCreatedOn(new Date());
			}
			task.setId(System.nanoTime());
			cache.putIfAbsent(task.getId(), task);
		}		

1. Implement the `update(Task)` method like this:

		public void update(Task task) {
			cache.replace(task.getId(), task);
		}

1. Implement the `delete(Long)` method like this:

		public void delete(Long id) {
			cache.remove(id);
		}

1. Save the `TaskServer.java` file
1. Open `TaskServiceTest.java` and uncomment the the `File[] jars = ....` and `.addAsLibraries(...)`
1. Run the JUnit test and verify that everything works.
1. Deploy the application using the following command from lab7 dir
		
		$ mvn clean package jboss-as:deploy
		
10. Congratulations you are done with lab 5.





