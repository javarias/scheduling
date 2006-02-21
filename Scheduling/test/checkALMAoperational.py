#Only checks for the archive MC to be operational.
#For many user tests developers probably want to have
#control of which MC's the have online, but normally
#all will require the archive..
import ACS
import archive
from Acspy.Clients.SimpleClient import PySimpleClient
client = PySimpleClient()
comp1 = client.getComponent("ARCHIVE_MASTER_COMP")

state1 = comp1._get_currentStateHierarchy()

while(state1.get_sync()[0][1] != 'OPERATIONAL'): pass
print 'archive comp operational'
client.releaseComponent ("ARCHIVE_MASTER_COMP")
