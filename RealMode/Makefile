#*******************************************************************************
#
# Makefile of Scheduling module
#
# who       when      what
# --------  --------  ----------------------------------------------
# NNNNNNNN  dd/mm/yy  created
#
#*******************************************************************************
# This Makefile follows ACS Standards (see Makefile(5) for more).
#*******************************************************************************
# REMARKS
#    None
#------------------------------------------------------------------------

#
# Modules in the various SCHEDULING sub-packages.
#
#MODULES_SCHED = ../SCHEDULING
#MODULES_KIT = vlt doc acs acstempl
#MODULES_TOOLS = cmm emacs compat doxygen tat expat loki
#MODULES_ACS = acsutil logging acserr cdb recovery baci enumprop maci acsdo acsnc acslog acsexmpl abeans objexp adminc jlog abeansgen acspy xmljbind comphelpgen XmlIdl define acstestentities jcont jcontexmpl

MODULES = ../SCHEDULING
#MODULES =  $(foreach kit, $(MODULES_KIT), Kit/$(kit)) \
#           $(foreach tools, $(MODULES_TOOLS), Tools/$(tools)) \
#           $(foreach acs, $(MODULES_ACS), CommonSoftware/$(acs)) \
#           acsBUILD


MAKE_FLAGS = "-k"
PLATFORM := $(shell uname)

SHELL=/bin/ksh
ECHO=echo

ifdef MAKE_VERBOSE
    AT = 
    OUTPUT =
else
    AT = @
    OUTPUT = > /dev/null
endif
#
os     = $(shell uname)
osrev  = $(shell uname -r)

ifeq ($(os),SunOS)
	realtime=YES
endif

define canned
	@$(ECHO) "############ Executing '$@' on all SCHEDULING modules #################"
	@for member in  $(foreach name, $(MODULES), $(name) ) ; do \
		    $(ECHO) "############ $${member}" ;\
		    if [ ! -d $${member} ]; then \
                         echo "### ==> $${member} MODULE NOT FOUND! FAILED! " | tee -a build.log;\
                    fi;\
		    if [ -f $${member}/src/Makefile ]; then \
			$(MAKE) $(MAKE_FLAGS) -C $${member}/src/ $@ || break ;\
		    elif [ -f $${member}/ws/src/Makefile ]; then \
			$(MAKE) $(MAKE_FLAGS) -C $${member}/ws/src/ $@ || break ;\
		    fi;\
		    if [ "$(realtime)" == "YES" ]; then \
			if [ -f $${member}/lcu/src/Makefile ]; then \
			$(MAKE) $(MAKE_FLAGS) -C $${member}/lcu/src/ $@ || break ;\
			fi;\
		    fi;\
		done
endef


#
# This target builds and installs the complete SCHEDULING 
# on a clean directory structure.
# Per each module it executes:
#    make clean all man install clean
#
build:   	clean_log checkModuleTree prepare update
	@$(ECHO) "... done"

#
# This target re-builds and installs the complete SCHEDULING 
# on an existing directory structure.
# Per each module it executes:
#    make clean all man install clean
#
rebuild:	clean_log update
	@$(ECHO) "... done"

clean_log:
	@$(ECHO) "############ Clean Build Log File: build.log #################"
	@rm -f build.log
	@touch build.log

#
# Check module tree
#
checkModuleTree:	
	@$(ECHO) "############ Check directory tree for modules  #################"| tee -a build.log
	@for member in  $(foreach name, $(MODULES), $(name) ) ; do \
		    if [ ! -d $${member} ]; then \
                         echo "### ==> $${member} MODULE NOT FOUND! FAILED! " | tee -a build.log;\
                    fi;\
		    if [ -f $${member}/src/Makefile ]; then \
                         sh acsBUILD/src/acsBUILDCheckModuleTree.sh $${member} | tee -a build.log;\
		    fi;\
		    if [ -f $${member}/ws/src/Makefile ]; then \
                         sh acsBUILD/src/acsBUILDCheckModuleTree.sh $${member}/ws | tee -a build.log;\
		    fi;\
		    if [ -f $${member}/lcu/src/Makefile ]; then \
                         sh acsBUILD/src/acsBUILDCheckModuleTree.sh $${member}/lcu | tee -a build.log;\
		    fi;\
		done


#
#
prepare:	
	@$(ECHO) "############ Prepare installation areas      #################" | tee -a build.log
	@sh acsBUILD/src/acsBUILDPrepareKit.sh >> build.log 2>& 1
	@$(MAKE) $(MAKE_FLAGS) -C Kit/acs/src/ all install clean >> build.log 2>& 1 || echo "### ==> FAILED! " | tee -a build.log
	@$(MAKE) $(MAKE_FLAGS) -C Kit/acstempl/src/ all install clean >> build.log 2>& 1 || echo "### ==> FAILED! " | tee -a build.log
	@$(MAKE) $(MAKE_FLAGS) -C Tools/doxygen/src/ all install clean >> build.log 2>& 1 || echo "### ==> FAILED! " | tee -a build.log

#
# Update of all core components
#
update:	
	@$(ECHO) "############ (Re-)build SCHEDULING Software         #################"| tee -a build.log
	@for member in  $(foreach name, $(MODULES), $(name) ) ; do \
		    if [ ! -d $${member} ]; then \
                         echo "### ==> $${member} MODULE NOT FOUND! FAILED! " | tee -a build.log;\
                    fi;\
		    if [ -f $${member}/src/Makefile ]; then \
		         $(ECHO) "############ $${member} MAIN" | tee -a build.log;\
                         $(MAKE) $(MAKE_FLAGS) -C $${member}/src/ clean >> build.log 2>& 1;\
                         $(MAKE) $(MAKE_FLAGS) -C $${member}/src/ all >> build.log 2>& 1 || echo "### ==> FAILED all ! " | tee -a build.log; \
                         $(MAKE) $(MAKE_FLAGS) -C $${member}/src/ man >> build.log 2>& 1 || echo "### ==> FAILED man ! " | tee -a build.log; \
                         $(MAKE) $(MAKE_FLAGS) -C $${member}/src/ install >> build.log 2>& 1 || echo "### ==> FAILED install ! " | tee -a build.log; \
                         $(MAKE) $(MAKE_FLAGS) -C $${member}/src/ clean >> build.log 2>& 1 || echo "### ==> FAILED clean ! " | tee -a build.log; \
                         continue ;\
		    fi;\
		    if [ -f $${member}/ws/src/Makefile ]; then \
		    $(ECHO) "############ $${member} WS" | tee -a build.log;\
                         $(MAKE) $(MAKE_FLAGS) -C $${member}/ws/src/ clean >> build.log 2>& 1;\
                         $(MAKE) $(MAKE_FLAGS) -C $${member}/ws/src/ all >> build.log 2>& 1 || echo "### ==> FAILED all ! " | tee -a build.log; \
                         $(MAKE) $(MAKE_FLAGS) -C $${member}/ws/src/ man >> build.log 2>& 1 || echo "### ==> FAILED man ! " | tee -a build.log; \
                         $(MAKE) $(MAKE_FLAGS) -C $${member}/ws/src/ install >> build.log 2>& 1 || echo "### ==> FAILED install ! " | tee -a build.log; \
                         $(MAKE) $(MAKE_FLAGS) -C $${member}/ws/src/ clean >> build.log 2>& 1 || echo "### ==> FAILED clean ! " | tee -a build.log; \
		    fi;\
		    if [ "$(realtime)" == "YES" ]; then \
			if [ -f $${member}/lcu/src/Makefile ]; then \
			    $(ECHO) "############ $${member} LCU" | tee -a build.log;\
                         $(MAKE) $(MAKE_FLAGS) -C $${member}/lcu/src/ clean >> build.log 2>& 1;\
                         $(MAKE) $(MAKE_FLAGS) -C $${member}/lcu/src/ all >> build.log 2>& 1 || echo "### ==> FAILED all ! " | tee -a build.log; \
                         $(MAKE) $(MAKE_FLAGS) -C $${member}/lcu/src/ man >> build.log 2>& 1 || echo "### ==> FAILED man ! " | tee -a build.log; \
                         $(MAKE) $(MAKE_FLAGS) -C $${member}/lcu/src/ install >> build.log 2>& 1 || echo "### ==> FAILED install ! " | tee -a build.log; \
                         $(MAKE) $(MAKE_FLAGS) -C $${member}/lcu/src/ clean >> build.log 2>& 1 || echo "### ==> FAILED clean ! " | tee -a build.log; \
			fi;\
		    fi;\
		done
	@sh acsBUILD/src/acsBUILDAfterBuildMod.sh >> build.log 2>& 1

#
# Test target
#
Test = test
$(Test):
	@$(ECHO) "############ Clean Test Log File: test.log #################"
	@rm -f test.log
	@touch test.log
	@$(ECHO) "############ TEST SCHEDULING Software #################"| tee -a test.log
	@for member in $(foreach name,$(MODULES),$(name)); do\
		if [ -d $${member}/ws/test ]; then\
			$(ECHO) "############ $${member}/ws/test WS TEST ############" | tee -a test.log ;\
			$(MAKE) -k -C $${member}/ws/test/ $@ | tee -a test.log | grep -E '(Nothing to|FAILED.|PASSED.|Error:)';\
			if [ -d $${member}/lcu/test ]; then\
				$(ECHO) "############ $${member}/lcu/test LCU TEST ############" | tee -a test.log;\
				$(MAKE) -k -C $${member}/lcu/test/ $@ | tee -a test.log | grep -E '(Nothing to|FAILED.|PASSED.|Error:)';\
			fi;\
		elif [ -d $${member}/test ]; then\
			$(ECHO) "############ $${member}/test MAIN TEST ############" | tee -a test.log ;\
			$(MAKE) -k -C $${member}/test/ $@ | tee -a test.log | grep -E '(Nothing to|FAILED.|PASSED.|Error:)';\
		else\
			$(ECHO) "### ==> $${member} TEST DIRECTORY STRUCTURE NOT FOUND! FAILED!" | tee -a test.log ;\
		fi;\
	done
	@$(ECHO) "... done"

#
# Standard targets
#
clean:	
	$(canned)
all:	
	$(canned)
install:
	$(canned)

#
# ___oOo___
