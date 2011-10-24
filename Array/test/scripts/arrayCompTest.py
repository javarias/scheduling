#!/usr/bin/env python

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
import scheduling
from Acspy.Clients.SimpleClient import PySimpleClient
from scheduling import SchedBlockExecutionCallback
import scheduling__POA


class ExecCallback(scheduling__POA.SchedBlockExecutionCallback):
    def __init__(self):
        pass
    def report(self, item, newState, compl):
        print "received notifications"


client = PySimpleClient()

callback = ExecCallback()
coff = client.activateOffShoot(callback)

array = client.getComponent('SCHEDULING_ARRAY')
modes = [scheduling.INTERACTIVE]
lifecycle = scheduling.NORMAL
array.configure("Array001", modes, lifecycle)
array.monitorExecution('me', coff)

uid = 'uid://X0/X136/X2'
item = scheduling.SchedBlockQueueItem(0, uid)
array.push(item)
array.getQueue()
array.start()

# ...

array.push(scheduling.SchedBlockQueueItem(1, uid))

# ...

client.releaseComponent(array._get_name())
