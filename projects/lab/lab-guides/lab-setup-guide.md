# Lab Setup Guide
This guide explains how to setup the project

### Install and build the mock project

1. After unpackaging the zip file that contains the labs, do the following

1. Copy the installation files to the `installs` directory, your installs directory should contain the following files: 
	* jboss-datagrid-6.3.0-eap-modules-hotrod-java-client.zip
	* jboss-datagrid-6.3.0-eap-modules-library.zip
	* jboss-datagrid-6.3.0-maven-repository.zip
	* jboss-datagrid-6.3.0-quickstarts.zip
	* jboss-datagrid-6.3.0-server.zip
	* jboss-eap-6.3.0-maven-repository.zip
	* jboss-eap-6.3.0-quickstarts.zip
	* jboss-eap-6.3.0.zip
       
     
1. Run the `init-dev.sh` and `init.sh` to setup dev and server environment (if running locally)

        $ ./init-dev.sh
        ...
        ...
        ...
        $ ./init.sh

	The `init-dev.sh` script will setup local maven repositories and generate a settings.xml for you. You can optinaly choose to replace you existing (if any) settings.xml with this one. Otherwise you will have specify the settings file to use everytime you run maven.
	
	The `init.sh` script will setup JBoss EAP with JDG modules and JDG standalone servers. If you want to run you servers on a virtual machine you can skip this for you dev host and instead repeat step 1 to 3 in your server host
        
1. Start the JBoss EAP (on your server host)

		$ target/jboss-eap-6.3/bin/standalone.sh
		
1. We are now done with the installation and deployment of the Mock project. If you didn't understand each step, don't worry from this point it will only get easier and much more about coding Java. :-) 


 
