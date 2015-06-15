Right now, there is an svn:ignore for web.xml in this directory: 
The tomcat build script will drop a web.xml here before making the war file,
but that web.xml shouldn't be checked in because it is contains a reference 
to the location of your own config.xml.

Basically, this is ugly all around, and I would like to find a better solution.