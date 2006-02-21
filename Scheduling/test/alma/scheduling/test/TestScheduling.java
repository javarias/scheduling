package alma.scheduling.test;

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
import alma.acs.container.ContainerServices;
import alma.acs.component.client.ComponentClient;
import alma.acs.nc.Consumer;
import alma.scheduling.NothingCanBeScheduledEvent;
import alma.scheduling.NothingCanBeScheduledEnum;
import alma.scheduling.MasterSchedulerIF;
import alma.scheduling.AlmaScheduling.ALMAMasterScheduler;

import java.util.logging.Logger;

public class TestScheduling {
    private boolean exitFlag = false;

    public TestScheduling() {
    }

    public void receive(NothingCanBeScheduledEvent event) {
        if(event.reason == NothingCanBeScheduledEnum.OTHER) {
            System.out.println("Got event with correct reason.");
            exitFlag = true;
        } else {
            System.out.println("Got event with wrong reason.. but still nothing being scheduled.");
            exitFlag = true;
        }
    }

    public boolean getExitFlag() {
        return exitFlag;
    }
    public static void main(String[] args){
        ContainerServices cs=null;
        try{
        //get container services
            ComponentClient c = new ComponentClient(null,
                System.getProperty("ACS.manager"), "OPTest1");

            cs = c.getContainerServices();
            Logger logger = cs.getLogger();
        //get control master component
            MasterComponent ctrl_mc = alma.ACS.MasterComponentHelper.narrow(
                    c.getContainerServices().getComponent("CONTROL_MASTER_COMP"));

        //Create listener and semaphore for starting control MC
            ROstringSeq statesProperty = ctrl_mc.currentStateHierarchy();
            StateChangeListener listener = new StateChangeListener(logger);
            listener.createMonitor(statesProperty, cs);
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
        //get the SB we're using for the test and pass it to startQueueScheduling
            String query = new String("/sbl:SchedBlock");
            String schema = new String("SchedBlock");
            alma.xmlstore.ArchiveConnection conn = 
                alma.xmlstore.ArchiveConnectionHelper.narrow(
                        cs.getComponent("ARCHIVE_CONNECTION"));
            conn.getAdministrative("SCHEDULING").init();
            alma.xmlstore.Operational archive = conn.getOperational("SCHEDULING");
            alma.xmlstore.Cursor cursor = archive.queryDirty(query,schema);
            alma.xmlstore.CursorPackage.QueryResult res;
            java.util.Vector sb_ids = new java.util.Vector();
            while( cursor.hasNext() ){
                res = cursor.next();
                sb_ids.add(res.identifier);
            }
            //ids is the array of SB ids which will be scheduled.
            String[] ids = new String[sb_ids.size()];
            for(int i=0; i < sb_ids.size(); i++) {
                ids[i] = (String)sb_ids.elementAt(i);
		System.out.println("sb id to schedule = "+ids[i]);
            }

        //get scheduling MasterSchedulerIF
            MasterSchedulerIF ms = alma.scheduling.MasterSchedulerIFHelper.narrow(
                cs.getComponent("SCHEDULING_MASTERSCHEDULER")); 
        // create a consumer for the NothingCanBeScheduledEvent
            TestScheduling test = new TestScheduling();
            Consumer test_consumer = new Consumer(alma.scheduling.CHANNELNAME_SCHEDULING.value, cs);
            test_consumer.addSubscription(NothingCanBeScheduledEvent.class, test);
            test_consumer.consumerReady();
        // call startQueueScheduling    
            ms.startQueueScheduling(ids);
        // now wait for the nothing can be scheduled event so we know everything is done before we exit.
            try {
                while(true) {
                    Thread.sleep(5000);
                    if(test.getExitFlag()) break;
                }
            } catch(Exception ex) {}
            test_consumer.disconnect();
            cs.releaseComponent("SCHEDULING_MASTERSCHEDULER");
	// shutdown control MC
            logger.info("SHUTDOWNPASS1");
            ctrl_mc.doTransition(SubsystemStateEvent.SUBSYSEVENT_SHUTDOWNPASS1);
            sync.waitForStateChanges(2);
            logger.info("SHUTDOWNPASS2");
            sync.reset();
            ctrl_mc.doTransition(SubsystemStateEvent.SUBSYSEVENT_SHUTDOWNPASS2);
            sync.waitForStateChanges(2);
            cs.releaseComponent("CONTROL_MASTER_COMP");
        } catch(Exception e) {
            if(cs != null) {
                try {
                    cs.releaseComponent("CONTROL_MASTER_COMP");
                }catch(Exception ex) {
                    return;
                }
                try {
                    cs.releaseComponent("SCHEDULING_MASTERSCHEDULER");
                }catch(Exception ex) {
                    return;
                }
            }
            e.printStackTrace();
            return;
        }
    }
}
