
package alma.scheduling.GUI.InteractiveSchedGUI;

import alma.scheduling.AlmaScheduling.ALMAArchive;
import alma.entity.xmlbinding.obsproject.*;

public class ArchiveQueryWindowController implements Runnable {

    private ALMAArchive archive;
    private ArchiveQueryWindow gui;
    private String[] queryResults;

    public ArchiveQueryWindowController(){
        gui = new ArchiveQueryWindow();
    }
    
    public ArchiveQueryWindowController(ALMAArchive a) {
        this.archive = a;
    }

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
