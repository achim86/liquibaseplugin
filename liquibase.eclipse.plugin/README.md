# Liquibase Eclipse Plugin

This plugin provides an eclipse view which simplifies your database management via Liquibase.

Inlcudes:
	- Attaching Liquibase to your eclipse projects with a highly covered directory structure
	- Running updates with progressbar via simple button click
	- Running rollback to last tag via simple button click
	
Doesn't Include:
	- Generation of change sets  

# Running from your IDE
1. Go to http://www.oracle.com/technetwork/database/enterprise-edition/jdbc-112010-090769.html download ojdbc*.jar
2. Go to eclipse and create a plugin of ojdbc*.jar (follow the steps below)
	-> file
	-> new
	-> other...
	-> plug-in development
	-> plug-in from Existing JAR archives
	-> add external
	-> select downloaded ojdbc*.jar
	![fileurls](http://img687.imageshack.us/img687/5705/ojdbc.png)
3. Add dependencies to org.oracle.osgi on liquibase.eclipse.plugin and liquibase.plugin
	![fileurls](http://img411.imageshack.us/img411/5573/dependencies.png)