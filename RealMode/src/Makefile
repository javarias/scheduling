#*******************************************************************************
#
# "@(#) $Id: Makefile,v 1.12 2003/07/31 23:09:03 sroberts Exp $"
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
USER_JFLAGS= -deprecation

#
# XML schema files and includes
#
XSDBIND = SchedulingEntities
XSDBIND_INCLUDE = systementities

XML_IDL="SchedBlock=alma.entity.xmlbinding.schedblock.SchedBlock; \
         SchedulingInfo=alma.entity.xmlbinding.schedulinginfo.SchedulingInfo; \
         SchedulingPolicy=alma.entity.xmlbinding.schedulingpolicy.SchedulingPolicy"

# 
# IDL Files and flags
# 
IDL_FILES = SchedulingInterfaces
#IDL_TAO_FLAGS =
#USER_IDL =

#
# Jarfiles and their directories
#
JARFILES = scheduling 
scheduling_DIRS =  alma
#scheduling_EXTRAS = 


INSTALL_FILES = ../lib/scheduling.jar

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
	@echo " . . . copying required CDB (schedulingCDB) in ACSDATA"
	-@cp -r ../config/schedulingCDB $(ACSDATA)/config;\
	          chmod -R ug+rw $(ACSDATA)/config/schedulingCDB;
	@echo " . . . installation done"

db : db_all
	@echo " . . . ../DB done"


#___oOo___
