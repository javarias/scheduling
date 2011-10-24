#! /bin/bash

#*******************************************************************************
# ALMA - Atacama Large Millimeter Array
# Copyright (c) AUI - Associated Universities Inc., 2011
# (in the framework of the ALMA collaboration).
# All rights reserved.
# 
# This library is free software; you can redistribute it and/or
# modify it under the terms of the GNU Lesser General Public
# License as published by the Free Software Foundation; either
# version 2.1 of the License, or (at your option) any later version.
# 
# This library is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
# Lesser General Public License for more details.
# 
# You should have received a copy of the GNU Lesser General Public
# License along with this library; if not, write to the Free Software
# Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307  USA
#*******************************************************************************
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
