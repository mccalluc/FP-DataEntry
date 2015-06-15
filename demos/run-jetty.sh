#!/bin/bash

# This script is a self-contained demo of the FilteredPush Data Entry Plugin.
# It checks out FP-DataEntry into the current working directory, builds it,
# loads in data about the 50 state quarters, and starts the servers.
#
# For more information about FP-DataEntry, visit
# http://sourceforge.net/p/filteredpush/svn/HEAD/tree/trunk/FP-DataEntry/

DEMOS=`dirname $0`
. $DEMOS/config-shell.sh
. $DEMOS/config-variables.sh
. $FILES_SCRIPT

if [ ! $REUSE ]
then
	mvn clean package assembly:single -Dmaven.test.skip=true -f $CHECKOUT/pom.xml
fi

java -cp $JAR $FP.backend.solr.SolrInstaller $CONFIG
java -cp $JAR $FP.backend.solr.SolrIndexer $CONFIG
java -cp $JAR $FP.BothEndsHandler $CONFIG &

# Or just
#	java -jar $JAR $CONFIG

# For production, instead of watching the log scroll by, you probably want something like:
#	nohup java -cp $JAR $FP.BothEndsHandler   $CONFIG < /dev/null > $WORKSPACE/handler.log 2>&1 &

set +o xtrace
echo
echo "FP-DataEntry should be up and running on http://localhost:$PORT."
