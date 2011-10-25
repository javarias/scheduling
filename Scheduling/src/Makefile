#*******************************************************************************
# ALMA - Atacama Large Millimeter Array
# Copyright (c) AUI - Associated Universities Inc., 2011
# (in the framework of the ALMA collaboration).
# All rights reserved.
# 
# This library is free software; you can redistribute it and/or
# modify it under the terms of the GNU Lesser General Public
# License as published by the Free Software Foundation; either
# version 2.1 of the License, or (at your option) any later version.
# 
# This library is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
# Lesser General Public License for more details.
# 
# You should have received a copy of the GNU Lesser General Public
# License along with this library; if not, write to the Free Software
# Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307  USA
#*******************************************************************************
#*******************************************************************************
#
# "@(#) $Id: Makefile,v 1.26 2011/10/25 15:25:39 dclarke Exp $ "
#
# Makefile of Scheduling Module
#
# who       when      what
# --------  --------  ----------------------------------------------
# sroberts  03/03/03  created
#

#*******************************************************************************
# This Makefile follows VLT Standards (see Makefile(5) for more).
#*******************************************************************************
# REMARKS
#    None
#------------------------------------------------------------------------


DEBUG = on
#COMPONENT_HELPERS=on
USER_JFLAGS= -deprecation #-Xlint

PY_SCRIPTS = ALMASched_lst_vs_day ALMASchedSim_antennaLocation

# Jarfiles and their directories
#

JARFILES = DynamicSchedulingSubsystem  schedulingComp 
schedulingComp_DIRS =  alma/scheduling/AlmaScheduling alma/scheduling/utils
DynamicSchedulingSubsystem_DIRS = alma/scheduling/Scheduler alma/scheduling/Define alma/scheduling/Event alma/scheduling/GUI alma/scheduling/MasterScheduler alma/scheduling/ObsProjectManager alma/scheduling/PlanningModeSim 
DynamicSchedulingSubsystem_EXTRAS = alma/scheduling/Image/*.jpg
#PMSimulator_EXTRAS = alma/scheduling/Image/*.jpg

#COMPONENTS_JARFILES = schedulingComp 
#schedulingComp_DIRS =  alma/scheduling/AlmaScheduling

SCRIPTS = runSchedSimGUI
#>>>>> END OF standard rules


#
# INCLUDE STANDARDS
# -----------------
MAKEDIRTMP := $(shell searchFile include/acsMakefile)
ifneq ($(MAKEDIRTMP),\#error\#)
   MAKEDIR := $(MAKEDIRTMP)/include
   include $(MAKEDIR)/acsMakefile
endif

#
# TARGETS
# -------
all:	do_all
	@echo " . . . 'all' done" 

clean : clean_all 
	@echo " . . . clean done"

clean_dist : clean_all clean_dist_all 
	@echo " . . . clean_dist done"

man   : do_man 
	@echo " . . . man page(s) done"

install : install_all
#	@echo " . . . copying required CDB (schedulingCDB) in ACSDATA"
#	-@cp -r ../config/schedulingCDB $(ACSDATA)/config;\
#	          chmod -R ug+rw $(ACSDATA)/config/schedulingCDB;
	@echo " . . . installation done"

db : db_all
	@echo " . . . ../DB done"


#___oOo___
