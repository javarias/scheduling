package ALMA.scheduling.test;

import org.omg.CosNotification.*;
import alma.acs.nc.*;

import ALMA.scheduling.MS;
import ALMA.scheduling.InvalidOperation;
import ALMA.scheduling.NothingCanBeScheduledEvent;
import ALMA.scheduling.NothingCanBeScheduledEventHelper;

import alma.acs.container.ContainerServices;
import alma.acs.container.ContainerException;

public class TestSchedConsumer extends Consumer {
    private ContainerServices container;
    private MS masterSchedulerComp;

    public TestSchedConsumer(ContainerServices cs) {
        super(ALMA.scheduling.CHANNELNAME.value);
        System.out.println("Got scheduling channel");
        this.container=cs;
        try {
            this.masterSchedulerComp = ALMA.scheduling.MSHelper.narrow(
                container.getComponent("MASTER_SCHEDULER"));
        } catch (ContainerException e) {
        }

    }

    public void push_structured_event(StructuredEvent structuredEvent) 
            throws org.omg.CosEventComm.Disconnected {
        
        try {
            NothingCanBeScheduledEvent event = 
                NothingCanBeScheduledEventHelper.extract(structuredEvent.filterable_data[0].value);
            System.out.println(event.reason);
            stopScheduling();
            //container.releaseComponent("MASTER_SCHEDULER");
            //disconnect();
        } catch (Exception e) {
        }
    }

    public void stopScheduling() {
        try{
            masterSchedulerComp.stopScheduling();
        } catch(InvalidOperation e) {}
        //container.releaseComponent("MASTER_SCHEDULING");
    }
        
    public static void main(String[] args) {
        /*
        TestSchedConsumer consumer = new TestSchedConsumer();
        try {
            consumer.addSubscription(ALMA.acsnc.DEFAULTTYPE.value);
            consumer.consumerReady();
        } catch (Exception e) {
            consumer.disconnect();
            alma.acs.nc.Helper.disconnect();
        }
        */
    }
}

