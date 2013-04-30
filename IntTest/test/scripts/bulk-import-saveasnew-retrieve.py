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
#
# script to load each of the .aot files given as parameters,
# store the project it contains to the archive and finally
# retrieve it from the archive.
#
# dclarke, 30-Jan-2006

import sys
import os

dirPending = 0
sep = '/'
dir = '.' + sep
files = []
uids  = []

for arg in sys.argv:
	if dirPending:
		dir = arg
		if dir[-1] != sep:
			dir = dir + sep
		dirPending = 0
	else:
		if arg == '-d':
			dirPending = 1
		else:
			files.append(dir + arg)

bad = 0
good = 0

for filename in files:
	print 'Importing project from file ' + filename  # project = importprj( filename ) 
	project = importprj(filename)     # import the file
	if project == None:               # did everything go well?
		print 'Cannot import project ' + filename
		bad = bad + 1
	else:
		print 'Project\'s EntityID = ' + project.getEntityID()
		print 'Store project ' + filename
		storenewprj(project)      # and store new project into the archive
		
		fromArchive = retrieveprj(project.getEntityID()) # retrieve the project from the archive
		if fromArchive == None:             # did everything go well?
			print 'Cannot retrieve project ' + project.getEntityID()
			bad = bad + 1
		else:
			outFilename = "out_" + filename;
			print "Retrieved project has EntityID ", fromArchive.getEntityID(), ", exporting to ", outFilename
			uids.append(fromArchive.getEntityID())
			good = good + 1
			exportprj(fromArchive, outFilename);

if good != 1:
	print good, 'projects imported, stored and retrieved. UIDs are:'
else:
	print '1 project imported, stored and retrieved. UID is:'

for uid in uids:
	print '	' + uid
	
if bad > 1:
	print bad, 'projects failed'
elif bad == 1:
	print '1 project failed'
		
sys.exit(bad)
	
