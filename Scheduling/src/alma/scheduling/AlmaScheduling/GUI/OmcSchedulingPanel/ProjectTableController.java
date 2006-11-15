package alma.scheduling.AlmaScheduling.GUI.OmcSchedulingPanel;

import alma.exec.extension.subsystemplugin.PluginContainerServices;
import alma.scheduling.SBLite;
import alma.scheduling.ProjectLite;
import alma.scheduling.MasterSchedulerIF;
import java.util.logging.Logger;


public class ProjectTableController extends SchedulingPanelController {

    public ProjectTableController(PluginContainerServices cs){
        super(cs);
    }

    public SBLite[] getProjectSBs(String projectId){
        SBLite[] sbs=null;
        getMSRef();
        releaseMSRef();
        return sbs;
    }

    public ProjectLite getProjectLite(String uid) {
        getMSRef();
        String[] id = new String[1];
        id[0] = uid;
        ProjectLite[] p = masterScheduler.getProjectLites(id);
        releaseMSRef();
        return p[0];
    }
    public SBLite[] getSBLites(String[] uids){
        getMSRef();
        SBLite[] sbs = masterScheduler.getSBLite(uids);
        releaseMSRef();
        return sbs;
    }
}
