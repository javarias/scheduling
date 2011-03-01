#*******************************************************************************
# PPPPPPPP
#
# "@(#) $Id: Makefile,v 1.21 2011/03/01 00:35:37 javarias Exp $"
#
# Makefile of ........
#
# who       when      what
# --------  --------  ----------------------------------------------
# javarias  25/01/10  created
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
SCRIPTS_L       =

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
JARFILES=SchedulingCommon
SchedulingCommon_DIRS=alma
SchedulingCommon_EXTRAS=alma/scheduling/datamodel/executive/Executive.hbm.xml \
                        alma/scheduling/datamodel/obsproject/ObsProject.hbm.xml \
                        alma/scheduling/datamodel/obsproject/ObsUnit.hbm.xml \
                        alma/scheduling/datamodel/obsproject/ObservingParameters.hbm.xml \
                        alma/scheduling/datamodel/obsproject/FieldSource.hbm.xml \
                        alma/scheduling/datamodel/obsproject/Target.hbm.xml \
                        alma/scheduling/datamodel/weather/WeatherHistRecord.hbm.xml \
                        alma/scheduling/datamodel/weather/AtmParameters.hbm.xml \
                        alma/scheduling/datamodel/output/Output.hbm.xml \
                        alma/scheduling/datamodel/config/Configuration.hbm.xml \
                        alma/scheduling/datamodel/observatory/TelescopeEquipment.hbm.xml \
                        alma/scheduling/CommonContext.xml \
                        ../config/hibernate.cfg.xml ehcache.xml
#
# java sources in Jarfile on/off
DEBUG=on
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
INSTALL_FILES += ../lib/hamcrest-core-1.1.jar ../lib/hamcrest-library-1.1.jar ../lib/jmock-2.5.1.jar ../lib/jmock-junit3-2.5.1.jar \
				 ../lib/cglib-nodep-2.1_3.jar ../lib/jmock-legacy-2.5.1.jar ../lib/objenesis-1.0.jar
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
all: gen do_all
#	cp scheduling.properties $(ACSDATA)/config
	@echo " . . . 'all' done" 

clean : clean_gen clean_all 
	@echo " . . . clean done"

clean_dist : clean_all clean_dist_all 
	@echo " . . . clean_dist done"

man   : do_man 
	@echo " . . . man page(s) done"

install : install_all
	@echo " . . . installation done"

#gen:
#	acsStartJava -endorsed org.exolab.castor.builder.SourceGenerator -f -i ../config/executive.xsd -package alma.scheduling.input.executive.generated
#	acsStartJava -endorsed org.exolab.castor.builder.SourceGenerator -f -i ../config/output.xsd -package alma.scheduling.output.generated
#	acsStartJava -endorsed org.exolab.castor.builder.SourceGenerator -f -i ../config/obsunit.xsd -package alma.scheduling.input.obsproject.generated
#	acsStartJava -endorsed org.exolab.castor.builder.SourceGenerator -f -i ../config/config.xsd -package alma.scheduling.input.config.generated
#	acsStartJava -endorsed org.exolab.castor.builder.SourceGenerator -f -i ../config/observatory.xsd -package alma.scheduling.input.observatory.generated
CASTOR_GEN = executive output obsunit config observatory


CASTOR_TARGETS = $(foreach target, $(CASTOR_GEN), ../object/$(target).castorgen)
.PHONY: gen 
gen: $(CASTOR_TARGETS)

clean_gen:
	$(shell cat ../object/*.castorgen | sed -r "s/\./\//g" | xargs rm -rf)
	@rm -rf $(CASTOR_TARGETS)

../object/executive.castorgen: ../config/executive.xsd
	acsStartJava -endorsed org.exolab.castor.builder.SourceGenerator -f -i $< -package alma.scheduling.input.executive.generated
	 $(shell echo alma.scheduling.input.executive.generated > ../object/executive.castorgen)

../object/output.castorgen: ../config/output.xsd
	 acsStartJava -endorsed org.exolab.castor.builder.SourceGenerator -f -i $<   -package alma.scheduling.output.generated
	 $(shell echo alma.scheduling.output.generated > ../object/output.castorgen)

../object/obsunit.castorgen: ../config/obsunit.xsd
	acsStartJava -endorsed org.exolab.castor.builder.SourceGenerator -f -i $< -package alma.scheduling.input.obsproject.generated
	$(shell echo alma.scheduling.input.obsproject.generated > ../object/obsunit.castorgen)

../object/config.castorgen: ../config/config.xsd
	acsStartJava -endorsed org.exolab.castor.builder.SourceGenerator -f -i $< -package alma.scheduling.input.config.generated
	$(shell echo alma.scheduling.input.config.generated > ../object/config.castorgen)

../object/observatory.castorgen: ../config/observatory.xsd
	acsStartJava -endorsed org.exolab.castor.builder.SourceGenerator -f -i $< -package alma.scheduling.input.observatory.generated
	$(shell echo alma.scheduling.input.observatory.generated > ../object/observatory.castorgen)



#$(foreach target, $CASTOR_XSD, 
#	$(eval 
#		$(call CASTOR_generation, $(target), ../config/$(target).xsd, alma.scheduling.input.$(target).generated)
#	)
#)

#Generic castor incremental generation
#define CASTOR_generation
#../object/$(1).castorgen: $(2)
#	acsStartJava -endorsed org.exolab.castor.builder.SourceGenerator -f -i $(2) -package $(3)
#	$(shell echo $(3) >> ../object/$(1).castorgen)
#endef


#___oOo___
