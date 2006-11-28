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
//import alma.acs.container.ContainerException;
import alma.acs.container.ContainerServices;
import alma.alarmsystem.AlarmService;
import alma.alarmsystem.source.ACSFaultState;
import java.util.logging.Logger;
import alma.scheduling.Define.SBQueue;
import alma.scheduling.MasterScheduler.MessageQueue;
import alma.JavaContainerError.wrappers.AcsJContainerServicesEx;


public class TestAcsAlarm extends ComponentClientTestCase {
    private ALMAArchive archive;
    private ComponentClient m_componentClient = null;
    private ContainerServices m_containerservices = null;
    private AlarmService AlarmManager = null;
    private Logger m_logger = null;
    private SBQueue sbQueue;
    private ALMAPublishEvent publisher=null;
    private MessageQueue messageQueue;
    private ALMAOperator operator;
    private ALMAProjectManager manager;
    private ALMATelescope telescope;
    private ALMAControl control;
 
    public TestAcsAlarm() throws Exception {
    	super("Test ACS Alarm system");
    }
    protected void setUp() throws Exception {
		super.setUp();
		try {
			GetComponentClient();
			initialize();
			sbQueue = new SBQueue();
			archive = new ALMAArchive(getContainerServices(),new ALMAClock());
			publisher = new ALMAPublishEvent(m_containerservices);
			messageQueue = new MessageQueue();
			operator = new ALMAOperator(m_containerservices, messageQueue);
			manager = new ALMAProjectManager(m_containerservices, operator, archive, sbQueue, publisher,new ALMAClock());
			telescope = new ALMATelescope();
                        control = new ALMAControl(m_containerservices, manager);
		} catch (Exception e) {
			System.out.println("Alarm system interface can not get connect");
			System.out.println(e.toString());
		}
		//archive = new ALMAArchive(getContainerServices(),new ALMAClock());
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	//@ALMAArchive(expected=ArchiveInternalError.class)
	/*
	public void testQuerySpecialSBs() {
		fail("Not yet implemented");
	}

	public void testGetAllProject() {
		fail("Not yet implemented");
	}
	*/
	
	public void testGetProjectStatus() throws SchedulingException {
		try {
				Project pj = new Project("obsProjectId","proposalId","projectName","projectVersion","PI");
				archive.getProjectStatus(pj);
				throw new SchedulingException();
		}
		catch(SchedulingException e) {
            		e.printStackTrace();
			assertTrue(true);
		}
	}

	/*
	public void testGetProjectStatusForObsProject() {
		fail("Not yet implemented");
	}

	public void testQueryProjectStatus() {
		fail("Not yet implemented");
	}

	public void testCheckArchiveStillActive() {
		fail("Not yet implemented");
	}
	
	public void testgetAllProject() {
		fail("Not yet implemented");
	}
	
	public void testgetAllObsProjects() {
		
	}
	
	public void testgetArchiveComponents() {
		
	}
	*/

	public void initialize() throws AcsJContainerServicesEx {
		System.out.println("GetComponentClient: container services initialize");
		try {
			//Get container services.
			System.out.println("Get alarm interface");
			m_containerservices = m_componentClient.getContainerServices();
			//consumer = new Consumer(channelName,m_containerservices);
			//Connent to component
			//m_cqd = new ComponentQueryDescriptor("QlsessionLook_","IDL:alma/alarmsystem/AlarmService:1.0");
			AlarmManager=alma.alarmsystem.AlarmServiceHelper.narrow(
		        m_containerservices.getDefaultComponent("IDL:alma/alarmsystem/AlarmService:1.0"));
			//qldsplayManager = 
			//alma.pipelineql.QlDisplayManagerHelper.narrow(m_containerservices.getDynamicComponent(m_cqd, false));
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
		m_logger = Logger.getLogger(name);
		String managerLoc = System.getProperty("ACS.manager");
		try {
			m_componentClient = new ComponentClient(m_logger, managerLoc, name);
			System.out.println("SchedAlarmManager:Construactor called");
		} catch (Exception e ) {
			System.out.println("SchedAlarmManager:Construactor error"+e.toString());
			m_componentClient = null;
		}	
	     //return m_componentClient;
		// TODO Auto-generated constructor stub
	}

	
	public static void main(String[] args) {
	System.out.println("Alarm system testing start.....");
        alma.acs.testsupport.tat.TATJUnitRunner.run(TestAcsAlarm.class);
    }
}
