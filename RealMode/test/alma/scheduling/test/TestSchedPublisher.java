package alma.scheduling.test;

import java.util.logging.Logger;

import org.omg.CosNotification.*;

import alma.acs.nc.*;
import alma.acs.container.ContainerServices;
import alma.acs.container.ContainerException;
import alma.acs.component.client.ComponentClient;

import alma.scheduling.NothingCanBeScheduledEvent;
import alma.scheduling.NothingCanBeScheduledEnum;
import alma.scheduling.master_scheduler.SchedulingPublisher;

//public class TestSchedPublisher extends Consumer {
public class TestSchedPublisher {
    private Logger logger;
    private Receiver receiver; 

    public TestSchedPublisher(ContainerServices cs){
//        super(alma.scheduling.CHANNELNAME.value);
        this.logger = cs.getLogger();
    }
/*
    public void push_structured_event(StructuredEvent structuredEvent) 
            throws org.omg.CosEventComm.Disconnected {
        
        logger.info("SCHED_TEST: got event");

    }
*/    
    
    public void receive(NothingCanBeScheduledEvent e) {
        logger.fine("SCHED_TEST: Event Received!");
        int i = e.reason.value();
        switch(i) {
            case 0:
                logger.fine("SCHED_TEST: Reason = NOTHING VISIBLE");
                break;
            case 1:
                logger.fine("SCHED_TEST: Reason = BAD WEATHER");
                break;
            case 2:
                logger.fine("SCHED_TEST: Reason = INSUFFICIENT REASOURCES");
                break;
            case 3:
                logger.fine("SCHED_TEST: Reason = NOT OPTIMAL");
                break;
            case 4:
                logger.fine("SCHED_TEST: Reason = OTHER");
                break;
            default:
                break;
        }
        logger.fine("SCHED_TEST: Reason = "+ e.comment);
        
    }

    public void disconnectReceiver() {
        receiver.detach("alma.scheduling.NothingCanBeScheduledEvent", this);
        receiver.end();
    }

    public void receiverSetup() {
        receiver = CorbaNotificationChannel.getCorbaReceiver(alma.scheduling.CHANNELNAME.value);
        receiver.attach("alma.scheduling.NothingCanBeScheduledEvent", this);
        receiver.begin();
        logger.fine("SCHED_TEST: Receiver setup done.");
    }

    public void stop() {
        logger = null;
        receiver = null;
        System.gc();
    }
    
    public static void main(String[] args) {
        try {
            ComponentClient client = new ComponentClient(null,
                System.getProperty("ACS.manager"), 
                    "Scheduling publisher test");
            ContainerServices cs = client.getContainerServices();
            SchedulingPublisher sp = new SchedulingPublisher(false, cs);
            TestSchedPublisher tsp = new TestSchedPublisher(cs);

            /*
            tsp.addSubscription(alma.scheduling.NothingCanBeScheduledEvent.class);
            tsp.consumerReady();
            */
            
            tsp.receiverSetup();

            sp.publishEvent(NothingCanBeScheduledEnum.NOTHING_VISIBLE,
                "nothing to see!");
            Thread.sleep(2000);
            sp.publishEvent(NothingCanBeScheduledEnum.BAD_WEATHER,
                "Thunder Storms!");
            Thread.sleep(2000);
            sp.publishEvent(NothingCanBeScheduledEnum.INSUFFICIENT_RESOURCES,
                "all antennas in maintence!");
            Thread.sleep(2000);
            sp.publishEvent(NothingCanBeScheduledEnum.NOT_OPTIMAL,
                "wait 2 hours and it'll be better!");
            Thread.sleep(2000);
            sp.publishEvent("Unknown Reason!");

            tsp.disconnectReceiver();
            tsp.stop();
            sp.shutdown();
            alma.acs.nc.Helper.disconnect();
        } catch(Exception e) {
            System.out.println(e.toString());
            e.printStackTrace();
        }
    }
}
