# $Id: import-save.py,v 1.1 2004/09/30 20:12:43 sroberts Exp $
#
# Imports an ObsProject from an XML file or a target list
# and saves it to the repository.
#
# Usage:
#    ALMA-OT -batch import-save.py <filename>
#
# amchavan, ESO, 17-09-2004

import sys

filename = sys.argv[0]              # pathname of the file to import
project = importprj( filename )     # import the file
if project == None:                 # did everything go well?
    sys.exit( 1 )                   # NO, exit with error code
storeprj( project )                 # and store new project into the archive 

