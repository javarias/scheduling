
package alma.scheduling.GUI.InteractiveSchedGUI;

import alma.entity.xmlbinding.obsproject.*;
import alma.scheduling.Define.SB;
import alma.scheduling.AlmaScheduling.ALMAArchive;
import alma.scheduling.Scheduler.SchedulerConfiguration;
import alma.acs.container.ContainerServices;

public class ArchiveQueryWindowController implements Runnable {

    private ALMAArchive archive;
    private ArchiveQueryWindow gui;
    private String[] queryResults;
    private ContainerServices containerServices;
    private SchedulerConfiguration config;
    private GUIController loggedInController;

    /////// CONSTRUCTORS ///////
    public ArchiveQueryWindowController(){
        gui = new ArchiveQueryWindow();
    }
    
    public ArchiveQueryWindowController(ALMAArchive a) {
        this.archive = a;
    }

    public ArchiveQueryWindowController(SchedulerConfiguration c, 
                                        ALMAArchive a,
                                        ContainerServices cs){
        this.config = c;
        this.archive = a;
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
            queryResults = archive.query(query, schema);
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
            return archive.retrieve(uid);
        }catch(Exception e){
            return null;
        }
    }

    public void stopArchive() {
        archive.releaseArchiveComponents();
    }

    public void run(){
        ArchiveQueryWindow archiveQuery = new ArchiveQueryWindow(this);
    }

    public void loginToInteractiveProject(String projectId, String pi) {
        try {
            //need to get SBs from project to add to empty SBQueue
            SB[] sbs = archive.getSBsForProject(projectId);
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
            ALMAArchive a = new ALMAArchive(c.getContainerServices(), 
                    new alma.scheduling.AlmaScheduling.ALMAClock());

            ArchiveQueryWindowController ctrl = new ArchiveQueryWindowController(a);
            ctrl.run();
            
        } catch(Exception e){}

    }
}
