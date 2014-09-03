# JDG + EAP Lab 3 Guide
This explains the steps for lab 2, either follow them step-by-step or if you 
feel adventurous read the overview and try to accomplish goals without the help 
of the step-by-step

## Background 
In Lab 2 we implemented querying by providing search mapping as to how to index
objects via configuration. The search mapping for Lab2 was very simple, but what
if your data model was much more complex with links between objects and 
inheritance. Then the configuration will become more and more complex. Keeping 
the search mapping configuration and data model in sync will become harder and 
harder over time as your data models grows. 

The Best practice recommendation for situations like this is to keep the 
mapping close to the model. So similar to JPA, Hibernate Search (which is the 
base for JDG Querying) supports annotation as meta data that describes the 
search mapping. 

This does require us to have access to the data model source code and that we 
are allowed to update it. 

## Objectives
You task in Lab 3 is move the search mapping from the configuration object and
instead provide this as annotations to the data model object.

These are the main tasks of lab 3

1. Remove the search mapping from the `org.jboss.infinispan.demo.Config`.
2. Update the `org.jboss.infinispan.demo.model.Task` model with Search mapping 
annotation.

## Step-by-Step

1. Open `src/main/java/org/jboss/infinispan/demo/Config.java` 
2. Uncomment the search mapping entries like this: 
		
		GlobalConfiguration glob = new GlobalConfigurationBuilder()
					.globalJmxStatistics().allowDuplicateDomains(true).enable() // This
					// method enables the jmx statistics of the global
					// configuration and allows for duplicate JMX domains
					.build();
			
		//			SearchMapping mapping = new SearchMapping();
		//			mapping.entity(Task.class).indexed().providedId()
		//			      .property("title", ElementType.METHOD).field();
			 
					Properties properties = new Properties();
		//			properties.put(org.hibernate.search.Environment.MODEL_MAPPING, mapping);
					properties.put("default.directory_provider", "ram");
					
3. Open `src/main/java/org/jboss/infinispan/demo/model/Task.java`
4. Add `@org.hibernate.search.annotations.Indexed` as a class modifier
5. Add `@org.hibernate.search.annotations.Field(store = org.hibernate.search.annotations.Store.YES)
as the modifier to the `title` field.






