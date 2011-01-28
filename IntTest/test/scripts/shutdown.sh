#! /bin/bash

tmp=tmp_obsprep

# Shutdown archive
archive stop 1>> ${tmp}/archiveStop.out 2>> ${tmp}/archiveStop.err

# Shutdown ACS
acsStop

# Shutdown the state system archive
acsStartJava alma.lifecycle.clients.ShutdownDbServer Server statearchive localhost >& ./${tmp}/stateArchiveStop.log
