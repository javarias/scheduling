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

    /**
      * Get SBLite for given sb id
      * @param id SB Id
      * @return SBLite 
      */
    public SBLite getSBLite(String id){
        getMSRef();
        String[] uid = new String[1];
        uid[0] = id;
        SBLite[] sb = masterScheduler.getSBLite(uid);
        releaseMSRef();
        return sb[0];
    }

    /**
      * Get ProjectLite for the project id 
      * @param id Project Id
      * @return ProjectLite[] Will only be one 
      */
    public ProjectLite[] getProjectLite(String id){
        getMSRef();
        String[] uid = new String[1];
        uid[0] = id;
        ProjectLite[] p = masterScheduler.getProjectLites(uid);
        releaseMSRef();
        return p;
    }
}
