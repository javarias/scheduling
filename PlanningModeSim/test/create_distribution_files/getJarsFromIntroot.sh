#!/bin/bash

for f in `ls *.jar`; do
    if test -f $INTROOT/lib/$f; then
        printf "replacing %s for %s\n" $f $INTROOT/lib/$f
        cp $INTROOT/lib/$f .
    fi 
done
