package alma.scheduling.AlmaScheduling;

import alma.scheduling.*;
import alma.scheduling.MasterSchedulerIF;
import alma.scheduling.Interactive_PI_to_SchedulingOperations;
import alma.scheduling.ProjectLite;
import alma.SchedulingExceptions.InvalidOperationEx;
import alma.SchedulingExceptions.InvalidObjectEx;
import alma.SchedulingExceptions.UnidentifiedResponseEx;
import alma.SchedulingExceptions.SBExistsEx;
import alma.SchedulingExceptions.NoSuchSBEx;
import alma.SchedulingExceptions.wrappers.AcsJInvalidOperationEx;
import alma.SchedulingExceptions.wrappers.AcsJInvalidObjectEx;
import alma.SchedulingExceptions.wrappers.AcsJUnidentifiedResponseEx;
import alma.SchedulingExceptions.wrappers.AcsJNoSuchSBEx;
import alma.SchedulingExceptions.wrappers.AcsJSBExistsEx;

import alma.scheduling.Define.*;
import alma.scheduling.Scheduler.*;

import alma.ACS.ComponentStates;
import alma.acs.container.ContainerServices;
import alma.acs.component.ComponentLifecycle;
import alma.acs.component.ComponentLifecycleException;
import alma.xmlentity.XmlEntityStruct;

import java.util.logging.Logger;

public class ALMAInteractiveScheduler extends InteractiveScheduler 
    implements Interactive_PI_to_SchedulingOperations, ComponentLifecycle  {

    private String instanceName;
    private ContainerServices container;
    private String arrayname;
    private SBQueue queue;
    private Logger logger;
    private MasterSchedulerIF masterScheduler;
    private String schedulerId;
    
    public ALMAInteractiveScheduler() {
            
    }

    /////////////////////////////////////////////////////////////////////

    /**
     * Needed from ACSComponentOperations (MasterSchedulerIFOperations)
     * @return ComponentStates
     */
    public ComponentStates componentState() {
        ComponentStates state = container.getComponentStateManager().getCurrentState();
        return state;
    }
    /**
     * Needed from ACSComponentOperations (MasterSchedulerIFOperations)
     * @return String
     */
    public String name() {
        return instanceName;
    }

    public void initialize(ContainerServices cs)
        throws ComponentLifecycleException {

        container = cs;
        logger = cs.getLogger();
        this.instanceName = container.getName();
    }
    public void execute() throws ComponentLifecycleException{
        logger.info("SCHEDULING: Interactive Scheduler execute() ");
        try {
            this.masterScheduler =alma.scheduling.MasterSchedulerIFHelper.narrow(
                    container.getDefaultComponent(
                        "IDL:alma/scheduling/MasterSchedulerIF:1.0"));
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    public void cleanUp(){
    }

    public void aboutToAbort() {
    }
    /////////////////////////////////////////////////////////////////////

    public void startSession(String piId, String projectId) 
        throws InvalidOperationEx, InvalidObjectEx {
            
        try {
            masterScheduler.startInteractiveSession(piId, projectId, schedulerId);
        }catch(Exception e){
            InvalidOperation e1 = new InvalidOperation("startSession",e.toString());
            AcsJInvalidOperationEx e2 = new AcsJInvalidOperationEx(e1);
            throw e2.toInvalidOperationEx();
        }
    }

    public void setArray(String an) throws InvalidOperationEx, InvalidObjectEx {
        //TODO check to make sure the array exists eventually
        arrayname = an;
    }
    public void setSchedulerId(String id) {
        schedulerId = id;
    }

    public String getArray() {
        return arrayname;
    }
    
    public void addSB(String sbId) throws 
        InvalidOperationEx, InvalidObjectEx, SBExistsEx {
        try {
            masterScheduler.addInteractiveSB(sbId, schedulerId);
        }catch(Exception e){
            InvalidOperation e1 = new InvalidOperation("addSB",e.toString());
            AcsJInvalidOperationEx e2 = new AcsJInvalidOperationEx(e1);
            throw e2.toInvalidOperationEx();
        }
    }
    
    public void deleteSB(String sbId) throws InvalidOperationEx, NoSuchSBEx{
        try {
            masterScheduler.deleteInteractiveSB(sbId, schedulerId);
        }catch(Exception e){
            InvalidOperation e1 = new InvalidOperation("deleteSB",e.toString());
            AcsJInvalidOperationEx e2 = new AcsJInvalidOperationEx(e1);
            throw e2.toInvalidOperationEx();
        }
    }

    public void updateSB(String sbId) throws
        InvalidOperationEx, InvalidObjectEx, NoSuchSBEx {

        try {
            masterScheduler.updateInteractiveSB(sbId, schedulerId);
        }catch(Exception e){
            InvalidOperation e1 = new InvalidOperation("updateSB",e.toString());
            AcsJInvalidOperationEx e2 = new AcsJInvalidOperationEx(e1);
            throw e2.toInvalidOperationEx();
        }

    }

    public void executeSB(String sbId) 
        throws InvalidOperationEx, NoSuchSBEx {

        try {
            masterScheduler.executeInteractiveSB(sbId, schedulerId);
        } catch(Exception e){
            logger.severe("SCHEDULING: executeSB in IS_COMP"+e.toString());
            e.printStackTrace();
            InvalidOperation e1 = new InvalidOperation("executeSB",e.toString());
            AcsJInvalidOperationEx e2 = new AcsJInvalidOperationEx(e1);
            throw e2.toInvalidOperationEx();
        }
    }

    public void stopSB() throws InvalidOperationEx {
        try {
            masterScheduler.stopInteractiveSB(schedulerId);
        } catch(Exception e) {
            e.printStackTrace();
            InvalidOperation e1 = new InvalidOperation("stopSB",e.toString());
            AcsJInvalidOperationEx e2 = new AcsJInvalidOperationEx(e1);
            throw e2.toInvalidOperationEx();
        }
    }
    
    public void endSession() throws InvalidOperationEx{
        try {
            masterScheduler.endInteractiveSession(schedulerId);
        } catch (Exception e){
            e.printStackTrace();
            InvalidOperation e1 = new InvalidOperation("endSession",e.toString());
            AcsJInvalidOperationEx e2 = new AcsJInvalidOperationEx(e1);
            throw e2.toInvalidOperationEx();
        }
    }
}
