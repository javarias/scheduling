#*******************************************************************************
# E.S.O. - ACS project
#
# "@(#) $Id: Makefile,v 1.2 2004/04/22 15:32:12 sroberts Exp $"
#
# Makefile of ........
#
# who       when      what
# --------  --------  ----------------------------------------------
# sohaila  06/11/03  created
#

#*******************************************************************************
# This Makefile follows VLT Standards (see Makefile(5) for more).
#*******************************************************************************
# REMARKS
#    None
#------------------------------------------------------------------------

#
# user definable Java-compilation flags
USER_JFLAGS= -deprecation

#
# Jarfiles and their directories
#
JARFILES=SchedPlanningModeSimulator 
SchedPlanningModeSimulator_DIRS=alma
jjj_EXTRAS= 

#
# java sources in Jarfile on/off
DEBUG=on

#
# other files to be installed
#----------------------------
INSTALL_FILES = ../lib/SchedPlanningModeSimulator.jar

#
#>>>>> END OF standard rules

#
# INCLUDE STANDARDS
# -----------------
ifdef ACSROOT
    MAKEDIR  = $(shell if [ -f $(INTROOT)/include/acsMakefile ];  then \
                     echo $(INTROOT)/include; \
	           else \
	             echo $(ACSROOT)/include; \
		   fi;)
    include $(MAKEDIR)/acsMakefile
else
    MAKEDIR  = $(shell if [ -f $(INTROOT)/include/vltMakefile ];  then \
                     echo $(INTROOT)/include; \
	           else \
	             echo $(VLTROOT)/include; \
		   fi;)
    include $(MAKEDIR)/vltMakefile
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
	@echo " . . . installation done"

db : db_all
	@echo " . . . ../DB done"


#___oOo___
