#! /bin/sh
#*******************************************************************************
# ALMA - Atacama Large Millimiter Array
# (c) Associated Universities Inc., 2010 
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
# Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307  USA
#
# "@(#) $Id: unitTest.sh,v 1.4 2011/06/17 23:00:01 javarias Exp $"
#
# who       when      what
# --------  --------  ----------------------------------------------
# javarias  2010-10-25  created
#

#************************************************************************
#   NAME
# 
#   SYNOPSIS
# 
#   DESCRIPTION
#
#   FILES
#
#   ENVIRONMENT
#
#   RETURN VALUES
#
#   CAUTIONS
#
#   EXAMPLES
#
#   SEE ALSO
#
#   BUGS     
#
#------------------------------------------------------------------------
#

CLASSPATH=../lib/ACScomponents/SchedulingMasterImpl.jar:$CLASSPATH
acsStartJava alma.acs.testsupport.tat.TATJUnitRunner alma.scheduling.master.compimpl.MasterSchedulerUnitTests
#
# ___oOo___
