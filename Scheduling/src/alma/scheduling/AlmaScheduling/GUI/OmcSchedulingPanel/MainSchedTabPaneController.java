package alma.scheduling.AlmaScheduling.GUI.OmcSchedulingPanel;

import java.util.logging.Logger;
import alma.Control.ControlMaster;
import alma.scheduling.MasterSchedulerIF;
import alma.exec.extension.subsystemplugin.PluginContainerServices;

public class MainSchedTabPaneController{
    private PluginContainerServices container;
    private Logger logger;
    private MasterSchedulerIF masterScheduler=null;
    private ControlMaster control=null;

    public MainSchedTabPaneController(PluginContainerServices cs){
        container = cs;
        logger = cs.getLogger();
    }

////////////////////////////////////    
    private void getMSRef(){
        try {
            masterScheduler =
                alma.scheduling.MasterSchedulerIFHelper.narrow(
                    container.getDefaultComponent(
                        "IDL:alma/scheduling/MasterSchedulerIF:1.0"));
            logger.info("SCHEDULING_PANEL: Got MS in MainTab");
        } catch (Exception e) {
            //logger.info("SCHEDULING_PANEL: failed to get MS reference, "+e.toString());
            //e.printStackTrace();
            masterScheduler = null;
        }
    }
    private void releaseMSRef(){
        if(masterScheduler != null){
            container.releaseComponent(masterScheduler.name());
            logger.info("SCHEDULING_PANEL: Released MS in MainTab");
            masterScheduler = null;
        }
    }

    private void getControlRef(){
        try {
            control = alma.Control.ControlMasterHelper.narrow(
                container.getComponent("CONTROL/MASTER"));
            logger.info("SCHEDULING_PANEL: Got Control in MainTab");
        } catch(Exception e){
            control = null;
        }
    }

    private void releaseControlRef(){
        if(control != null) {
            container.releaseComponent(masterScheduler.name());
            logger.info("SCHEDULING_PANEL: Released Control in MainTab");
        }
    }
////////////////////////////////////    

    public String[] getAvailableAntennas(){
        String[] res = new String[1];
        try {
            res = control.getAvailableAntennas();
        } catch(Exception e){
            res[0] = new String("Problem getting antennas from control: "+e.toString());
            logger.severe("SCHEDULING_PANEL: Problem getting antennas from control - "+e.toString());
            e.printStackTrace();
        }
        return res;
    }
}
