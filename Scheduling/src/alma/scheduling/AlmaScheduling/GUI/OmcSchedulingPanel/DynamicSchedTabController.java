package alma.scheduling.AlmaScheduling.GUI.OmcSchedulingPanel;

import java.util.logging.Logger;
import java.util.Vector; 

import alma.scheduling.SBLite;
import alma.scheduling.ProjectLite;
import alma.scheduling.MasterSchedulerIF;
import alma.scheduling.InvalidOperation;
import alma.SchedulingExceptions.InvalidOperationEx;
import alma.scheduling.Define.*;
import alma.scheduling.Dynamic_Operator_to_Scheduling;
import alma.SchedulingExceptions.wrappers.AcsJInvalidOperationEx;

import alma.xmlentity.XmlEntityStruct;
import alma.acs.callbacks.RequesterUtil;
import alma.acs.callbacks.ResponseReceiver;
import alma.acs.nc.Consumer;
import alma.acs.container.ContainerServices;
import alma.Control.ExecBlockStartedEvent;
import alma.Control.ExecBlockEndedEvent;
import alma.offline.ASDMArchivedEvent;
import alma.xmlstore.XmlStoreNotificationEvent;
import alma.exec.extension.subsystemplugin.PluginContainerServices;

public class DynamicSchedTabController extends SchedulingPanelController {
    private String schedulername;
    private Consumer consumer = null;
    private Consumer ctrl_consumer = null;
    private String currentSBId;
    private String currentExecBlockId;
    private String arrayName;
    private DynamicSchedTab parent;
    private Dynamic_Operator_to_Scheduling dsComp;
    
    public DynamicSchedTabController(PluginContainerServices cs, String a, DynamicSchedTab p){
        super(cs);
        parent = p;
        arrayName = a;
        try{
            consumer = new Consumer(alma.xmlstore.CHANNELNAME.value,cs);
            consumer.addSubscription(XmlStoreNotificationEvent.class, this);
            consumer.consumerReady();
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public String getSchedulerName(){
        return schedulername;
    }

    public void setSchedulerName(String n){
        schedulername = n;
    }

    public void startDynamicScheduling() throws InvalidOperationEx {
        //tell MS to startScheduling
        try {
            getMSRef();
            schedulername = masterScheduler.createDynamicSchedulingComponent(arrayName);
            getDynamicSchedComponent();
            setCallbackInfo();
            masterScheduler.startScheduling1(new XmlEntityStruct(), arrayName);

            releaseMSRef();
        }catch(Exception e) {
            InvalidOperation e1 = new InvalidOperation("startDynamicScheduling", e.toString());
            AcsJInvalidOperationEx e2 = new AcsJInvalidOperationEx(e1);
            throw e2.toInvalidOperationEx();
        }
    }

    private void setCallbackInfo() throws Exception {
       ResponseReceiver allSbResponse = new ResponseReceiver() {
            public void incomingResponse (Object x) {
                logger.info("DS allSbResponse class: "+x.getClass().getName());
                updateSBTableWithAllSBs((String[])x);
            }
            public void incomingException (Exception x) {
                logger.severe("Responding failed: "+x);
            }
        };
       ResponseReceiver topSbResponse = new ResponseReceiver() {
            public void incomingResponse (Object x) {
                logger.info("***************************");
                logger.info("DS topSbResponse class: "+x.getClass().getName());
                logger.info("***************************");
                updateSBTableWithTopSBs((String[])x);
            }
            public void incomingException (Exception x) {
                logger.severe("Responding failed: "+x);
            }
        };
        dsComp.setCbForAllSBs(RequesterUtil.giveCBStringSequence((
                              ContainerServices)container,allSbResponse),
                              RequesterUtil.giveDescIn()); 
        dsComp.setCbForTopSBs(RequesterUtil.giveCBStringSequence((
                              ContainerServices)container,topSbResponse),
                              RequesterUtil.giveDescIn()); 
    }

    public void stopDynamicScheduling() {
        try {
            releaseDynamicSchedComponent();
            getMSRef();
            masterScheduler.destroyArray(arrayName);
            releaseMSRef();
            //container.releaseComponent(schedulername);
        } catch(Exception e) {
            logger.warning("SCHEDULING_PANEL: DS problem destroying array");
            logger.warning("SCHEDULING_PANEL: "+e.toString());
        }
    }

    private void getDynamicSchedComponent() throws Exception {
        dsComp = alma.scheduling.Dynamic_Operator_to_SchedulingHelper.narrow(
                        container.getComponent(schedulername));
    }

    private void releaseDynamicSchedComponent() throws Exception {
        container.releaseComponent(schedulername);
    }

    private void updateSBTableWithAllSBs(String[] s) {
        logger.info("Responded with all SBs");
        try{
           setCallbackInfo();
        } catch(Exception e) {
            logger.severe("SCHEDULING_PANEL: error setting callback for DS "+e.toString());
            e.printStackTrace();
        }
    }
    
    private void updateSBTableWithTopSBs(String[] s) {
        logger.info("Responded with list of top SBs");
        try{
            setCallbackInfo();
        } catch(Exception e) {
            logger.severe("SCHEDULING_PANEL: error setting callback for DS "+e.toString());
            e.printStackTrace();
        }
        getMSRef();
        SBLite[] topSBs = masterScheduler.getSBLite(s);
        logger.info("number of top sbs = "+topSBs.length);
        parent.updateTableWithSBList(topSBs);
        releaseMSRef();
    }

    public void respondToDS(String sbid) {
        try {
            dsComp.selectSB(sbid);
        } catch(Exception e){
            logger.severe("SP Couldn't respond to DS: "+e.toString());
            e.printStackTrace();
        }
    }
    
    public void receive(XmlStoreNotificationEvent e) {
    }

    public void processXmlStoreNotificationEvent(XmlStoreNotificationEvent e) {
    //    logger.info("SCHEDULING_PANEL: not doing anything with xml store notification event for now");
    }

    class CheckArchiveEvent implements Runnable {
        private XmlStoreNotificationEvent event;

        public CheckArchiveEvent(XmlStoreNotificationEvent e) {
            event = e;
        }
        public void run(){
            processXmlStoreNotificationEvent(event);
        }
    }
}
