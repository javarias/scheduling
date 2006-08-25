package alma.scheduling.AlmaScheduling.GUI.SchedulingPanel;

import java.util.logging.Logger;
//import alma.acs.container.ContainerServices;
import alma.exec.extension.subsystemplugin.PluginContainerServices;

import alma.scheduling.MasterSchedulerIF;

public class RunQueuedScheduling implements Runnable {
    private PluginContainerServices container;
    private MasterSchedulerIF masterScheduler = null;
    private String[] sb_ids;
    private String arrayname;
    private Logger logger;
    
    public RunQueuedScheduling(PluginContainerServices cs, String[] ids, String array){
        sb_ids = ids;
        arrayname = array;
        container = cs;
        logger =cs.getLogger();
        getMS();
    }
    private void  getMS(){
        try {
            if(masterScheduler == null) {
                masterScheduler = alma.scheduling.MasterSchedulerIFHelper.narrow(
                    container.getDefaultComponent(
                        "IDL:alma/scheduling/MasterSchedulerIF:1.0"));
            }
        } catch(Exception e){
            e.printStackTrace();
            logger.severe("Error in RunQueuedScheduling: "+e.toString());
        }
    }
    private void releaseMS(){
        try {
            if(masterScheduler != null){
                container.releaseComponent(masterScheduler.name());
                
            }
        } catch(Exception e){
            e.printStackTrace();
            logger.severe("Error in RunQueuedScheduling: "+e.toString());
        }
    }

    public void run() {
        if(masterScheduler == null) {
            System.out.println("NO Connection to MasterScheduler. Cannot schedule");
            return;
        }
        try {
            masterScheduler.startQueuedScheduling(sb_ids, arrayname);
        } catch(Exception e) {
            releaseMS();
            e.printStackTrace();
            logger.severe("Error in RunQueuedScheduling: "+e.toString());
        }
    }
    
    public void stop() {
        try {
            releaseMS();
        } catch (Exception e){
            e.printStackTrace();
            logger.severe("Error in RunQueuedScheduling: "+e.toString());
        }
    }
}
