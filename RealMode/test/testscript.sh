#! /usr/bin/ksh

# Get required paths
ACS_CDB=`pwd`

TESTPATH=`pwd`/../lib/schedulingTest.jar

# Start acs processes
echo " Starting ORB Services"
acsStartORBSRVC &
sleep 50

echo " Starting Manager"
maciManager &
sleep 15

echo " Starting hugoActivator"
acsStartContainer hugoActivator &
sleep 10

# Run test
echo " Running test"

java -classpath $ACSROOT/lib/xmlstore.jar:$ACSROOT/lib/xmljbind.jar:$ACSROOT/lib/systementities.jar:$ACSROOT/lib/jcont.jar:$ACSROOT/lib/acsjlog.jar:$ACSROOT/lib/endorsed/xercesImpl.jar:$ACSROOT/lib/micarch.jar:$INTROOT/lib/ObsPrepEntities.jar:$ACSROOT/lib/xmlentity.jar:/alma/ACS-2.0/Orbacus/local/lib/OB.jar:$ACSROOT/lib/maci.jar:$TESTPATH alma.scheduling.test.SchedulingTestClient

# Shutdown acs and java processes
maciActivatorShutdown hugoActivator 
maciManagerShutdown 
acsStopORBSRVC 

