package alma.scheduling.test;
//java
import java.util.logging.Logger;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.framework.Test;
//import java.io.IOException;
//import java.io.BufferedReader;
//import java.io.File;
//import java.io.FileReader;
//acs & alma
import alma.acs.component.client.ComponentClientTestCase;
import alma.acs.component.client.ComponentClient;
import alma.acs.container.ContainerException;
import alma.acs.container.ContainerServices;
import alma.ACS.MasterComponent;
import alma.ACS.ROstringSeq;
import alma.ACS.SUBSYSSTATE_AVAILABLE;
import alma.ACS.SUBSYSSTATE_INITIALIZING_PASS1;
import alma.ACS.SUBSYSSTATE_INITIALIZING_PASS2;
import alma.ACS.SUBSYSSTATE_OFFLINE;
import alma.ACS.SUBSYSSTATE_ONLINE;
import alma.ACS.SUBSYSSTATE_PREINITIALIZED;
import alma.ACS.SUBSYSSTATE_OPERATIONAL;
import alma.ACS.SUBSYSSTATE_PRESHUTDOWN;
import alma.ACS.SUBSYSSTATE_SHUTDOWN;
import alma.ACS.MasterComponentPackage.SubsystemStateEvent;
import alma.ACS.MasterComponentImpl.StateChangeSemaphore;
import alma.ACS.MasterComponentImpl.StateChangeListener;
import alma.acs.nc.Consumer;
import alma.scheduling.NothingCanBeScheduledEvent;
import alma.scheduling.NothingCanBeScheduledEnum;
import alma.scheduling.SBLite;
import alma.scheduling.MasterSchedulerIF;
import alma.scheduling.AlmaScheduling.ALMAMasterScheduler;

//import alma.xmlstore.ArchiveConnection;
//import alma.xmlstore.Operational;
//import alma.xmlstore.Identifier;
//import alma.xmlentity.XmlEntityStruct;

public class EndToEndTest extends ComponentClientTestCase {

    private Logger logger=null;
    private boolean exitFlag = false;

    public EndToEndTest() throws Exception {
        super(EndToEndTest.class.getName());
    }

    protected void setUp() throws Exception {
        super.setUp();
        logger = getContainerServices().getLogger();
        assertNotNull(logger);
        logger.info("EndToEndTest: Setup complete");
    }

    protected void tearDown() throws Exception {
    }

    public void receive(NothingCanBeScheduledEvent event) {
        if(event.reason == NothingCanBeScheduledEnum.OTHER) {
            logger.info("Got event with correct reason.");
            exitFlag = true;
        } else {
            logger.info("Got event with wrong reason.. but still nothing being scheduled.");
            exitFlag = true;
        }
    }
        
    public void testEndToEnd() throws Exception {
        try{
        //get control master component
            MasterComponent ctrl_mc = alma.ACS.MasterComponentHelper.narrow(
                    getContainerServices().getComponent("CONTROL_MASTER_COMP"));

            assertNotNull(ctrl_mc);
        //Create listener and semaphore for starting control MC
            ROstringSeq statesProperty = ctrl_mc.currentStateHierarchy();
            StateChangeListener listener = new StateChangeListener(logger);
            listener.createMonitor(statesProperty, getContainerServices());
            StateChangeSemaphore sync = listener.getStateChangeSemaphore();
            sync.reset();
        //initialize control MC
            logger.info("INITPASS1");
            ctrl_mc.doTransition(SubsystemStateEvent.SUBSYSEVENT_INITPASS1);
            sync.waitForStateChanges(2);
            logger.info("INITPASS2");
            sync.reset();
            ctrl_mc.doTransition(SubsystemStateEvent.SUBSYSEVENT_INITPASS2);
            sync.waitForStateChanges(2);
        //get scheduling MasterSchedulerIF
            MasterSchedulerIF ms = alma.scheduling.MasterSchedulerIFHelper.narrow(
                getContainerServices().getComponent("SCHEDULING_MASTERSCHEDULER")); 
        // create a consumer for the NothingCanBeScheduledEvent
            Consumer test_consumer = new Consumer(alma.scheduling.CHANNELNAME_SCHEDULING.value, getContainerServices());
            test_consumer.addSubscription(NothingCanBeScheduledEvent.class, this);
            test_consumer.consumerReady();
        // get SBLites and get the Ids
            SBLite[] sbs = ms.getSBLites();
            String[] ids= new String[sbs.length];
            for(int i=0; i < sbs.length; i++){
                ids[i] = sbs[i].schedBlockRef;
  //              logger.info("SB id = "+ids[i]);
            }
            assertTrue(ids.length > 0);
        // call startQueueScheduling    
            ms.startQueueScheduling(ids);
        // now wait for the nothing can be scheduled event so we know everything is done before we exit.
            try {
                while(true) {
                    Thread.sleep(5000);
                    if(exitFlag) break;
                }
            } catch(Exception ex) {}
            test_consumer.disconnect();
            getContainerServices().releaseComponent("SCHEDULING_MASTERSCHEDULER");
	// shutdown control MC
            logger.info("SHUTDOWNPASS1");
            ctrl_mc.doTransition(SubsystemStateEvent.SUBSYSEVENT_SHUTDOWNPASS1);
            sync.waitForStateChanges(2);
            logger.info("SHUTDOWNPASS2");
            sync.reset();
            ctrl_mc.doTransition(SubsystemStateEvent.SUBSYSEVENT_SHUTDOWNPASS2);
            sync.waitForStateChanges(2);
            getContainerServices().releaseComponent("CONTROL_MASTER_COMP");
        } catch(Exception e) {
            if(getContainerServices() != null) {
                try {
                    getContainerServices().releaseComponent("CONTROL_MASTER_COMP");
                }catch(Exception ex) {
                    return;
                }
                try {
                    getContainerServices().releaseComponent("SCHEDULING_MASTERSCHEDULER");
                }catch(Exception ex) {
                    return;
                }
            }
            e.printStackTrace();
            return;
        }
    }
    
}

