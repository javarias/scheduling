#!/bin/bash

if [ -z $INTLISTROOT ]; then
	export INTLISTROOT=/tmp
fi

if [ -z $INTROOT ]; then
	export INTROOT=/tmp
fi

ant $@
