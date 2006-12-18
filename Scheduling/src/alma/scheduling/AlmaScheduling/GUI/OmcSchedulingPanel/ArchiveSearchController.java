package alma.scheduling.AlmaScheduling.GUI.OmcSchedulingPanel;

import java.util.Vector;
import java.util.logging.Logger;
import alma.exec.extension.subsystemplugin.PluginContainerServices;
import alma.scheduling.SBLite;
import alma.scheduling.ProjectLite;
import alma.scheduling.MasterSchedulerIF;

public class ArchiveSearchController extends SchedulingPanelController {
    
    public ArchiveSearchController(PluginContainerServices cs) {
        super(cs);
    }


    public Vector doQuery(String sbQuery, String pName, String piName, String pType) {
        Vector tmp = new Vector();
        try {
            getMSRef();
            String[] sbs = masterScheduler.queryArchive(sbQuery,"SchedBlock");
            String[] projs = masterScheduler.queryForProject(pName,piName,pType);
            //do union now
            String[] unionSB = masterScheduler.getSBProjectUnion(sbs,projs);
            SBLite[] unionSBLites = masterScheduler.getSBLite(unionSB);
            String[] unionProj = masterScheduler.getProjectSBUnion(projs,sbs);
            ProjectLite[] unionProjectLites = masterScheduler.getProjectLites(unionProj);
            releaseMSRef();
            tmp.add(unionProjectLites);
            tmp.add(unionSBLites);
        }catch (Exception e) {
            logger.severe("SP_ARCHIVE_CONTROLLER: Error doing query: "+e.toString()); 
            e.printStackTrace();
        }
        return tmp;
    }
    
}
