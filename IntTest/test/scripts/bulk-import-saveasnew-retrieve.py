# $Id: bulk-import-saveasnew-retrieve.py,v 1.2 2009/09/22 22:40:17 rhiriart Exp $
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
			good = good + 1
			exportprj(fromArchive, outFilename);

if good != 1:
	print good, 'projects imported, stored and retrieved'
else:
	print '1 project imported, stored and retrieved'
	
if bad > 1:
	print bad, 'projects failed'
elif bad == 1:
	print '1 project failed'
		
sys.exit(bad)
	
