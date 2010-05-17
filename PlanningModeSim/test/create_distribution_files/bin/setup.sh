export APRC_HOME=$(pwd)
export APRC_WORK_DIR=$APRC_HOME/tmp

for f in `find $APRC_HOME -name "*.jar"`; do
    CLASSPATH=$f:$CLASSPATH
done
export CLASSPATH

export PATH=$APRC_HOME/bin:$PATH
