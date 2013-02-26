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
# PPPPPPPP
#
# "@(#) $Id: Makefile,v 1.23 2013/02/26 18:26:02 javarias Exp $"
#
# Makefile of ........
#
# who       when      what
# --------  --------  ----------------------------------------------
# ahoffsta  04/02/10  created
#

#*******************************************************************************
# This Makefile follows VLT Standards (see Makefile(5) for more).
#*******************************************************************************
# REMARKS
#    None
#------------------------------------------------------------------------

#
# user definable C-compilation flags
#USER_CFLAGS = 

#
# additional include and library search paths
#USER_INC = 
#USER_LIB = 

#
# MODULE CODE DESCRIPTION:
# ------------------------
# As a general rule:  public file are "cleaned" and "installed"  
#                     local (_L) are not "installed".

#
# C programs (public and local)
# -----------------------------
EXECUTABLES     =
EXECUTABLES_L   = 

#
# <brief description of xxxxx program>
xxxxx_OBJECTS   =	
xxxxx_LDFLAGS   =
xxxxx_LIBS      =

#
# special compilation flags for single c sources
#yyyyy_CFLAGS   = 

#
# Includes (.h) files (public only)
# ---------------------------------
INCLUDES        =

#
# Libraries (public and local)
# ----------------------------
LIBRARIES       =
LIBRARIES_L     =

#
# <brief description of lllll library>
lllll_OBJECTS   =

#
# Scripts (public and local)
# ----------------------------
SCRIPTS         =
#SCRIPTS_L       =

#
# TCL scripts (public and local)
# ------------------------------
TCL_SCRIPTS     =
TCL_SCRIPTS_L   =

#
# Python stuff (public and local)
# ----------------------------
PY_SCRIPTS         =
PY_SCRIPTS_L       =

PY_MODULES         =
PY_MODULES_L       =

PY_PACKAGES        =
PY_PACKAGES_L      =
pppppp_MODULES	   =

#
# <brief description of tttttt tcl-script>
tttttt_OBJECTS  =
tttttt_TCLSH    = 
tttttt_LIBS     = 

#
# TCL libraries (public and local)
# ------------------------------
TCL_LIBRARIES   =
TCL_LIBRARIES_L =

#
# <brief description of tttlll library>
tttlll_OBJECTS  = 

#
# Configuration Database Files
# ----------------------------
CDB_SCHEMAS = 

# 
# IDL Files and flags
# 
IDL_FILES =
TAO_IDLFLAGS =
USER_IDL =
#
# Jarfiles and their directories
#
JARFILES=SchedulingPMS SchedulingPMSCli
SchedulingPMS_DIRS=alma/scheduling/psm/util alma/scheduling/psm/sim alma/scheduling/psm/reports
SchedulingPMS_EXTRAS=alma/scheduling/psm/reports/general_report.xsl \
					 ph1mContext.xml \
					 alma/scheduling/psm/reports/bandsBeforeSim.jasper \
					 alma/scheduling/psm/reports/executiveReport.jasper \
					 alma/scheduling/psm/reports/lstRangesBeforeSim.jasper \
					 alma/scheduling/psm/reports/lstRangesAfterSim.jasper \
					 alma/scheduling/psm/reports/completionReport.jasper \
					 alma/scheduling/psm/reports/images/alma-logo.jpg \
					 alma/scheduling/algorithm/weather/data/simulation/Opacity_average_15min_all.dat \
					 alma/scheduling/algorithm/weather/data/simulation/Opacity_good_15min_all.dat \
					 alma/scheduling/algorithm/weather/data/simulation/Temp_average_15min_all.dat \
					 alma/scheduling/algorithm/weather/data/simulation/Temp_bad_15min_all.dat \
					 alma/scheduling/algorithm/weather/data/simulation/Temp_good_15min_all.dat \
					 alma/scheduling/algorithm/weather/data/simulation/WindDirection_average_15min_all.dat \
					 alma/scheduling/algorithm/weather/data/simulation/WindDirection_bad_15min_all.dat \
					 alma/scheduling/algorithm/weather/data/simulation/WindDirection_good_15min_all.dat \
					 alma/scheduling/algorithm/weather/data/simulation/WindSpeed_average_15min_all.dat \
					 alma/scheduling/algorithm/weather/data/simulation/WindSpeed_bad_15min_all.dat \
					 alma/scheduling/algorithm/weather/data/simulation/WindSpeed_good_15min_all.dat \
					 alma/scheduling/algorithm/weather/data/simulation/PathLenght_fluct_zenith_ave_all.dat \
					 alma/scheduling/algorithm/weather/data/simulation/PathLenght_fluct_zenith_good_all.dat \
					 alma/scheduling/algorithm/weather/data/simulation/PathLenght_fluct_zenith_bad_all.dat
					 
SchedulingPMSCli_DIRS=alma/scheduling/psm/cli

#
# java sources in Jarfile on/off
DEBUG= 
#
# ACS XmlIdl generation on/off
#
XML_IDL= 
#
# Java Component Helper Classes generation on/off
#
COMPONENT_HELPERS=
#
# Java Entity Classes generation on/off
#
XSDBIND=
#
# Schema Config files for the above
#
XSDBIND_INCLUDE=
# man pages to be done
# --------------------
MANSECTIONS =
MAN1 =
MAN3 =
MAN5 =
MAN7 =
MAN8 =

#
# local man pages
# ---------------
MANl =

#
# ASCII file to be converted into Framemaker-MIF
# --------------------
ASCII_TO_MIF = 

#
# other files to be installed
#----------------------------
INSTALL_FILES =

#
# list of all possible C-sources (used to create automatic dependencies)
# ------------------------------
CSOURCENAMES = \
	$(foreach exe, $(EXECUTABLES) $(EXECUTABLES_L), $($(exe)_OBJECTS)) \
	$(foreach rtos, $(RTAI_MODULES) , $($(rtos)_OBJECTS)) \
	$(foreach lib, $(LIBRARIES) $(LIBRARIES_L), $($(lib)_OBJECTS))

#
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
all: build_compiler build_reports do_all
	@echo $(CLASSPATH)
	@echo " . . . 'all' done" 

clean : clean_all 
	@echo " . . . clean done"

clean_dist : clean_all clean_dist_all 
	@echo " . . . clean_dist done"

man   : do_man 
	@echo " . . . man page(s) done"

install : install_all
	@echo " . . . installation done"

build_compiler: alma/util/JasperReportsCompiler.java
	@javac -cp $(shell vltMakeJavaClasspath) $^
	@jar cf ../lib/jasperCompiler.jar alma/util/*.class

build_reports: 	alma/scheduling/psm/reports/bandsBeforeSim.jasper \
				alma/scheduling/psm/reports/executiveReport.jasper \
				alma/scheduling/psm/reports/lstRangesBeforeSim.jasper \
				alma/scheduling/psm/reports/lstRangesAfterSim.jasper \
				alma/scheduling/psm/reports/completionReport.jasper \
	
%.jasper: %.jrxml
	acsStartJava -endorsed alma.util.JasperReportsCompiler $^
#___oOo___
