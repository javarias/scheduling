# $Id: otproto.py,v 1.1 2004/03/29 23:45:35 sroberts Exp $
#
# Reproduce the otproto's "batch mode" functionality:
# Imports an ObsProject from an XML file or a target list,
# maps it to its ObsUnitSet, and saves it to the repository.
#
# Usage:
#    otproto.py <filename>
#
# amchavan, 24-Oct-2003

import sys

filename = sys.argv[0]              # pathname of the file to import
project = importprj( filename )     # import the file
if project == None:                 # did everything go well?
    sys.exit( 1 )                   # NO, exit with error code
mapprj( project )                   # project imported: run the mapper
print project.dump()                # dump project to stdout...
storeprj( project )                 # ...and dump it to the archive 

