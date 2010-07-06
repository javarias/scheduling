#! /bin/bash

TARGET='APRC';

mkdir -p $TARGET/lib/castor-1.3.1;
mkdir -p $TARGET/lib/spring-2.5.5;
mkdir -p $TARGET/lib/alma-7.1;

echo "Checking for Castor library..."
if [ ! -f $ACSROOT/lib/castor.jar ] ; then
	echo "   * Not present in ACS distribution, please correct this error.";
fi
cp $ACSROOT/lib/castor.jar $TARGET/lib/castor-1.3.1/castor.jar;

echo "Checking for Xerces-J library..."
#if [ ! -f Xerces-J-bin.2.9.1.tar.gz ] ; then
#	echo "   * Not present, downloading...";
#	wget http://apache.freeby.pctools.cl/xerces/j/Xerces-J-bin.2.9.1.tar.gz;
#	tar xfz Xerces-J-bin.2.9.1.tar.gz;
#fi
#cp xerces-2_9_1/xercesImpl.jar $TARGET/lib/castor-1.3.1;
if [ ! -f $ACSROOT/lib/endorsed/xercesImpl.jar ] ; then
        echo "   * Not present in ACS distribution, please correct this error.";
fi
cp $ACSROOT/lib/endorsed/xercesImpl.jar $TARGET/lib/castor-1.3.1/xercesImpl.jar;


echo "Checking for Spring libraries..."
if [ ! -f spring-framework-2.5.5-with-dependencies.zip ] ; then
	echo "   * Not present, downloading...";
	wget wget http://s3.amazonaws.com/dist.springframework.org/release/SPR/spring-framework-2.5.5-with-dependencies.zip
	unzip spring-framework-2.5.5-with-dependencies.zip;
fi
cp -r spring-framework-2.5.5/lib/* spring-framework-2.5.5/dist $TARGET/lib/spring-2.5.5/;
cp $ACSROOT/lib/hsqldb.jar $TARGET/lib/spring-2.5.5/hsqldb/hsqldb.jar

echo "Checking for SFL4J libraries..."
if [ ! -f slf4j-1.5.2.zip ] ; then
	echo "   * Not present, downloading...";
	wget http://www.slf4j.org/dist/slf4j-1.5.2.zip;
	unzip slf4j-1.5.2.zip;
fi
#cp slf4j-1.5.2/slf4j-simple-1.5.2.jar $TARGET/lib/alma-7.1/;
#rm -rf $TARGET/lib/spring-2.5.5/slf4j/slf4j-log4j12-1.5.0.jar

echo "Getting jars from INTROOT..."
cp $INTROOT/lib/SchedulingPSM.jar $INTROOT/lib/SchedulingPSMCli.jar $INTROOT/lib/SchedulingCommon.jar $INTROOT/lib/SchedulingDSA.jar $TARGET/lib/alma-7.1/;

echo "Adding scripts and directories structures...";
cp -r create_distribution_files/getJarsFromIntroot.sh $TARGET/lib/alma-7.1/;
cp -r create_distribution_files/bin create_distribution_files/config create_distribution_files/OO $TARGET/;
mkdir $TARGET/doc;
chmod a+x $TARGET/bin/*;
chown -R $USER:$GROUP $TARGET;
