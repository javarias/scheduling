package alma.scheduling.AlmaScheduling.GUI.OmcSchedulingPanel;

import java.util.logging.Logger;
import alma.scheduling.SBLite;
import alma.scheduling.ProjectLite;
import alma.scheduling.MasterSchedulerIF;
import alma.scheduling.Interactive_PI_to_Scheduling;
import alma.scheduling.Define.*;
import alma.acs.nc.Consumer;
import alma.Control.ExecBlockEndedEvent;
import alma.offline.ASDMArchivedEvent;
import alma.xmlstore.XmlStoreNotificationEvent;
import alma.exec.extension.subsystemplugin.PluginContainerServices;

public class InteractiveSchedTabController extends SchedulingPanelController {
    private Interactive_PI_to_Scheduling scheduler;
    private String schedulername;
    private Consumer consumer = null;
    private Consumer ctrl_consumer = null;
    private String currentSBId;
    private String currentExecBlockId;
    private String arrayName;
    private InteractiveSchedTab parent;
    
    public InteractiveSchedTabController(PluginContainerServices cs, String a, InteractiveSchedTab p){
        super(cs);
        parent = p;
        arrayName = a;
        startInteractiveScheduler();
        try{
            consumer = new Consumer(alma.xmlstore.CHANNELNAME.value,cs);
            consumer.addSubscription(XmlStoreNotificationEvent.class, this);
            consumer.consumerReady();
            ctrl_consumer = new Consumer(alma.Control.CHANNELNAME_CONTROLSYSTEM.value, container);
            ctrl_consumer.addSubscription(alma.Control.ExecBlockEndedEvent.class, this);
            ctrl_consumer.addSubscription(alma.offline.ASDMArchivedEvent.class, this);
            ctrl_consumer.consumerReady();
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public String getSchedulerName(){
        return schedulername;
    }

    private void startInteractiveScheduler() {
        try {
            getMSRef();
            schedulername = masterScheduler.startInteractiveScheduling1(arrayName);
            logger.info("SCHEDULINGPANEL: Interactive scheduling ("+schedulername+") started on array "+arrayName+".");
            releaseMSRef();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public void stopInteractiveScheduling() {
        try {
            getMSRef();
            masterScheduler.stopInteractiveScheduler(schedulername);
            masterScheduler.destroyArray(arrayName);
            releaseMSRef();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void getISRef() {
        try {
            System.out.println("scheduler name = "+ schedulername);
            scheduler = alma.scheduling.Interactive_PI_to_SchedulingHelper.narrow(
                    container.getComponent(schedulername));
        }catch(Exception e){
            e.printStackTrace();
        }
        logger.info("Got interactive scheduler reference");

    }
    public void releaseISRef(){
        try{
            logger.info("About to release "+scheduler.name());
                getMSRef();
                masterScheduler.stopInteractiveScheduler(schedulername);
                releaseMSRef();
            container.releaseComponent(schedulername);
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    public void setArrayInUse(String arrayName){
        try {
            getMSRef();
            masterScheduler.setArrayInUse(arrayName);
            releaseMSRef();
        }catch(Exception e){
        }
    }

    public void startInteractiveSession(){
        try {
            scheduler.startSession("","");
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    /*
    public void releaseArray(String array) {
        try {
            getMSRef();
            releaseMSRef();
        } catch(Exception e){
            e.printStackTrace();
        }
    }
    */
    public void executeSB(String id) throws SchedulingException {
        try{
            logger.info("IS: Sending sb ("+id+") to be executed");
            currentSBId = id;
            scheduler.executeSB(id);
        }catch( Exception e){
            throw new SchedulingException (e);
        }
    }

    public void stopSB()throws SchedulingException{
        try{
            logger.info("IS: Requesting sb to stop");
            scheduler.stopSB();
        }catch( Exception e){
            throw new SchedulingException (e);
        }
    }

    public void receive(XmlStoreNotificationEvent event) {
        //logger.info("IS: got xml update event");
        CheckArchiveEvent processor = new CheckArchiveEvent(event);
        Thread t = new Thread(processor);
        t.start();
    }

    public void receive(ExecBlockEndedEvent e){
        String exec_id = e.execId.entityId;
        String sbid = e.sbId.entityId;
        logger.info("SCHEDULING_PANEL: SB("+sbid+")'s exec block("+exec_id+") ended");
        if(currentSBId.equals(sbid) ){
            currentExecBlockId = exec_id;
        }
        String completion;
        switch(e.status.value()) {
            case 0:
                completion ="FAILED";
                break;
            case 1:
                completion ="SUCCESS";
                break;
            case 2:
                completion ="PARTIAL";
                break;
            case 3:
                completion ="TIMEOUT";
                break;
            default:
                completion ="ERROR";
                break;
        }
        parent.setSBStatus(sbid, completion);
    }
    public void receive(ASDMArchivedEvent e){
        logger.info("SCHEDULING_PANEL: Got asdm archived event for SB("+e.workingDCId.schedBlock.entityId+")'s ASDM("+e.asdmId.entityId+")");
        String asdmId = e.asdmId.entityId;
        String completion = e.status;
        if(currentExecBlockId.equals(asdmId)){
            //ok to re-enable the search area now..
            parent.setEnable(true);
            if(completion.equals("complete")){
                parent.setSBStatus(currentSBId, "ARCHIVED");
            }
            //set status to ARCHIVED
        }
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
