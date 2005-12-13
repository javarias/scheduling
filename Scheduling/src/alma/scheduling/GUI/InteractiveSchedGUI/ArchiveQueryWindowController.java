
package alma.scheduling.GUI.InteractiveSchedGUI;

import alma.entity.xmlbinding.obsproject.*;
import alma.scheduling.Define.SB;
import alma.scheduling.Define.SBQueue;
import alma.scheduling.Define.ProjectManager;
import alma.scheduling.Define.SchedulingException;
import alma.scheduling.AlmaScheduling.ALMAArchive;
import alma.scheduling.Scheduler.SchedulerConfiguration;
import alma.acs.container.ContainerServices;
import alma.scheduling.AlmaScheduling.ALMAOperator;
import alma.scheduling.AlmaScheduling.ALMAClock;
import alma.scheduling.Event.Publishers.PublishEvent;

public class ArchiveQueryWindowController implements Runnable {

    private ProjectManager manager;
    private ArchiveQueryWindow gui;
    private String[] queryResults;
    private ContainerServices containerServices;
    private SchedulerConfiguration config;
    private GUIController loggedInController;

    /////// CONSTRUCTORS ///////
    public ArchiveQueryWindowController(){
        gui = new ArchiveQueryWindow();
    }
    
    public ArchiveQueryWindowController(ProjectManager m) {
        this.manager = m;
    }

    public ArchiveQueryWindowController(SchedulerConfiguration c, 
                                        ContainerServices cs){
        this.config = c;
        this.manager = c.getProjectManager();
        this.containerServices = cs;
    }

    ////////////////////////////

    public String[] queryProjectAndPi(String name, String pi) {
        String projectStr, piStr;
        if(name.equals("")){
            projectStr = "*";
        }
        if(pi.equals("")){
            piStr ="*";
        }
        String schema = new String("ObsProject");
        String query = new String("/prj:ObsProject[prj:pI[\""+pi+
                "\"] and prj:projectName [\""+name+"\"]]");
        try {
            //project manager's func will be archiveQuery(query, schema);
            //queryResults = archive.query(query, schema);
            queryResults = manager.archiveQuery(query, schema);
            /*
            System.out.println(results.length);
            for(int i=0; i < results.length; i++){
                System.out.println(results[i]);
            }*/
        } catch(Exception e) {
            queryResults[0] = new String(e.toString());
        }
        return queryResults;
    }

    public ObsProject retrieveProject(String uid){
        try{
            //project manager's func will be archiveRetrieve(uid)
            //return archive.retrieve(uid);
            return (ObsProject)manager.archiveRetrieve(uid);
        }catch(Exception e){
            return null;
        }
    }

    public void stopArchive() throws SchedulingException {
        //project manager's func will be archiveReleaseComponents
        //archive.releaseArchiveComponents();
        manager.archiveReleaseComponents();
    }

    public void run(){
        ArchiveQueryWindow archiveQueryGui = new ArchiveQueryWindow(this);
    }

    public void loginToInteractiveProject(String projectId, String pi) {
        try {
            //need to get SBs from project to add to empty SBQueue
            //SB[] sbs = archive.getSBsForProject(projectId);
            SB[] sbs = manager.getSBsForProject(projectId);
            for(int i=0; i < sbs.length; i++){
                sbs[i].setType(SB.INTERACTIVE);
            }
            config.getQueue().add(sbs);

            loggedInController = new GUIController(config, containerServices);
            Thread t = containerServices.getThreadFactory().newThread(loggedInController);
            t.start();
            loggedInController.setLogin(pi);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args){
        try {
            alma.acs.component.client.ComponentClient c = 
                new alma.acs.component.client.ComponentClient(null, System.getProperty("ACS.manager"), "test");
            alma.scheduling.AlmaScheduling.ALMAProjectManager m = 
                
                new alma.scheduling.AlmaScheduling.ALMAProjectManager(
                        c.getContainerServices(), 
                        (ALMAOperator)null, 
                        new ALMAArchive(c.getContainerServices(), new ALMAClock()),
                        new SBQueue(),
                        (PublishEvent)null, 
                        (ALMAClock)null);

            ArchiveQueryWindowController ctrl = new ArchiveQueryWindowController(m);
            ctrl.run();
            
        } catch(Exception e){}

    }
}
