package alma.scheduling.AlmaScheduling.GUI.OmcSchedulingPanel;

import alma.exec.extension.subsystemplugin.PluginContainerServices;
import alma.scheduling.SBLite;
import alma.scheduling.ProjectLite;
import alma.scheduling.MasterSchedulerIF;
import java.util.logging.Logger;

public class SchedulingPanelController {
    protected MasterSchedulerIF masterScheduler;
    protected PluginContainerServices container;
    protected Logger logger;

    public SchedulingPanelController(PluginContainerServices cs) {
        container = cs;
        logger = cs.getLogger();
    }

    protected void getMSRef() {
        try {
            if(masterScheduler == null) {
                masterScheduler = alma.scheduling.MasterSchedulerIFHelper.narrow(
                    container.getDefaultComponent(
                        "IDL:alma/scheduling/MasterSchedulerIF:1.0"));
            }
        } catch(Exception e){
            e.printStackTrace();
            logger.severe("SP_ARCHIVE_CONTROLLER: Error getting MS: "+e.toString()); 
        }
    }
    
    
    protected void releaseMSRef(){
        try {
            if(masterScheduler != null){
                container.releaseComponent(masterScheduler.name());
            }
        } catch(Exception e){
            e.printStackTrace();
            logger.severe("SP_ARCHIVE_CONTROLLER: Error releasing MS: "+e.toString());
        }
    }


}

