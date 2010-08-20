#!/usr/bin/env python

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
