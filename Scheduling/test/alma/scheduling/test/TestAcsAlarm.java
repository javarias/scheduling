package alma.scheduling.test;

import junit.framework.TestCase;
import junit.*;
import alma.acs.component.client.ComponentClientTestCase;
import alma.scheduling.AlmaScheduling.ALMAArchive;
import alma.scheduling.AlmaScheduling.ALMAPublishEvent;
import alma.scheduling.AlmaScheduling.ALMAClock;
import alma.scheduling.AlmaScheduling.ALMAOperator;
import alma.scheduling.AlmaScheduling.ALMAProjectManager;
import alma.scheduling.AlmaScheduling.ALMATelescope;
import alma.scheduling.AlmaScheduling.ALMAControl;
import alma.scheduling.Define.*;
import alma.acs.component.ComponentQueryDescriptor;
import alma.acs.component.client.ComponentClient;
import alma.acs.component.client.AdvancedComponentClient;
import alma.acs.container.ContainerServices;
import alma.alarmsystem.AlarmService;
import alma.alarmsystem.source.ACSFaultState;

import java.sql.Timestamp;
import java.util.logging.Logger;
import alma.acs.logging.ClientLogManager;
import alma.scheduling.Define.SBQueue;
import alma.scheduling.MasterScheduler.MessageQueue;
import alma.JavaContainerError.wrappers.AcsJContainerServicesEx;

import alma.alarmsystem.source.ACSAlarmSystemInterfaceFactory;
import alma.alarmsystem.source.ACSAlarmSystemInterface;
import alma.alarmsystem.source.ACSFaultState;
import cern.cmw.mom.pubsub.impl.ACSJMSTopicConnectionImpl;

import java.util.Properties;
import java.util.Vector;
import alma.acs.nc.Consumer;
import cern.laser.source.alarmsysteminterface.impl.FaultStateImpl;
import cern.laser.source.alarmsysteminterface.impl.XMLMessageHelper;
import cern.laser.source.alarmsysteminterface.impl.ASIMessageHelper;
import cern.laser.source.alarmsysteminterface.impl.message.ASIMessage;
import java.util.Collection;
import alma.acs.logging.ClientLogManager;
import java.util.Iterator;

public class TestAcsAlarm {
    private ALMAArchive archive;
    private AdvancedComponentClient m_componentClient = null;
    private AlarmService AlarmManager = null;
    private Logger m_logger = null;
    private SBQueue sbQueue;
    private ALMAPublishEvent publisher=null;
    private MessageQueue messageQueue;
    private ALMAOperator operator;
    private ALMAProjectManager manager;
    private ALMATelescope telescope;
    private ALMAControl control;
    private ContainerServices m_containerservices;
    private ALMAClock clock;
    String  managerLoc;
    private Consumer m_consumer = null;
    private String m_channelName = "CMW.ALARM_SYSTEM.ALARMS.SOURCES.ALARM_SYSTEM_SOURCES";

 
    public TestAcsAlarm() throws Exception {
    	//super("Test ACS Alarm system");
	setUp();
	testsendAlarm();
	testQuerySpecialSBs();
	testGetAllProject();
	testGetProjectStatus();
	try {
		Thread.sleep(10000);
	} catch (Exception e) {}
	System.exit(-1);

    }
    protected void setUp() throws Exception {
		//super.setUp();
		
		try {
			GetComponentClient();
			initialize();
			sbQueue = new SBQueue();
			clock= new ALMAClock();
			archive = new ALMAArchive(m_componentClient.getContainerServices(),clock);
			publisher = new ALMAPublishEvent(m_containerservices);
			messageQueue = new MessageQueue();
			operator = new ALMAOperator(m_containerservices, messageQueue);
			manager = new ALMAProjectManager(m_containerservices, operator, archive, sbQueue, publisher,new ALMAClock());
			telescope = new ALMATelescope();
                        //control = new ALMAControl(m_containerservices, manager);
		} catch (Exception e) {
			System.out.println("Alarm system interface can not get connect");
			System.out.println(e.toString());
		}

		//set up the notification channel ....
		//assertFalse("Using ACS implementation instead of CERN",ACSAlarmSystemInterfaceFactory.usingACSAlarmSystem());
                m_consumer = new Consumer(m_channelName,m_componentClient.getContainerServices());
		//assertNotNull("Error instantiating the Consumer",m_consumer);
		m_consumer.addSubscription(com.cosylab.acs.jms.ACSJMSMessageEntity.class,this);
		m_consumer.consumerReady();
	}

	protected void tearDown() throws Exception {
		//super.tearDown();
	}
	
	public void sendAlarm(String ff, String fm, int fc, String fs) {
        try {
		m_logger = ClientLogManager.getAcsLogManager().getLoggerForApplication("Test",true);
		managerLoc = System.getProperty("ACS.manager");
		if(managerLoc==null) {
			System.out.println("JAVA property 'ACS manager' must set to the corbaloc");
			System.exit(-1);
		}


            ACSAlarmSystemInterface alarmSource = ACSAlarmSystemInterfaceFactory.createSource();
            ACSFaultState state = ACSAlarmSystemInterfaceFactory.createFaultState(ff, fm, fc);
            state.setDescriptor(fs);
            state.setUserTimestamp(new Timestamp(System.currentTimeMillis())); 	
            Properties prop = new Properties();
            prop.setProperty(ACSFaultState.ASI_PREFIX_PROPERTY, "prefix");
			prop.setProperty(ACSFaultState.ASI_SUFFIX_PROPERTY, "suffix");
			prop.setProperty("ALMAMasterScheduling_PROPERTY", "ConnArchiveException");
			state.setUserProperties(prop);
            alarmSource.push(state);
            System.out.println("alarm had send");
        } catch(Exception e) {
        	e.printStackTrace();
        }        
    }

	//@ALMAArchive(expected=ArchiveInternalError.class)
	
	public void testQuerySpecialSBs() throws SchedulingException{
		try {
			System.out.println("start test queryspecialSBs");
			archive.querySpecialSBs();
			//throw new SchedulingException();
		}
		catch(SchedulingException e) {
			e.printStackTrace();
    		//assertTrue(true);
		}
	}
	
	
	
	public void testGetAllProject() throws SchedulingException {
		try {
			//m_containerservices.releaseComponent(archive.toString());
			archive.getAllProject();
		}
		catch(SchedulingException e) {
			e.printStackTrace();
    		//assertTrue(true);
		}
	}
	
	
	
	
	public void testGetProjectStatus() throws SchedulingException {
		try {
				Project pj = new Project("obsProjectId","proposalId","projectName","projectVersion","PI");
				archive.getProjectStatus(pj);
				throw new SchedulingException();
		}
		catch(SchedulingException e) {
            		e.printStackTrace();
            		//assertTrue(true);
		}
	}

	
	
	
	
	public void testsendAlarm() {
		System.out.println("start send alarm");
		sendAlarm("Scheduling","SchedArrayConnAlarm",1,ACSFaultState.ACTIVE);
		sendAlarm("Scheduling","SchedArchiveConnAlarm",1,ACSFaultState.ACTIVE);
		sendAlarm("Scheduling","SchedControlConnAlarm",1,ACSFaultState.ACTIVE);
		sendAlarm("Scheduling","SchedControlConnAlarm",2,ACSFaultState.ACTIVE);
		sendAlarm("Scheduling","SchedControlConnAlarm",3,ACSFaultState.ACTIVE);
		sendAlarm("Scheduling","SchedArrayConnAlarm",1,ACSFaultState.ACTIVE);
		sendAlarm("Scheduling","SchedSBAbortedAlarm",1,ACSFaultState.ACTIVE);
		sendAlarm("Scheduling","SchedInvalidOperationAlarm",1,ACSFaultState.ACTIVE);
		System.out.println("end send alarm");
	//	assertTrue(true);
	}
    
	public void receive(com.cosylab.acs.jms.ACSJMSMessageEntity msg) {
		ASIMessage asiMsg;
		Collection faultStates;

		System.out.println("Receive the alarm event!");
		try {
			asiMsg = XMLMessageHelper.unmarshal(msg.text);
			faultStates = ASIMessageHelper.unmarshal(asiMsg);
		} catch (Exception e) {
			System.out.println("Exception caught while unmarshalling the msg "+e.getMessage());
			e.printStackTrace();
			return;
		}
		
		Iterator iter = faultStates.iterator();
		while (iter.hasNext()) {
			FaultStateImpl fs = (FaultStateImpl)iter.next();
			StringBuilder str = new StringBuilder("Alarm message received from source: <");
			str.append(fs.getFamily());
			str.append(",");
			str.append(fs.getMember());
			str.append(",");
			str.append(fs.getCode());
			str.append(">");
			str.append(" Status: ");
			str.append(fs.getDescriptor());
			System.out.println(str.toString());
		}

	}

	public void initialize() throws AcsJContainerServicesEx {
		System.out.println("GetComponentClient: container services initialize");
		try {
			//Get container services.
			System.out.println("Get alarm interface");
			m_componentClient = new AdvancedComponentClient(m_logger,managerLoc,"SchedulingAlarmTestClient");
			if(m_componentClient==null)
				System.exit(-1);
			System.out.println("ComponentClient="+m_componentClient);
			m_containerservices = m_componentClient.getContainerServices();
			ACSJMSTopicConnectionImpl.containerServices=m_containerservices;
			AlarmManager=alma.alarmsystem.AlarmServiceHelper.narrow(
		        m_containerservices.getDefaultComponent("IDL:alma/alarmsystem/AlarmService:1.0"));
			String m_compName = AlarmManager.name();
			System.out.println("m_compName:"+m_compName);
		} catch(Exception e) {
			System.out.println("QLSessionManager:initialize"+e.toString());
			m_containerservices = null;
			AlarmManager = null;
			//m_compName = null;
		}
	}

	public  void GetComponentClient() {
		//super();
		String name ="SchedAlarmManager";
		//m_logger = Logger.getLogger(name);
		m_logger =ClientLogManager.getAcsLogManager().getLoggerForApplication("SchedulingAlarmTestClient",true);
		managerLoc = System.getProperty("ACS.manager");
		if(managerLoc==null) {
			System.out.println("JAVA property 'ACS manager' must set to the corbaloc");
			System.exit(-1);
		}
	}


	public static void main(String[] args) {
	
		try {
		TestAcsAlarm test = new TestAcsAlarm();
		} 
		catch (Exception e ) {
			System.out.println(e.toString());
		}
			}


	
}
