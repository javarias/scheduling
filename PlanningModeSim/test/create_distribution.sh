#! /bin/bash

TARGET='../APRC';
TMP='tmp'
# TODO: Create a temporary directory to save all download and decompressed directories.

mkdir -p $TMP;
cd $TMP;

mkdir -p $TARGET/lib/castor-1.3.1;
mkdir -p $TARGET/lib/spring-2.5.5;
mkdir -p $TARGET/lib/alma-7.1;

echo "Checking for JasperReports library..."
if [ ! -f jasperreports-3.7.4.jar ] ; then
        echo "   * Not present, downloading...";
        wget http://sourceforge.net/projects/jasperreports/files/jasperreports/JasperReports%203.7.4/jasperreports-3.7.4.jar/download
fi
if [ ! -f jasperreports-fonts-3.7.4.jar ]; then
        echo "   * Not present, downloading...";
        wget http://sourceforge.net/projects/jasperreports/files/jasperreports/JasperReports%203.7.4/jasperreports-fonts-3.7.4.jar/download
fi
cp jasperreports-3.7.4.jar $TARGET/lib/alma-7.1/;
cp jasperreports-fonts-3.7.4.jar $TARGET/lib/alma-7.1/;

echo "Checking for JFreeChart library..."
if [ ! -f jfreechart-1.0.13.tar.gz ] ; then
        echo "   * Not present, downloading...";
	wget http://sourceforge.net/projects/jfreechart/files/1.%20JFreeChart/1.0.13/jfreechart-1.0.13.tar.gz/download
fi
tar xfz jfreechart-1.0.13.tar.gz
cp jfreechart-1.0.13/lib/jfreechart-1.0.13-experimental.jar jfreechart-1.0.13/lib/jfreechart-1.0.13.jar jfreechart-1.0.13/lib/jfreechart-1.0.13-swt.jar jfreechart-1.0.13/lib/jcommon-1.0.16.jar $TARGET/lib/alma-7.1/;


echo "Checking for Castor library..."
if [ ! -f $ACSROOT/lib/castor.jar ] ; then
	echo "   * Not present in ACS distribution, please correct this error.";
	exit
fi
cp $ACSROOT/lib/castor.jar $TARGET/lib/castor-1.3.1/;
cp $ACSROOT/lib/c3p0-0.9.1.2.jar $TARGET/lib/alma-7.1/;
cp $ACSROOT/lib/systementities.jar $TARGET/lib/alma-7.1/;

echo "Checking for Xerces-J library..."
if [ ! -f $ACSROOT/lib/endorsed/xercesImpl.jar ] ; then
        echo "   * Not present in ACS distribution, please correct this error.";
	exit
fi
cp $ACSROOT/lib/endorsed/xercesImpl.jar $TARGET/lib/castor-1.3.1/xercesImpl.jar;


echo "Checking for Spring libraries..."
if [ ! -f spring-framework-2.5.5-with-dependencies.zip ] ; then
	echo "   * Not present, downloading...";
	wget http://s3.amazonaws.com/dist.springframework.org/release/SPR/spring-framework-2.5.5-with-dependencies.zip
fi
unzip -o spring-framework-2.5.5-with-dependencies.zip > /dev/null;
cp -r spring-framework-2.5.5/lib/* spring-framework-2.5.5/dist $TARGET/lib/spring-2.5.5/;
cp $ACSROOT/lib/hsqldb.jar $TARGET/lib/spring-2.5.5/hsqldb/hsqldb.jar

echo "Checking for SFL4J libraries..."
if [ ! -f slf4j-1.5.2.zip ] ; then
	echo "   * Not present, downloading...";
	wget http://www.slf4j.org/dist/slf4j-1.5.2.zip;
fi
unzip -o slf4j-1.5.2.zip > /dev/null;

echo "Checking for EHCache libraries..."
if [ ! -f ehcache-core-2.1.0-distribution.tar.gz ] ; then
        echo "   * Not present, downloading...";
        wget http://sourceforge.net/projects/ehcache/files/ehcache-core/ehcache-core-2.1.0/ehcache-core-2.1.0-distribution.tar.gz/download;
fi
tar xfz ehcache-core-2.1.0-distribution.tar.gz;
cp ehcache-core-2.1.0/ehcache-core-2.1.0.jar $TARGET/lib/alma-7.1/;


echo "Getting jars from INTROOT..."
cp $INTROOT/lib/SchedulingPSM.jar $INTROOT/lib/SchedulingPSMCli.jar $INTROOT/lib/SchedulingCommon.jar $INTROOT/lib/SchedulingDSA.jar $TARGET/lib/alma-7.1/;



echo "Adding scripts and directories structures...";
cp -r ../create_distribution_files/getJarsFromIntroot.sh $TARGET/lib/alma-7.1/;
cp -r ../create_distribution_files/bin ../create_distribution_files/config ../create_distribution_files/OO $TARGET/;
mkdir $TARGET/doc;
chmod a+x $TARGET/bin/*;
chown -R $USER:$GROUP $TARGET;
