package alma.scheduling.test;

import alma.acs.component.client.ComponentClient;
import alma.acs.logging.AcsLogger;
import alma.scheduling.ProjectLite;
import alma.scheduling.SBLite;
import alma.scheduling.AlmaScheduling.ALMAArchivePoller;
import alma.scheduling.Define.SchedulingException;

public class ArchivePollerClient extends ComponentClient {

    ALMAArchivePoller archivePoller;
    
    public ArchivePollerClient(AcsLogger logger, String managerLoc, String clientName) throws Exception {
        super(logger, managerLoc, clientName);
        archivePoller = new ALMAArchivePoller(getContainerServices());
    }

    public void pollArchive(String prjuid) throws SchedulingException {
        archivePoller.pollArchive(prjuid);
    }
    
    public ProjectLite[] getProjectLites() {
        return archivePoller.getProjectLites();
    }
    
    public SBLite[] getSbLites() {
        return archivePoller.getExistingSBLites();
    }
    
    public static void main(String[] args) {
        String managerLoc = System.getProperty("ACS.manager");
        if (managerLoc == null) {
            System.out.println("Java property 'ACS.manager must be set to the corbaloc of the ACS manager!");
            System.exit(-1);
        }
        String clientName = "ArchivePollerClient";
        ArchivePollerClient poller = null;
        try {
            poller = new ArchivePollerClient(null, managerLoc, clientName);
            System.out.println("calling pollArchive");
            String uid = null;
            if (args.length != 0) uid = args[0];
            poller.pollArchive(uid);
//            System.out.println("press a key to continue...");
//            System.in.read();
//            System.out.println("calling pollArchive a second time");
//            poller.pollArchive(uid);
            System.out.println("calling getProjectLites");
            poller.getProjectLites();
            System.out.println("calling getSbLites");
            poller.getSbLites();
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
        } finally {
            if (poller != null) {
                try {
                    poller.tearDown();
                } catch (Exception ex) {
                    ex.printStackTrace(System.err);
                }
            }
        }
    }    
}
