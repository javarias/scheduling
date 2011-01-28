#!/bin/bash
# To build this script is necessary to build EXEC/applications/test 
# and install it into your INTROOT
acsStartJava alma.exec.acsplugins.test.PluginStarter \
				 alma.scheduling.master.gui.SchedulingPanelMainFrame 2>&1 \
				 |tee tmp/SchedulingPanelMainFrame.log
