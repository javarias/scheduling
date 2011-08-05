#!/bin/bash

if [[ $# == 0 ]]
then
   projects=(projects/R80*.aot)
else
   projects=($@)
fi

basicScript="import-saveasnew.py"

script=`which ${basicScript}`
echo $script

if [ -z "${script}" ]
then
   echo "Cannot find script ${basicScript} on path"
   exit 666
fi

export JAVA_OPTIONS="-Dot.submission.host=http://127.0.0.1:8180 -Dot.submission.user=john -Dot.submission.pass=john -Dalma.obsprep.allowprivileged=true"

declare -i i=0
declare -i r

for project in ${projects[@]} ; do
    ALMA-OT -batch ${script} ${project}
    i=i+1
    echo "$i / ${#projects[@]}"
done

unset JAVA_OPTIONS
