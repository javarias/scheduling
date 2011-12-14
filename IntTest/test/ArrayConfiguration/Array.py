import Control
import asdmIDLTypes
import time
import ACSErr
import Control
import offline
from ArrayConfiguration.ArrayConfiguration import ArrayConfiguration
from ArrayConfiguration.ArrayConfiguration import FailureMode
from ArrayConfiguration.ArrayConfiguration import FailsNever
from ArrayConfiguration.ArrayConfiguration import FailsRandomly
from ArrayConfiguration.ArrayConfiguration import FailsRegularly
from ArrayConfiguration.ArrayConfiguration import TimingMode
from ArrayConfiguration.ArrayConfiguration import TimingConstant
from ArrayConfiguration.ArrayConfiguration import TimingNormal

class Array:

#
# Construction
#
    def __init__(self, name):
        self.name = str(name)
        self.controlName = 'CONTROL/' + str(name)

#
# The component interface
#
    def initialize(self):
        LOGGER.logInfo('initialize() called for ' + str(self.name) + ' from CDB')
        return None

    def cleanUp(self):
        LOGGER.logInfo('cleanUp() called for ' + str(self.name) + ' from CDB')
        return None
 
    def getMasterState(self):
        LOGGER.logInfo('getMasterState() called for ' + str(self.name) + ' from CDB')
        return Control.OPERATIONAL
   
    def createAutomaticArray(self):
        LOGGER.logInfo('createAutomaticArray() called for ' + str(self.name) + ' from CDB')
        id = Control.ArrayIdentifier(self.name, self.controlName)
        execTime = TimingNormal(60, 20)
        execFail = FailsNever()
        archTime = TimingConstant(10)
        archFail = FailsNever()
        config = ArrayConfiguration(execFail, execTime, archFail, archTime)
        setGlobalData(self.name, config)
        LOGGER.logInfo("finishing createAutomaticArray() for " + str(self.name) + ", config is " + str(config))
        return id

    def getArrayName(self):
        return self.name
 
    def getArrayComponentName(self):
        return self.controlName
   
    def observe(self, sbId, sessionId, when, container):
        LOGGER.logInfo('observe() called for ' + str(self.name) + ' from CDB')
        LOGGER.logInfo('    sbId:      ' + str(sbId))
        LOGGER.logInfo('    sessionId: ' + str(sessionId))
        LOGGER.logInfo('    when:      ' + str(when))
        LOGGER.logInfo('    container: ' + str(container))
        
        # Create an ExecBlock IDLEntityRef        
        execCount = getGlobalData("execCounter")
        entityId = "uid://X0/X" + str(execCount)
        setGlobalData("execCounter", int(execCount)+1)
        partId = "X0"
        entityTypeName = "ExecBlock"
        instanceVersion = "1.0"
        execId = asdmIDLTypes.IDLEntityRef(entityId, partId, entityTypeName, instanceVersion)
        config = getGlobalData(self.name)
        
        logMessage = "Simulation parameters for SB " + str(sbId.entityId) + ", EB " + str(entityId) + " on " + str(self.name) + ": "
        LOGGER.logInfo(logMessage)
        LOGGER.logInfo("BBBB 1")
        
        # Create the ExecBlockStartedEvent
        execBlockStartedEvent = Control.ExecBlockStartedEvent(execId, sbId, sessionId, self.name, 0L)
        
        # Get the timings
        executeTime = config.getExecTime()
        archiveTime = config.getArchTime()
        
        # Create the ExecBlockEndedEvent
        logMessage = logMessage + "execution " + str(executeTime) + "s "
        
        if config.execSucceeds():
            xcompletion = Control.SUCCESS
            logMessage = logMessage + "(SUCCESS), "
        else:
            xcompletion = Control.FAIL
            logMessage = logMessage + "(FAIL), "
            
        execBlockEndedEvent = Control.ExecBlockEndedEvent(execId, sbId, sessionId, self.name, "DC000", xcompletion, [], 0L)
            
        # Create the ASDMArchivedEvent
        logMessage = logMessage + "archiving " + str(archiveTime) + "s "
        
        dcName = self.name + "/DC001"
        bddStreamInfo = Control.BDDStreamInfo("", 0, 0, 0)
        dataCapturerId = offline.DataCapturerId(dcName, self.name, sessionId, sbId, bddStreamInfo, bddStreamInfo)
        if config.archSucceeds():
            acompletion = "complete"
        else:
            acompletion = "fail"
            
        logMessage = logMessage + "(" + acompletion + ")"
        asdmArchivedEvent = offline.ASDMArchivedEvent(dataCapturerId, acompletion, execId, 0L)
                
        # Log the expected behaviour
        LOGGER.logInfo(logMessage)
                
        # Send the ExecBlockStartedEvent, wait some seconds, and send the ExecBlockEndedEvent
        LOGGER.logInfo("Sending ExecBlockStartedEvent")
        supplyEventByInstance(self.name, "CONTROL_SYSTEM", execBlockStartedEvent)
        time.sleep(executeTime)
        LOGGER.logInfo("Sending ExecBlockEndedEvent")
        supplyEventByInstance(self.name, "CONTROL_SYSTEM", execBlockEndedEvent)
        time.sleep(archiveTime)
        LOGGER.logInfo("Sending ASDMArchivedEvent")
        supplyEventByInstance(self.name, "CONTROL_SYSTEM", asdmArchivedEvent)

