#!/bin/bash

##############################################################################
# ALMA - Atacama Large Millimiter Array
# (c) European Southern Observatory, 2002
# Copyright by ESO (in the framework of the ALMA collaboration),
# All rights reserved
#
# This library is free software; you can redistribute it and/or
# modify it under the terms of the GNU Lesser General Public
# License as published by the Free Software Foundation; either
# version 2.1 of the License, or (at your option) any later version.
#
# This library is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
# Lesser General Public License for more details.
#
# You should have received a copy of the GNU Lesser General Public
# License along with this library; if not, write to the Free Software
# Foundation, Inc., 59 Temple Place, Suite 330, Boston,
# MA 02111-1307  USA
#
# $Id: runArchiveDao.sh,v 1.1 2010/05/14 19:35:44 dclarke Exp $
#

# Documentation about the test goes here.
#
#

[ -f config/testEnv ] || exit $?
. config/testEnv
export APRC_WORK_DIR

export CLASSPATH=../lib/SchedulingCommonTest.jar:../slf4j-log4j12-1.5.2.jar:$CLASSPATH

declare TEST_OPTIONS=
declare TEST_CLASS=alma.scheduling.datamodel.obsproject.dao.ArchiveObsProjectDaoTest
declare TEST_LOG_FILE=tmp/ArchiveObsProjectDaoTest.log

printf "###############################################\n"
printf "ArchiveObsProjectDaoTest: "
acsStartJava -endorsed junit.textui.TestRunner "$TEST_CLASS" &> "$TEST_LOG_FILE"

RESULT=$?
if [ "$RESULT" = "0" ]; then
    printf "OK\n"
else
    printf "ERROR\n"
fi
exit "$RESULT"

# __oOo__
