package alma.scheduling.AlmaScheduling.GUI.OmcSchedulingPanel;

import java.util.logging.Logger;
import alma.exec.extension.subsystemplugin.PluginContainerServices;
import alma.scheduling.SBLite;
import alma.scheduling.ProjectLite;
import alma.scheduling.MasterSchedulerIF;
import alma.Control.ControlMaster;
import alma.scheduling.Define.*;
import alma.scheduling.ArrayModeEnum;

public class CreateArrayController extends SchedulingPanelController {

    private ControlMaster control;

    public CreateArrayController(PluginContainerServices cs){
        super(cs);
    }

    private void getControlRef() {
        try {
            control = alma.Control.ControlMasterHelper.narrow(
                    container.getComponent("CONTROL/MASTER"));
            logger.info("SCHEDULING_PANEL: Got control system in array creator");
        } catch(Exception e){
            e.printStackTrace();
            logger.severe("SCHEDULING_PANEL: Error getting components from array creator");
        }
    }
    private void releaseControlRef() {
        try {
            container.releaseComponent(control.name());
        } catch(Exception e) {
            e.printStackTrace();
            logger.severe("SCHEDULING_PANEL: Error releasing components from array creator");
        }
    }

    public String[] getAntennas() {
        String[] antennas=null;
        try{
            getControlRef();
            antennas= control.getAvailableAntennas();
            releaseControlRef();
        } catch(Exception e){
            e.printStackTrace();
        }
        return antennas;
    }

    public String createArray(String arrayMode, String[] antennas) throws SchedulingException {
        String arrayName = null;
        getMSRef();
        try {
            if(arrayMode.toLowerCase().equals("dynamic")){
                arrayName = masterScheduler.createArray(
                        antennas,ArrayModeEnum.DYNAMIC);
            } else if(arrayMode.toLowerCase().equals("interactive")){
                arrayName = masterScheduler.createArray(
                        antennas,ArrayModeEnum.INTERACTIVE);
            } else if(arrayMode.toLowerCase().equals("queued")) {
                arrayName = masterScheduler.createArray(
                        antennas,ArrayModeEnum.QUEUED);
            } else if(arrayMode.toLowerCase().equals("manual")){
                arrayName = masterScheduler.createArray(
                        antennas,ArrayModeEnum.MANUAL);
            }
        } catch(Exception e) {
            releaseMSRef();
            e.printStackTrace();
            throw new SchedulingException (e);
        }
        releaseMSRef();
        return arrayName;
    }
}
