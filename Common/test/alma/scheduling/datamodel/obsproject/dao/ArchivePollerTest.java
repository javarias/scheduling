package alma.scheduling.datamodel.obsproject.dao;

import java.util.logging.Logger;

import junit.framework.TestCase;
import alma.scheduling.archive.ArchivePoller;
import alma.scheduling.utils.LoggerFactory;

public class ArchivePollerTest extends TestCase {

    private static Logger logger = LoggerFactory.getLogger(ArchiveObsProjectDaoTest.class);
    
    public ArchivePollerTest(String name) {
        super(name);
    }

    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }
    
    public void testArchivePoller() throws Exception {
    	ArchivePoller ap = new ArchivePoller(logger);
    	boolean keepGoing = true;
    	
//    	while (keepGoing) {
    		ap.pollArchive();
//    		int r = javax.swing.JDialog.showConfirmDialog(
//    				null, "Archive Poll has run. Do more?",
//    				"Keep Polling", javax.swing.JOptionPane.YES_NO_OPTION);
//    		keepGoing = (r == javax.swing.JOptionPane.YES_OPTION);
//     	}
    }
}
