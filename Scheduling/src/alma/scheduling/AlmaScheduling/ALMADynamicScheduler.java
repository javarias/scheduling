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

import alma.ACS.CBDescIn;
import alma.ACS.CBstringSeq;
import alma.acs.callbacks.ResponderUtil;
import alma.ACS.ComponentStates;
import alma.acs.container.ContainerServices;
import alma.acs.component.ComponentLifecycle;
import alma.acs.component.ComponentLifecycleException;
import alma.xmlentity.XmlEntityStruct;

import java.util.logging.Logger;
import java.util.Vector;

public class ALMADynamicScheduler
    implements Dynamic_Operator_to_SchedulingOperations, ComponentLifecycle  {

    private String instanceName;
    private ContainerServices container;
    private String arrayname;
    //private SBQueue queue;
    private Logger logger;
    private MasterSchedulerIF masterScheduler;
    private String schedulerId;
    private CBstringSeq allSB_cb;
    private CBDescIn allSB_cbDesc;
    private CBstringSeq topSB_cb;
    private CBDescIn topSB_cbDesc;
    private String currentMessageId;
    
    public ALMADynamicScheduler() {
            
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
            masterScheduler=null;
        }
    }

    public void cleanUp(){
        aboutToAbort();
    }

    public void aboutToAbort() {
        try {
            container.releaseComponent(masterScheduler.name());
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
    /////////////////////////////////////////////////////////////////////

    public void setSchedulerId(String id){
        schedulerId = id;
    }

    public void setArray(String array){
        arrayname = array;
    }

    public String getArray(){
        return arrayname;
    }

    public SBLite[] getAllSBLites(){
        return null;
    }

    public SBLite[] getTopSBLites(){
        return null;
    }

    public void stopSB(){
    }

    public void selectSB(String sbid) throws NoSuchSBEx, InvalidOperationEx {
        //respond to MS with sbid as reply and currentMessageId
        if(currentMessageId == null) {
            InvalidOperation e1 = new InvalidOperation("selectSB", 
                                                       "CurrentMessageId == null");
            AcsJInvalidOperationEx e2 = new AcsJInvalidOperationEx(e1);
            throw e2.toInvalidOperationEx();
        }
        try{
            (alma.scheduling.MasterSchedulerIFHelper.narrow(
                container.getDefaultComponent(
                    "IDL:alma/scheduling/MasterSchedulerIF:1.0"))).response(
                        currentMessageId, sbid);
        }catch(Exception e) {
            logger.severe("SCHEDULING_DS: error trying to select SB "+e.toString());
            e.printStackTrace();
        }
    }

    public void setAllSbs(SBLite[] sbs){
        String[] allSBs = new String[sbs.length];
        for(int i = 0; i < sbs.length; i++){
            allSBs[i] = sbs[i].schedBlockRef;
        }
        ResponderUtil.respond(allSBs, allSB_cb, allSB_cbDesc);
    }

    public void setTopSbs(SBLite[] sbs, String messageId) {
        currentMessageId = messageId;
        String[] topSBs = new String[sbs.length];
        for(int i = 0; i < sbs.length; i++){
            topSBs[i] = sbs[i].schedBlockRef;
        }
        ResponderUtil.respond(topSBs, topSB_cb, topSB_cbDesc);
    }

    public void setCbForAllSBs(CBstringSeq cb, CBDescIn descIn){
        allSB_cb = cb ;
        allSB_cbDesc = descIn;
    }
    public void setCbForTopSBs(CBstringSeq cb, CBDescIn descIn){
        logger.info("SCHEDULING: Setting callback info for TopSBs");
        topSB_cb = cb;
        topSB_cbDesc = descIn;
    }
}
