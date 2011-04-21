from Acspy.Clients.SimpleClient import PySimpleClient
import Control
import time

client = PySimpleClient()
array = client.getComponent("CONTROL/Array010")
array.beginExecution()
time.sleep(60)
array.endExecution(Control.SUCCESS, "This is not a interesting message")
client.releaseComponent("CONTROL/Array010")
