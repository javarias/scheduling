#! /bin/bash

tmp=tmp_obsprep

mkdir -p ${tmp}
for i in `ls ${tmp}/*.out ${tmp}/*.err 2>>/dev/null`; do
	echo "" > $i 1>>/dev/null
done

# Start hsqldb server using file-based setup
echo "starting hsqldb server for testing..."
acsStartJava org.hsqldb.Server -database.0 file:hsqldb/statearchive -dbname.0 statearchive >& ./${tmp}/stateArchiveStart.log &
sleep 5
echo "creating tables for hsqldb..."
acsStartJava alma.lifecycle.clients.CreateEmptyDatabase Server statearchive localhost sql/hsqldb-ddl.sql >& ./${tmp}/stateArchiveClean.log

export ACS_CDB=`pwd`/config/forOBSPREP

echo "Starting ACS"
acsStart -noloadifr >& ./${tmp}/acsStart.log

echo "Starting java container"
export JAVA_OPTIONS="-Darchive.configFile=archiveConfig.obsprep.properties"
acsStartContainer -java frodoContainer &> ${tmp}/frodoContainer.log & 
sleep 5
unset JAVA_OPTIONS

echo "Starting archive..."
archive start 1>> ${tmp}/archiveStart.out 2>> ${tmp}/archiveStart.err

echo "Ready to add projects"
