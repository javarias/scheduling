###################################################################
#
#  TESTLIST
#
# ALMA - Atacama Large Millimiter Array
# (c) Associated Universities Inc., 2002 
# (c) European Southern Observatory, 2002
# Copyright by ESO (in the framework of the ALMA collaboration)
# and Cosylab 2002, All rights reserved
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
#  "@(#) $Id: TESTLIST.td,v 1.1 2003/08/28 20:00:31 sroberts Exp $"
#
#  Following is a list of test programs that are called 
#  sequencially.
#  When two programs are specified the first one is treated
#  as a server and started in the background.
#
#  !!! NOTE currently only a maximum of two programs are supported
#
#  Following actions are performed:
#
#  - Program is started with output redirected into file.
#  - If two programs are specified the output is merged.
#  - The timestamp is striped off the output
#  - The output is compared with the reference output <program>.REF
#  - If difference are found the are listed in <program>.diff
# 
#
###################################################################
#
# The initial sleep is necessary to avoid timint problems:
# it is necessary to wait for MACI from the previous test
# to terminate before starting it away.
# Currently after MACI receives a kill, is waits some time before
# terminating.
# It will be fixed when a shutdown command for MACI will be
# available.
# ------------------
# In this case we do not care about the output from the ORB services
# script, manager, or the C++ activator(s).
1  testscript9  "@SLEEP 10" "maciActivate Activator" "@SLEEP 20"  "acsStartContainer hugoActivator" "@SLEEP 20" "almaOTBatchMode.sh targets.txt" "@SLEEP 20" "abeansStart -endorsed ALMA.scheduling.test.SchedTest7" 

#___o0o___
