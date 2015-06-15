# Defaults:

BLURB="
	<p>
		This is just a tiny, self-contained demo of the FilteredPush Data Entry Plugin.
	</p>
	<p>
		Sometimes individual instances of objects which have been "mass-produced"
		need to be cataloged. Books in a library are the obvious example, but
		plant specimens in a herbarium or coins in a museum are similar.
		There may be a source of bulk records, but you can't just load
		them into your database: At the very least, you need to link 
		the generic records to your particular instances;
		You might also want to tweak the bulk records to better describe 
		your particular instances. FP-DataEntry can make this kind of work easier.
	</p>
"
PORT='8888'
CHECKOUT=`readlink -f $DEMOS/..`
OPTIND=1
REUSE=''
NAME='fp-demo'
FILES_SCRIPT=$DEMOS/config-files-quarters.sh
while getopts "f:n:p:r" OPT
do
	case $OPT in
		f) FILES_SCRIPT=$OPTARG ;;
		n) NAME=$OPTARG ;;
		p) PORT=$OPTARG ;;
		r) REUSE=true # ie, skip the maven build and use the existing jar/war
	esac
done
JAR=$CHECKOUT/target/FP-DataEntry-jetty-1.0-SNAPSHOT-jar-with-dependencies.jar
FP=org.filteredpush.dataentry
WORKSPACE=`readlink -m $CHECKOUT/../FP-DataEntry-workspace/$NAME`
SOLR_DIR=$WORKSPACE/solr
INPUT=$WORKSPACE/input.txt
CONFIG=$WORKSPACE/config.xml

if [ -d $WORKSPACE ]
then
	read -p "Config directory '$WORKSPACE' already exists. Ok to delete and re-create? [Y/N] " OK_TO_DELETE
	if [ $OK_TO_DELETE == 'Y' -o $OK_TO_DELETE == 'y' ]
	then
		rm -rf $WORKSPACE || sudo rm -rf $WORKSPACE # If created by tomcat script, it will be chowned. 
	fi
fi

mkdir --parents $WORKSPACE
