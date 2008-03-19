/**
 * 
 */
package alma.scheduling.test;

import java.util.logging.Logger;

import alma.scheduling.*;
import alma.scheduling.MasterSchedulerIF;
import alma.scheduling.AlmaScheduling.ALMAClock;
import alma.scheduling.AlmaScheduling.*;
import alma.scheduling.AlmaScheduling.ALMAMasterScheduler;
import alma.acs.component.client.ComponentClientTestCase;
import alma.acs.component.client.ComponentClient;
import alma.acs.container.ContainerServices;
import alma.acs.component.client.AdvancedComponentClient;
import java.util.logging.Logger;
import alma.acs.logging.ClientLogManager;
import alma.scheduling.AlmaScheduling.GUI.OmcSchedulingPanel.*;
import alma.exec.extension.subsystemplugin.*;
import alma.scheduling.ProjectLite;
import alma.scheduling.Interactive_PI_to_Scheduling;

/**
 * @author wlin
 *
 */
public class TestSimulatorInteractiveSB {

	/**
	 * 
	 */
	String  managerLoc;
	private AdvancedComponentClient m_componentClient = null;
	private ContainerServices m_containerservices;
	private Logger m_logger = null;
	private Interactive_PI_to_Scheduling scheduler;
	private ALMAArchive archive;
	
	public TestSimulatorInteractiveSB() {
	    initialize();
	}
	
	public void initialize()  {
		// TODO Auto-generated constructor stub
		m_logger =ClientLogManager.getAcsLogManager().getLoggerForApplication("SimulatorInteractiveSBTest",true);
		managerLoc = System.getProperty("ACS.manager");
		if(managerLoc==null) {
			System.out.println("JAVA property 'ACS manager' must set to the corbaloc");
			System.exit(-1);
		}
		
		try {
			m_componentClient = new AdvancedComponentClient(m_logger,managerLoc,"SchedulingAlarmTestClient");
			if(m_componentClient==null)
				System.exit(-1);
			m_containerservices = m_componentClient.getContainerServices();
		    MasterSchedulerIF ms = alma.scheduling.MasterSchedulerIFHelper.narrow(
		    		m_containerservices.getComponent("SCHEDULING_MASTERSCHEDULER"));
		    // create a array
		    String[] antenna = {"DV01","DA41"};
		    ms.createArray(antenna,ArrayModeEnum.INTERACTIVE);
		    String schedulername = ms.startInteractiveScheduling1("Control/Array001");
		    scheduler = alma.scheduling.Interactive_PI_to_SchedulingHelper.narrow(
                    m_containerservices.getComponent(schedulername));
		    
			System.out.println("SB queue length:"+ms.getSBLites().length);
			String id= (ms.getSBLites())[0].schedBlockRef;
			System.out.println("id="+id);
		        String currentSBId = id;
		    System.out.println("get project for specified sbid");
			ProjectLite project = ms.getProjectLiteForSB(id);
			System.out.println("start a session");
			scheduler.startSession(project.piName,project.uid);
			System.out.println("execute a sb");
			scheduler.executeSB(id);
			System.out.println("set current id");
			scheduler.setCurrentSB(id);	
		} 
		catch (Exception e ) {
			System.out.println("Can not get SCHEDULING_MASTERSCHEDULER component");
			System.out.println(e.toString());
		}
	}

	public void setServices(PluginContainerServices c) {
		this.m_containerservices = c;
		m_containerservices.getLogger().fine("Start plugin service");
	}

	public void start() throws Exception {
	    System.out.println("Start to run this ");
	    initialize();
	}

	public boolean runRestricted (boolean b) throws Exception {
	    return b ;

	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {
			TestSimulatorInteractiveSB test = new TestSimulatorInteractiveSB();
			} 
			catch (Exception e ) {
				System.out.println(e.toString());
			}
				}
	}
