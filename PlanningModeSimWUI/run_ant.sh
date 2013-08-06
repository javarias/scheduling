#!/bin/bash

if [ -n $INTLIST ]; then
	for dir in `echo $INTLIST |tr ':' ' '`; do
		fix_dir=${dir//\//\\\/}
		REPLACE="$REPLACE<artifact pattern=\"$fix_dir\/lib\/\[artifact\]\.\[ext\]\" \/> "
	done
	cp ivysettings.xml ivysettings.xml.orig
	cat ivysettings.xml.orig | sed "s/<artifact\/>/$REPLACE/g" > ivysettings.xml
fi

if [ -z $INTROOT ]; then
	export INTROOT=/tmp
fi

ant $@
mv ivysettings.xml.orig ivysettings.xml
