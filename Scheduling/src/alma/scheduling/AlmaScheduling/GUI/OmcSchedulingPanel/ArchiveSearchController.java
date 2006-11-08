package alma.scheduling.AlmaScheduling.GUI.OmcSchedulingPanel;

import java.awt.*;
import java.util.EventListener;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import java.util.logging.Logger;
import alma.exec.extension.subsystemplugin.PluginContainerServices;
import alma.scheduling.MasterSchedulerIF;

public class ArchiveSearchController {
    private PluginContainerServices container;
    private MasterSchedulerIF masterScheduler = null;
    private Logger logger;
    
    public ArchiveSearchController(PluginContainerServices cs) {
        container = cs;
        logger = cs.getLogger();
    }

    private void getMSRef() {
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
    
    private void releaseMSRef(){
        try {
            if(masterScheduler != null){
                container.releaseComponent(masterScheduler.name());
                
            }
        } catch(Exception e){
            e.printStackTrace();
            logger.severe("SP_ARCHIVE_CONTROLLER: Error releasing MS: "+e.toString());
        }
    }

    public String[] doSBQuery(String query) {
        getMSRef();
        String[] tmp =new String[0];
        try {
            tmp = masterScheduler.queryArchive(query,"SchedBlock");
        }catch (Exception e){
            logger.severe("SP_ARCHIVE_CONTROLLER: Error doing sb query: "+e.toString()); 
            tmp[0] = e.toString();
            e.printStackTrace();
        }
        releaseMSRef();
        return tmp;
    }

    public String[] doProjectQuery(String query){
        getMSRef();
        String[] tmp =new String[0];
        try {
            tmp = masterScheduler.queryArchive(query,"ObsProject");
        }catch (Exception e){
            logger.severe("SP_ARCHIVE_CONTROLLER: Error doing project query: "+e.toString()); 
            tmp[0] = e.toString();
            e.printStackTrace();
        }
        releaseMSRef();
        return tmp;
    }

}
