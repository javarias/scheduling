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

    public SchedulingPanelController(){
        masterScheduler=null;
        container=null;
        logger=null;
    }

    public SchedulingPanelController(PluginContainerServices cs) {
        container = cs;
        logger = cs.getLogger();
    }
    public void onlineSetup(PluginContainerServices cs) {
        container = cs;
        logger = cs.getLogger();
    }
    protected void getMSRef() {
        try {
            if(masterScheduler == null) {
                masterScheduler = alma.scheduling.MasterSchedulerIFHelper.narrow(
                    container.getDefaultComponent(
                        "IDL:alma/scheduling/MasterSchedulerIF:1.0"));
                logger.info("SCHEDULING_PANEL: Got MS");
            }
        } catch(Exception e){
            e.printStackTrace();
            logger.severe("SCHEDULING_PANEL: Error getting MS: "+e.toString()); 
        }
    }
    
    
    protected void releaseMSRef(){
        try {
            if(masterScheduler != null){
                container.releaseComponent(masterScheduler.name());
                logger.info("SCHEDULING_PANEL: Released MS.");
            }
        } catch(Exception e){
            e.printStackTrace();
            logger.severe("SCHEDULING_PANEL: Error releasing MS: "+e.toString());
        }
    }


}

