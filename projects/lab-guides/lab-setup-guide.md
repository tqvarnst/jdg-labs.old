# Lab Setup Guide

Follow this guide to setup the lab environment for JDG labs

## Prerequisits

* Host operating system should be Linux or a BSD based os like Mac OS X.
  * Alternative one of the above specified OS could be running in a virtual machine with a Desktop Manager
* The following software should be installed prior to the lab
  * OpenJDK or Oracle JDG using 1.6 or 1.7
  * Apache Maven (installed and on the path)
  * JBoss Developer Studion 7.X
* Download the following JBoss software as zip files (used later in the labs)
  * `jboss-datagrid-6.3.0-eap-modules-hotrod-java-client.zip`
  * `jboss-datagrid-6.3.0-eap-modules-library.zip`
  * `jboss-datagrid-6.3.0-maven-repository.zip`
  * `jboss-datagrid-6.3.0-quickstarts.zip`
  * `jboss-datagrid-6.3.0-server.zip`
  * `jboss-eap-6.2.4-full-maven-repository.zip`
  * `jboss-eap-6.3.0-maven-repository.zip`
  * `jboss-eap-6.3.0-quickstarts.zip`
  * `jboss-eap-6.3.0.zip`
* Download jdg-labs.zip (todo: provide link)

## Setup Development environment
1. Unzip jdg-labs.zip in a suiteable directory (for example $HOME)

  `$ unzip jdg-labs.zip -d $HOME`

1. Change directory into the lab root dir

  `$ cd $HOME/jdg-workshops`
  
1. Setup the development environment with the provided script

  `$ sh init-dev.sh`
  
2. Copy generated settings.xml to $HOME/.m2/ (don't forgett to backup any existins settings.xml)

  `$ cp target/settings.xml ~/.m2/`
  
3. Start JBoss Developer Studio
3. Turn off XML validation
4. Import the projects
4. 
 


4. Done