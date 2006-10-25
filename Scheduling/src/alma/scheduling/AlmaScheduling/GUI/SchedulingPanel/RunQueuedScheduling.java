package alma.scheduling.AlmaScheduling.GUI.SchedulingPanel;

import java.util.logging.Logger;
import alma.acs.container.ContainerServices;
import alma.exec.extension.subsystemplugin.PluginContainerServices;

import alma.scheduling.MasterSchedulerIF;

public class RunQueuedScheduling implements Runnable {
    //private ContainerServices container;
    private PluginContainerServices container;
    private MasterSchedulerIF masterScheduler = null;
    private String[] sb_ids;
    private String arrayname;
    private Logger logger;
    
    public RunQueuedScheduling(PluginContainerServices cs, String[] ids, String array){
    //public RunQueuedScheduling(ContainerServices cs, String[] ids, String array){
        sb_ids = ids;
        arrayname = array;
        container = cs;
        logger =cs.getLogger();
        getMSRef();
    }
    private void getMSRef(){
        try {
            if(masterScheduler == null) {
                masterScheduler = alma.scheduling.MasterSchedulerIFHelper.narrow(
                    container.getDefaultComponent(
                        "IDL:alma/scheduling/MasterSchedulerIF:1.0"));
            }
        } catch(Exception e){
            e.printStackTrace();
            logger.severe("RUN_QUEUED_SCHEDULING: Error in RunQueuedScheduling: "+e.toString());
        }
    }
    private void releaseMSRef(){
        try {
            if(masterScheduler != null){
                container.releaseComponent(masterScheduler.name());
                
            }
        } catch(Exception e){
            e.printStackTrace();
            logger.severe("RUN_QUEUED_SCHEDULING: Error in RunQueuedScheduling: "+e.toString());
        }
    }

    public void run() {
        if(masterScheduler == null) {
            logger.warning("RUN_QUEUED_SCHEDULING: NO Connection to MasterScheduler. Cannot schedule");
            return;
        }
        try {
            masterScheduler.startQueuedScheduling(sb_ids, arrayname);
        } catch(Exception e) {
            releaseMSRef();
            //e.printStackTrace();
            logger.severe("RUN_QUEUED_SCHEDULING: Error in RunQueuedScheduling: "+e.toString());
        }
    }
    
    public void stop() {
        try {
            releaseMSRef();
        } catch (Exception e){
            e.printStackTrace();
            logger.severe("RUN_QUEUED_SCHEDULING: Error in RunQueuedScheduling: "+e.toString());
        }
    }
}
