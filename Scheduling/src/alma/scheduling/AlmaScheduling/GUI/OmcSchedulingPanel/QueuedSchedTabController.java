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
    
    public QueuedSchedTabController(PluginContainerServices cs, QueuedSchedTab p, String a){
        super(cs);
        parent = p;
        arrayName = a;
    }


}
