package alma.scheduling.AlmaScheduling;
import alma.acs.container.ContainerServices;
import alma.scheduling.Define.SBQueue;
import alma.scheduling.MasterScheduler.MessageQueue;

public class ALMASchedulingUtility {

    private static boolean isInitialized = false;

    private static ContainerServices container=null;
    private static ALMAArchive archive = null;
    private static ALMAClock clock = null;
    private static ALMAControl control = null;
    private static ALMAOperator operator =null;
    private static ALMAProjectManager manager = null;
    private static ALMAPublishEvent publisher = null;
    private static SBQueue sbQueue = null;
    private static MessageQueue messageQueue=null;
    private static Thread masterSchedulerThread=null;
    private static ALMATelescope telescope=null;

    private ALMASchedulingUtility(){
    }

    private static void initialize(ContainerServices cs) {
        container = cs;
        clock = new ALMAClock();
        archive = new ALMAArchive(container,clock);
        messageQueue = new MessageQueue();
        operator = new ALMAOperator(container,messageQueue);
        sbQueue = new SBQueue();
        publisher = new ALMAPublishEvent(container);
        manager = new ALMAProjectManager(container, operator, archive, sbQueue, publisher, clock); 
        control = new ALMAControl(container, manager);
        telescope = new ALMATelescope();
        isInitialized = true;
    }
    
    public static void setMasterSchedulerThread(Thread ms){
        masterSchedulerThread = ms;
    }

    public static Thread getMasterSchedulerThread(){
        return masterSchedulerThread;
    }
    public static ALMAArchive getArchive(ContainerServices cs){
        if(!isInitialized){
            initialize(cs);
        }
        return archive;
    }
    public static ALMAClock getClock(ContainerServices cs){
        if(!isInitialized){
            initialize(cs);
        }
        return clock;

    }
    public static ALMAControl getControl(ContainerServices cs){
        if(!isInitialized){
            initialize(cs);
        }
        return control;
    }
    public static MessageQueue getMessageQueue(ContainerServices cs){
        if(!isInitialized){
            initialize(cs);
        }
        return messageQueue;
    }
    public static ALMAOperator getOperator(ContainerServices cs){
        if(!isInitialized){
            initialize(cs);
        }
        return operator;
    }
    public static SBQueue getSBQueue(ContainerServices cs){
        if(!isInitialized){
            initialize(cs);
        }
        return sbQueue;
    }
    public static ALMAProjectManager getProjectManager(ContainerServices cs) {
        if(!isInitialized){
            initialize(cs);
        }
        return manager;  
    }
    public static ALMAPublishEvent getPublisher(ContainerServices cs){
        if(!isInitialized){
            initialize(cs);
        }
        return publisher;
    }
    public static ALMATelescope getTelescope(ContainerServices cs){
        if(!isInitialized){
            initialize(cs);
        }
        return telescope;
    }
        
}
