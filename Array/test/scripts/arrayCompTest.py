#!/usr/bin/env python

import scheduling

from Acspy.Clients.SimpleClient import PySimpleClient

client = PySimpleClient()
array = client.getComponent('SCHEDULING_ARRAY')
modes = [scheduling.INTERACTIVE]
lifecycle = scheduling.NORMAL
array.configure("Array001", modes, lifecycle)
uid = 'uid://X0/X136/X2'
item = scheduling.SchedBlockQueueItem(0, uid)
array.push(item)
array.getQueue()
array.start()

array.push(scheduling.SchedBlockQueueItem(1, uid))

client.releaseComponent(array._get_name())
