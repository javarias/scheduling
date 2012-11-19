#!/usr/bin/env python

import sys
from Acspy.Clients.SimpleClient import PySimpleClient

print sys.argv

if len(sys.argv) != 2:
    print 'Error: This script expect exactly one argument, an ObsProject uid'

client = PySimpleClient()
up = client.getComponent('SCHEDULING_UPDATER')
up.refreshObsProject(sys.argv[1])
client.releaseComponent(up._get_name())
client.disconnect()
