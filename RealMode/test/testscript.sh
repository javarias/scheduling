#! /usr/bin/ksh

#javac -classpath $ACSROOT/lib/xmlstore.jar:$ACSROOT/lib/xmljbind.jar:$ACSROOT/lib/systementities.jar:$ACSROOT/lib/jcont.jar:$ACSROOT/lib/acsjlog.jar:$ACSROOT/lib/endorsed/xercesImpl.jar:$ACSROOT/lib/micarch.jar:$INTROOT/lib/ObsPrepEntities.jar:$ACSROOT/lib/xmlentity.jar alma/scheduling/test/SchedulingTestClient.java

#TESTPATH=`pwd`
TESTPATH=`pwd`/../lib/schedulingTest.jar


java -classpath $ACSROOT/lib/xmlstore.jar:$ACSROOT/lib/xmljbind.jar:$ACSROOT/lib/systementities.jar:$ACSROOT/lib/jcont.jar:$ACSROOT/lib/acsjlog.jar:$ACSROOT/lib/endorsed/xercesImpl.jar:$ACSROOT/lib/micarch.jar:$INTROOT/lib/ObsPrepEntities.jar:$ACSROOT/lib/xmlentity.jar:/alma/ACS-2.0/Orbacus/local/lib/OB.jar:$ACSROOT/lib/maci.jar:$TESTPATH alma.scheduling.test.SchedulingTestClient
