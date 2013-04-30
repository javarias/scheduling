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
