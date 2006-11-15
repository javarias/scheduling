package alma.scheduling.AlmaScheduling.GUI.OmcSchedulingPanel;

import alma.exec.extension.subsystemplugin.PluginContainerServices;
import alma.scheduling.SBLite;
import alma.scheduling.ProjectLite;
import alma.scheduling.MasterSchedulerIF;
import java.util.logging.Logger;

public class SBTableController extends SchedulingPanelController {
    public SBTableController(PluginContainerServices cs){
        super(cs);
    }

    public SBLite getSBLite(String id){
        getMSRef();
        String[] uid = new String[1];
        uid[0] = id;
        SBLite[] sb = masterScheduler.getSBLite(uid);
        releaseMSRef();
        return sb[0];
    }
}
