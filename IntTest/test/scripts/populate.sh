#!/bin/bash

if [[ $# == 0 ]]
then
   projects=(projects/R80*.aot)
else
   projects=($@)
fi

export JAVA_OPTIONS="-Dot.submission.host=http://127.0.0.1:8180 -Dot.submission.user=john -Dot.submission.pass=john -Dalma.obsprep.allowprivileged=true"

for project in ${projects[@]} ; do
    ALMA-OT -batch $INTROOT/bin/import-saveasnew.py ${project}
done

unset JAVA_OPTIONS
