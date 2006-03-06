#*******************************************************************************
#
# "@(#) $Id: Makefile,v 1.8 2006/03/06 15:02:37 sslucero Exp $"
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

#
# Jarfiles and their directories
#
JARFILES = scheduling PMSSimulator
scheduling_DIRS =  alma/scheduling/
scheduling_EXTRAS = alma/scheduling/Image/*.jpg

#JARFILES = PMSSimulator
PMSSimulator_DIRS = alma/scheduling/Scheduler alma/scheduling/Define alma/scheduling/Event alma/scheduling/GUI alma/scheduling/MasterScheduler/ alma/scheduling/ObsProjectManager/ alma/scheduling/PlanningModeSim/
PMSSimulator_EXTRAS = alma/scheduling/Image/*.jpg

INSTALL_FILES = ../lib/scheduling.jar

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
