package alma.scheduling.AlmaScheduling.GUI.OmcSchedulingPanel;

import java.util.Vector;
import java.awt.*;
import java.util.EventListener;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import java.util.logging.Logger;
import alma.exec.extension.subsystemplugin.PluginContainerServices;
import alma.scheduling.SBLite;
import alma.scheduling.ProjectLite;
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

    /**
      * Return an array of SBLites that match the query
      */
    //public SBLite[] doSBQuery(String query) {
    private String[] doSBQuery(String query) {
        getMSRef();
        String[] tmp =new String[0];
        //SBLite[] sbs=new SBLite[0];
        try {
            tmp = masterScheduler.queryArchive(query,"SchedBlock");
            //sbs = masterScheduler.getSBLite(tmp);
        }catch (Exception e){
            logger.severe("SP_ARCHIVE_CONTROLLER: Error doing sb query: "+e.toString()); 
            tmp[0] = e.toString();
            e.printStackTrace();
        }
        releaseMSRef();
        //return sbs;
        return tmp;
    }

    /**
      * Return an array of ProjectLites that match the query
      */
    private String[] doProjectQuery(String pName, String piName, String pType) {
    //public ProjectLite[] doProjectQuery(String pName, String piName, String pType) {
        getMSRef();
        String[] tmp =new String[0];
        //ProjectLite[] projects= new ProjectLite[0];
        try {
            tmp = masterScheduler.queryForProject(pName,piName,pType);
           // projects = masterScheduler.getProjectLites(tmp);
        }catch (Exception e){
            logger.severe("SP_ARCHIVE_CONTROLLER: Error doing project query: "+e.toString()); 
            tmp[0] = e.toString();
            e.printStackTrace();
        }
        releaseMSRef();
        return tmp;
        //return projects;
    }

    public Vector<String[]> doQuery(String sbQuery, String pName, String piName, String pType) {
        Vector<String[]> tmp = new Vector<String[]>();
        try {
            getMSRef();
            String[] sbs = masterScheduler.queryArchive(sbQuery,"SchedBlock");
                //doSBQuery(sbQuery);
            String[] projs = masterScheduler.queryForProject(pName,piName,pType);
                //doProjectQuery(pName,piName, pType);
            //do union now
            String[] unionSB = masterScheduler.getSBProjectUnion(sbs,projs);
            String[] unionProj = masterScheduler.getProjectSBUnion(projs,sbs);
            releaseMSRef();
            tmp.add(unionSB);
            tmp.add(unionProj);
        }catch (Exception e) {
            logger.severe("SP_ARCHIVE_CONTROLLER: Error doing query: "+e.toString()); 
            e.printStackTrace();
        }
        return tmp;
        
    }
    
}
