package alma.scheduling.AlmaScheduling.GUI.OmcSchedulingPanel;

import java.util.logging.Logger;
import alma.acs.container.ContainerServices;
import alma.exec.extension.subsystemplugin.PluginContainerServices;

import alma.scheduling.MasterSchedulerIF;
import alma.xmlentity.XmlEntityStruct;

public class RunDynamicScheduling implements Runnable {
    //private ContainerServices container;
    private PluginContainerServices container;
    private MasterSchedulerIF masterScheduler = null;
    private String[] sb_ids;
    private String arrayname;
    private Logger logger;
    
    public RunDynamicScheduling(PluginContainerServices cs ){
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
            logger.severe("RUN_DYNAMIC_SCHEDULING: Error in RunDynamicScheduling: "+e.toString());
        }
    }
    private void releaseMSRef(){
        try {
            if(masterScheduler != null){
                container.releaseComponent(masterScheduler.name());
            }
        } catch(Exception e){
            e.printStackTrace();
            logger.severe("RUN_DYNAMIC_SCHEDULING: Error in RunDynamicScheduling: "+e.toString());
        }
    }

    public void run() {
        if(masterScheduler == null) {
            logger.warning("RUN_DYNAMIC_SCHEDULING: NO Connection to MasterScheduler. Cannot schedule");
            return;
        }
        XmlEntityStruct policy = new XmlEntityStruct();
        try {
            masterScheduler.startScheduling(policy);
        } catch(Exception e) {
            releaseMSRef();
            //e.printStackTrace();
            logger.severe("RUN_DYNAMIC_SCHEDULING: Error in RunDynamicScheduling: "+e.toString());
        }
    }
    
    public void stop() {
        try {
            releaseMSRef();
        } catch (Exception e){
            e.printStackTrace();
            logger.severe("RUN_DYNAMIC_SCHEDULING: Error in RunDynamicScheduling: "+e.toString());
        }
    }
}
