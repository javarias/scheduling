#!/bin/bash
#this script assumes that ACS is properly configured and initialized

JAVA_OPTS='-Xmn788M -Xms1024M'

start()
{
	cd $HOME/APRC
	. ./bin/setup.sh
	
	echo -n "Starting java RMI Registry..."
#	CLASSPATH=`vltMakeJavaClasspath`:$CLASSPATH rmiregistry &
	rmiregistry &
	echo -e "\t done"

	echo -n "Starting HSQLDB Database server..."
	aprc start
	sleep 2
	echo -e "\t done"

	echo -n "Starting APRC RMI Server..."
	ACS_INSTANCE=0
	manager=stedev01.sco.alma.cl
	export MANAGER_REFERENCE=corbaloc::${manager}:3${ACS_INSTANCE}00/Manager
	MANAGER_REFERENCE=corbaloc::${manager}:3${ACS_INSTANCE}00/Manager acsStartJava -endorsed -Djava.security.policy=../server.policy -maxHeapSize 2g  alma.scheduling.psm.cli.PsmCli remote &> $APRC_WORK_DIR/logs/remote.log &
	sleep 2
	echo -e "\t done"
#	MANAGER_REFERENCE=corbaloc::${manager}:3${ACS_INSTANCE}00/Manager tomcat start
	LANG=en_US tomcat start
}

stop()
{
	cd $HOME/APRC
	. ./bin/setup.sh
	tomcat stop
	killall acsStartJava
	killall rmiregistry
	bsh $APRC_HOME/bin/shutdownSQL.bsh
}

case "$1" in
	start)
		start
		;;
	stop)
		stop
		;;
esac
