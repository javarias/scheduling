# $Id: import-saveasnew-retrieve.py,v 1.2 2009/09/22 22:40:17 rhiriart Exp $

import sys

filename = sys.argv[0] # pathname of the file to import
outfilename = sys.argv[1] # pathname of the file to export

#xml = "./ExpertTestProject.aot"

project = importprj( filename )          # import project
if project == None:                 # did everything go well?
    sys.exit( 1 )                   # NO, exit with error code

# dump some info to the console for our tests
print "project has EntityID ", project.getEntityID()

# save to the archive
storenewprj(project)                # store new project into the archive 

# retrieve from the archive
id = project.getEntityID()
fromArchive = retrieveprj(project.getEntityID())
if fromArchive == None:             # did everything go well?
    sys.exit( 1 )                   # NO, exit with error code

# dump some info to the console for our tests
print "retrieved project has EntityID ", fromArchive.getEntityID()

# and export it back to disk
exportprj(fromArchive, outfilename)
print "exported project to", outfilename
