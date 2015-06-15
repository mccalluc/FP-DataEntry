#!/bin/bash

# This script deploys all the demos mentioned in the root README.
# PLEASE don't waste your time running this frequently:
#
# Tomcat deployments are slower than Jetty because the config.xml location is baked into the war,
# so a full rebuild is necessary with every config change.
# (With Jetty, you can use '-r' to reuse existing jars.)
# And we need to build both a jar and war for each deployment to Tomcat.
# (We need the SolrInstaller and SolrIndexer main methods, and you can't
# get at them with just a war file.)

DEMOS=`dirname $0`
bash $DEMOS/run-tomcat.sh -f $DEMOS/config-files-openlibrary.sh      -n fp-openlibrary-demo && \
bash $DEMOS/run-tomcat.sh -f $DEMOS/config-files-koha.sh             -n fp-koha-demo && \
bash $DEMOS/run-tomcat.sh -f $DEMOS/config-files-specify7.sh         -n fp-specify7-demo && \
bash $DEMOS/run-tomcat.sh -f $DEMOS/config-files-quarters.sh         -n fp-quarters-demo && \
bash $DEMOS/run-tomcat.sh -f $DEMOS/config-files-muppets.sh          -n fp-muppets-demo && \
bash $DEMOS/run-tomcat.sh -f $DEMOS/config-files-lichens.sh          -n fp-lichens-demo && \
bash $DEMOS/run-tomcat.sh -f $DEMOS/config-files-collectionspace.sh  -n fp-collectionspace-demo && \
bash $DEMOS/run-tomcat.sh -f $DEMOS/config-files-archivesspace.sh    -n fp-archivesspace-demo && \
bash $DEMOS/run-tomcat.sh -f $DEMOS/config-files-omeka.sh            -n fp-omeka-demo && \
bash $DEMOS/run-tomcat.sh -f $DEMOS/config-files-gbifapi.sh          -n fp-gbifapi-demo
