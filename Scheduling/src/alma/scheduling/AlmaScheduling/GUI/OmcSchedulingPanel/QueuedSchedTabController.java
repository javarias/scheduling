package alma.scheduling.AlmaScheduling.GUI.OmcSchedulingPanel;

import java.util.logging.Logger;
import alma.scheduling.SBLite;
import alma.scheduling.ProjectLite;
import alma.scheduling.MasterSchedulerIF;
import alma.scheduling.Interactive_PI_to_Scheduling;
import alma.scheduling.Define.*;
import alma.acs.nc.Consumer;

import alma.Control.ExecBlockEndedEvent;
import alma.offline.ASDMArchivedEvent;
import alma.xmlstore.XmlStoreNotificationEvent;
import alma.exec.extension.subsystemplugin.PluginContainerServices;

public class QueuedSchedTabController extends SchedulingPanelController {
    private QueuedSchedTab parent;
    private String arrayName;
    private Thread thread;
    private RunQueuedScheduling run;
    
    public QueuedSchedTabController(PluginContainerServices cs, QueuedSchedTab p, String a){
        super(cs);
        parent = p;
        arrayName = a;
    }

    public SBLite[] getSBLites(String[] ids){
        getMSRef();
        SBLite[] sbs = masterScheduler.getSBLite(ids);
        releaseMSRef();
        return sbs;
    }

    public void runQueuedScheduling(String[] sb_ids){
        try {
            run = new RunQueuedScheduling(
                    container, sb_ids, arrayName);
            thread = new Thread(run);
            thread.start();
        } catch(Exception e){
            e.printStackTrace();
        }

    }
            

}
