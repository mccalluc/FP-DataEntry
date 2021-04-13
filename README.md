*This is a fork of a project I did at the Harvard Herbaria. It is,
unfortunately, hosted by SourceForge, and I'd prefer not to link to
them any more because of their business practices.*

(For a really quick introduction, glance at the [screenshots](http://wiki.filteredpush.org/wiki/FP-DataEntry).)

# Introduction

The FP-DataEntry plugin speeds up data entry by helping you find records for 
items like those in your collection, and by copying those records to your own 
data entry web application, saving you the trouble of copy-and-pasting or 
retyping. The idea is analogous  to copy-cataloging in the library world.

If there are multiple matches, rather than displaying them all, one after 
another, the software constructs a composite record. If all the matches agree 
for a particular field, the value is just displayed. If there are differences 
for some particular field, a select menu is displayed instead. This way you can 
spend your time on finding the best data for your collection, rather than 
trying to spot the differences between nearly identical records.

The software consists of a server which responds to queries about a particular 
set of records, and client-side Javascript which connects your existing 
collection management web application to the FP-DataEntry server.

# Requirements

To build: Java 6 or higher and Maven are required.

To deploy with Jetty: One free port is necessary.

To deploy with Tomcat: Should work with Tomcat7.

On the browser: JavaScript must be enabled, and CORS and HTML5 Web Messaging 
are required. (This is the case for current versions of every major browser.)
(TODO: That said, very little browser testing has been done except for Firefox
on Linux.)

For integration into existing applications: The plugin can be accessed via
a bookmarklet, but if you can modify the source of your existing application,
the result will probably be easier to use.

- A JavaScript file needs to be loaded, and a button needs to be added which
will call a function from that file.
- A div may be added to the page to control the placement on the injected
iframe.


# Build and Run

No matter which way you run the software, essentially the same thing is
happening behind the scenes: The `main()` methods of each of these classes
need to be run in turn:

	SolrInstaller
	SolrIndexer
	BothEndsHandler

Each is passed a single argument: the path to a configuration file. For each
of the demos linked above, the configuration can be inspected at `/config.xml`:
For example, [fp-quarters-demo/config.xml](http://firuta.huh.harvard.edu:8080/fp-quarters-demo/config.xml). To see all the possible
options in one place, the [DTD](FP-DataEntry/src/main/resources/configuration.dtd) is also useful.


## Running tests

Tests can be run from the command-line with maven:

	$ mvn clean test

Or you can also check the test coverage:

	$ mvn clean emma:emma


## Running in the IDE or command line

Import the project, and then run the `main()` method of `org.filteredpush.dataentry.Main`.
Equivalently, from the command line:

	$ svn checkout svn://svn.code.sf.net/p/filteredpush/svn/trunk/FP-DataEntry
	$ cd FP-DataEntry
	$ mvn clean package assembly:single -Dmaven.test.skip=true
	$ java -jar target/FP-DataEntry-jetty-1.0-SNAPSHOT-jar-with-dependencies.jar

(Optionally, a configuration file can be given as an argument when running the jar.)

The demo will be available at http://localhost:8888


## Running with Jetty

For more control, you'll probably use at least a basic wrapper script:

	$ svn checkout svn://svn.code.sf.net/p/filteredpush/svn/trunk/FP-DataEntry
	$ bash FP-DataEntry/demos/run-jetty.sh

Then visit http://localhost:8888 to see it in action. (If 8888 is already in
use you can use the `-p` (port) parameter.) The script writes out the
necessary configuration and data files, runs maven, and starts the server.


## Running with Tomcat

If you don't have a free port, or you'd like to install it alongside other
applications, the process is a little more complicated, but this script
outlines how a Tomcat deployment could work. You would certainly want to tweak
it for your needs:

	$ svn checkout svn://svn.code.sf.net/p/filteredpush/svn/trunk/FP-DataEntry
	$ bash FP-DataEntry/demos/run-tomcat.sh

(The script needs to move the war file to `$CATALINA_BASE`, and you may be 
prompted for your password unless you have `sudo`-ed recently.)


## Security Note: Mixed Content

For [good reasons](https://developer.mozilla.org/en-US/docs/Security/MixedContent), any page with an "https:" URL is blocked from requesting plain
"http:" content. The plugin embeds the protocol of the server it was created 
on: If the server was HTTP, then the plugin is HTTP; if it was HTTPS, then
HTTPS. If you find yourself trying to use the plugin on an HTTPS site and 
encounter a warning about "mixed content", there are two options: Disable the 
security check in your browser, or get the server (Tomcat or Jetty) working 
with HTTPS. That is outside the scope of this document.


# Architecture

As you can see above, the server actually has two components.

* The front end provides the static Javascript, CSS, and HTML which 
comprise the plugin. 
* The back end provides a JSON API which queries the backing Solr index.

The work of the software is divided between index-time and query-time:

## Index-time

```
	      +-----------------+
	      |                 |
	      |   SolrIndexer   |
	      |                 |
	      +-----------------+
	      (1)|    ^     (3)|
	         v (2)|        v
	+--------------+   +--------------+
	|              |   |              |
	|     Data     |   |   Embedded   |
	|    Source    |   |     Solr     |
	|              |   |              |
	+--------------+   +--------------+
```

(1): Read data source 

(2): Get results. 

(3): Update Solr.

Note that (2) and (3) are actually done concurrently, rather than reading 
everything into  memory, and then writing it out. Currently, tab- or comma-
delimitted files and GBIF downloads are the only supported data sources. 
Others could be supported by implementing `GenericRecordsForIndexAndUpdate`.

## Query-time
```
	  +----------------+ (3)           +------------------+
	  |   Collection   |-------------->|   FP-DataEntry   |
	  |   Management   |          (10) |      plugin      |
	  |      HTML      |<--------------|      iframe      |
	  +----------------+               +------------------+
	     (1)|    ^                   (4)|    ^  (8)|    ^
	        v (2)|                      v (7)|     v (9)| 
	  +----------------+     +----------------+   +----------------+
	  |  FP-DataEntry  |     |  FP-DataEntry  |   |   Collection   |
	  |     plugin     |     |     plugin     |   |   Management   |
	  |   front end    |     |    back end    |   |   JSONP API    |
	  +----------------+     +----------------+   +----------------+
	                            (5)|    ^
	                               v (6)|
	                          +--------------+
	                          |              |
	                          |     Solr     |
	                          |              |
	                          +--------------+
```
	  
(1+2): Your collection management web application loads fp-data-entry-plugin.js 
from the FP-DataEntry front end server. 

(3): Your application defines a trigger which calls fp_data_entry_plugin(): 
This function creates a new iframe on your application's page. The iframe URL 
contains search terms.

(4): The JS in the iframe in turn queries the FP-DataEntry back end server... 

(5): which queries Solr (or a data source you provide by implementing 
GenericQueryEngine)... 

(6+7): and the results are returned to the iframe. 

(8+9): If so configured, a JSONP API on your local database can be queried to 
check for matches from a controlled vocabulary. 

(10): On each change in the iframe, the HTML5 Web Messaging API is used to 
communicate the current state back up to the collection management page. 
JS provided by the plugin listens for this message and updates the fields of 
collection management page.


# Internals

The index.html on any FP-DataEntry front end server provides a demonstration of 
how to configure and use the JS. Just view source, and there will be comments 
which describe how things work. If there is already a server running with the 
data you need, you're set.

If you just need a new source of data, you'll need to set your configuration 
file to point to the new data source, and change other parts of the schema to
fit your data.

Beyond that, you'll need to edit the code, and that gets more complicated.

Project organization follows maven conventions. The Java code is divided into 
these packages:

```
	org.filteredpush
	    .dataentry               Main class and utilities
	    .dataentry.enums         Enumeration classes
	    .dataentry.frontend      Supports the front-end
	    .dataentry.backend       Supports the back-end
	    .dataentry.backend.solr  Creating and reading Solr indexes
	    .dataentry.servlet       Wraps the Jetty Handler classes in Servlets
	    .dataentry.configuration Tools for reading xml config files,
	                             and interfaces which define the data
	                             needed by particular modules
```

## Front End

The front end server and the back end server could be started separately, as
is done in the tests, but in common use they are bundled under BothEndsHandler.
The front end server is trivial: it fills out and serves templated documents
from the resources directory. (The values to fill are set when the server 
starts.) The real complexity is in the Javascript. Note that there are two
distinct contexts:

1. Your existing web application will load `fp-data-entry-plugin.js`, which has 
no other dependencies, and which defines a single function: 
`fp_data_entry_plugin()`. Your application is responsible for calling this 
function when the user has filled in a search field. For more information, see 
the index.html on a running server.

2. That function creates an iframe, and into that iframe all the other JS files 
are loaded. The "`fp`" namespace is defined, and configuration data and 
functions are added to it.

One comment about `demo-extra.js`: This file demonstrates how a lookup against 
a  controlled vocabulary can be supported. If this is something you need, you 
will probably copy it to your own server and make edits. The file should not be 
referenced in  production use... but it is very useful for a new person trying
to use the plugin to support controlled vocabularies.

## Back End

The install / index / run sequence was described at the top, and the details of 
configuration are documented in the sample configuration file.

It's important to understand the `Term` and `Tuple` classes. These can be 
thought of as enumerations which are specified at run time. Each `Term` 
corresponds to a field of your existing data entry application. Because some 
fields only make sense together (Latitude/Longitude; Genus/Species), a `Tuple` 
is defined to bundle them together. Depending on your configuration, many 
`Tuple`s may each only contain a single `Term`. To handle this bundling, all 
data is actually stored in solr as JSON lists.

These bundled fields are represented in Java with `TupleSingleRecord`.

An added complication is that, depending on your data source, a single record 
may have more than one value for a field which your existing data entry 
application treats as single-valued. An example might be  "processed date" and 
"verbatim date": You might generally prefer the processed date, but if the 
source data was weird, you might prefer the verbatim date in a particular case. 
To handle this, in the solr schema we use `copyField` and indicate that the
target field is multi-valued, and in Java we use `TupleMultiRecord`.
