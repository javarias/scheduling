#! /bin/bash

TARGET='../WebContent/WEB-INF/lib/';

mkdir -p $TARGET;

mkdir tmp;
cd tmp;
echo "Checking for Spring libraries..."
if [ ! -f spring-framework-2.5.5-with-dependencies.zip ] ; then
	echo "   * Not present, downloading...";
	wget wget http://s3.amazonaws.com/dist.springframework.org/release/SPR/spring-framework-2.5.5-with-dependencies.zip
fi
unzip -o spring-framework-2.5.5-with-dependencies.zip > /dev/null;
cp spring-framework-2.5.5/lib/antlr/antlr-2.7.6.jar $TARGET/;
cp spring-framework-2.5.5/lib/cglib/cglib-nodep-2.1_3.jar $TARGET/;
cp spring-framework-2.5.5/lib/jakarta-commons/commons-collections.jar $TARGET/;
cp spring-framework-2.5.5/lib/jakarta-commons/commons-fileupload.jar $TARGET/;
cp spring-framework-2.5.5/lib/jakarta-commons/commons-io.jar $TARGET/;
cp spring-framework-2.5.5/lib/dom4j/dom4j-1.6.1.jar $TARGET/;
cp spring-framework-2.5.5/lib/groovy/groovy-1.5.5.jar $TARGET/;
cp spring-framework-2.5.5/lib/hibernate/hibernate3.jar $TARGET/;
cp spring-framework-2.5.5/lib/hibernate/hibernate-annotations.jar $TARGET/;
cp spring-framework-2.5.5/lib/hibernate/hibernate-commons-annotations.jar $TARGET/;
cp spring-framework-2.5.5/lib/hibernate/hibernate-entitymanager.jar $TARGET/;
cp spring-framework-2.5.5/lib/jruby/jruby.jar $TARGET/;
cp spring-framework-2.5.5/lib/j2ee/jta.jar $TARGET/;
cp spring-framework-2.5.5/lib/log4j/log4j-1.2.15.jar $TARGET/;
cp spring-framework-2.5.5/dist/spring.jar $TARGET/;

echo "Copying HSQLDB libraries from ACS..."
cp $ACSROOT/lib/hsqldb.jar $TARGET

echo "Checking for SFL4J libraries..."
if [ ! -f slf4j-1.5.2.zip ] ; then
	echo "   * Not present, downloading...";
	wget http://www.slf4j.org/dist/slf4j-1.5.2.zip;
fi
unzip -o slf4j-1.5.2.zip > /dev/null;
cp slf4j-1.5.2/slf4j-api-1.5.2.jar $TARGET;
cp slf4j-1.5.2/slf4j-log4j12-1.5.2.jar $TARGET;

#echo "Checking for ZK libraries..."
#if [ ! -f zk-bin-5.0.3.tar.gz ] ; then
#	echo "   * Not present, downloading...";
#	wget https://sourceforge.net/projects/zk1/files/ZK/zk-5.0.3/zk-bin-5.0.3.tar.gz/download;
#fi
#tar xfz zk-bin-5.0.3.tar.gz;
#cp zk-bin-5.0.3/dist/lib/zcommon.jar $TARGET;
#cp zk-bin-5.0.3/dist/lib/zcommons-el.jar $TARGET;
#cp zk-bin-5.0.3/dist/lib/zhtml.jar $TARGET;
#cp zk-bin-5.0.3/dist/lib/zk.jar $TARGET;
#cp zk-bin-5.0.3/dist/lib/zkplus.jar $TARGET;
#cp zk-bin-5.0.3/dist/lib/zul.jar $TARGET;
#cp zk-bin-5.0.3/dist/lib/zweb.jar $TARGET;
#cp zk-bin-5.0.3/dist/lib/ext/bsh.jar $TARGET;
#cp zk-bin-5.0.3/dist/lib/ext/js.jar $TARGET;
#cp zk-bin-5.0.3/dist/lib/ext/jython.jar $TARGET;
#cp zk-bin-5.0.3/dist/lib/zkforge/fckez.jar $TARGET;

echo "Checking for ZK Enterprise libraries..."
if [ ! -f zkpe-bin-eval-5.0.3.zip ] ; then
        echo "   * Not present, downloading...";
        wget http://www.zkoss.org/zkdownload/downloadEvalFile?id=1682440901;
	mv downloadEvalFile\?id\=1682440901 zkpe-bin-eval-5.0.3.zip
fi
unzip -o zkpe-bin-eval-5.0.3.zip > /dev/null;
cp zkpe-bin-eval-5.0.3/dist/lib/zcommon.jar $TARGET;
cp zkpe-bin-eval-5.0.3/dist/lib/zcommons-el.jar $TARGET;
cp zkpe-bin-eval-5.0.3/dist/lib/zhtml.jar $TARGET;
cp zkpe-bin-eval-5.0.3/dist/lib/zkex.jar $TARGET;
cp zkpe-bin-eval-5.0.3/dist/lib/zk.jar $TARGET;
cp zkpe-bin-eval-5.0.3/dist/lib/zkplus.jar $TARGET;
cp zkpe-bin-eval-5.0.3/dist/lib/zml.jar $TARGET;
cp zkpe-bin-eval-5.0.3/dist/lib/zul.jar $TARGET;
cp zkpe-bin-eval-5.0.3/dist/lib/zweb.jar $TARGET;
cp zkpe-bin-eval-5.0.3/dist/lib/ext/bsh.jar $TARGET;
cp zkpe-bin-eval-5.0.3/dist/lib/ext/js.jar $TARGET;
cp zkpe-bin-eval-5.0.3/dist/lib/ext/jython.jar $TARGET;
cp zkpe-bin-eval-5.0.3/dist/lib/zkforge/fckez.jar $TARGET;

echo "Getting jars from INTROOT..."
cp $INTROOT/lib/SchedulingPSM.jar $INTROOT/lib/SchedulingPSMCli.jar $INTROOT/lib/SchedulingCommon.jar $INTROOT/lib/SchedulingDSA.jar $TARGET;

echo "Checking for EHCache libraries..."
if [ ! -f ehcache-core-2.1.0-distribution.tar.gz ] ; then
        echo "   * Not present, downloading...";
        wget http://sourceforge.net/projects/ehcache/files/ehcache-core/ehcache-core-2.1.0/ehcache-core-2.1.0-distribution.tar.gz/download;
fi
tar xfz ehcache-core-2.1.0-distribution.tar.gz;
cp ehcache-core-2.1.0/ehcache-core-2.1.0.jar $TARGET;

echo "Checking for JasperReports library..."
if [ ! -f jasperreports-3.7.0.jar ] ; then
        echo "   * Not present, downloading...";
	wget http://sourceforge.net/projects/jasperreports/files/jasperreports/JasperReports%203.7.0/jasperreports-3.7.0.jar/download
fi
if [ ! -f jasperreports-fonts-3.7.0.jar ]; then
        echo "   * Not present, downloading...";
	wget http://sourceforge.net/projects/jasperreports/files/jasperreports/JasperReports%203.7.0/jasperreports-fonts-3.7.0.jar/download
fi
cp jasperreports-3.7.0.jar $TARGET;
cp jasperreports-fonts-3.7.0.jar $TARGET;

echo "Checking for JFreeChart library..."
if [ ! -f jfreechart-1.0.13.tar.gz ] ; then
        echo "   * Not present, downloading...";
        wget http://sourceforge.net/projects/jfreechart/files/1.%20JFreeChart/1.0.13/jfreechart-1.0.13.tar.gz/download
fi
tar xfz jfreechart-1.0.13.tar.gz
cp jfreechart-1.0.13/lib/jfreechart-1.0.13-experimental.jar jfreechart-1.0.13/lib/jfreechart-1.0.13.jar jfreechart-1.0.13/lib/jfreechart-1.0.13-swt.jar jfreechart-1.0.13/lib/jcommon-1.0.16.jar $TARGET;

echo "Checking for Castor library..."
if [ ! -f $ACSROOT/lib/castor.jar ] ; then
        echo "   * Not present in ACS distribution, please correct this error.";
fi
cp $ACSROOT/lib/castor.jar $TARGET;
cp $ACSROOT/lib/c3p0-0.9.1.2.jar $TARGET;
cp $ACSROOT/lib/xalan.jar $TARGET;
cp $ACSROOT/lib/xalan_serializer.jar $TARGET;

