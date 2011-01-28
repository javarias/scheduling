/**
 * 
 */
package alma.scheduling.test;

import java.util.logging.Logger;

import alma.Control.CorrelatorType;
import alma.acs.component.client.AdvancedComponentClient;
import alma.acs.container.ContainerServices;
import alma.acs.logging.ClientLogManager;
import alma.exec.extension.subsystemplugin.PluginContainerServices;
import alma.scheduling.OLDArrayModeEnum;
import alma.scheduling.Interactive_PI_to_Scheduling;
import alma.scheduling.MasterSchedulerIF;
import alma.scheduling.ProjectLite;
import alma.scheduling.AlmaScheduling.ALMAArchive;
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
	private Interactive_PI_to_Scheduling scheduler1;
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
			m_componentClient = new AdvancedComponentClient(m_logger,managerLoc,"SchedulingOMCTestClient");
			if(m_componentClient==null)
				System.exit(-1);
			
			m_containerservices = m_componentClient.getContainerServices();
		    MasterSchedulerIF ms = alma.scheduling.MasterSchedulerIFHelper.narrow(
		    		m_containerservices.getComponent("SCHEDULING_MASTERSCHEDULER"));
		    
		    //ArchiveSubsystemMasterIF masterArchive = alma.archive.ArchiveSubsystemMasterIFHelper.narrow(
			//	m_containerservices.getComponent("ARCHIVE_MASTER_COMP"));
		    
		    //masterArchive.doTransition(SubsystemStateEvent.SUBSYSEVENT_INITPASS1);
		    //masterArchive.doTransition(SubsystemStateEvent.SUBSYSEVENT_INITPASS2);
		    //masterArchive.doTransition(SubsystemStateEvent.SUBSYSEVENT_START);
		    
		    
		    //QlDisplayMaster qlDisplay=alma.pipelineql.QlDisplayMasterHelper.narrow(
		    //		m_containerservices.getComponent("PIPELINE_QUICKLOOK_MASTER_COMP"));
		    
		    //archive.
		    // create a array
		    String[] antenna = {"DV01","DA41"};
		    String[] array01 = {"DV01"};
		    String[] array02 = {"DA41"};
		    String[] selectPhotonics = new String[0];
		    ms.createArray(antenna,
		    		       selectPhotonics,
		    		       CorrelatorType.BL,
		    		       OLDArrayModeEnum.INTERACTIVE);
		    //ms.createArray(array02,ArrayModeEnum.INTERACTIVE);
		    String schedulername = ms.startInteractiveScheduling1("Control/Array001");
		    //String schedulername1 = ms.startInteractiveScheduling1("Control/Array002");
		    scheduler = alma.scheduling.Interactive_PI_to_SchedulingHelper.narrow(
                    m_containerservices.getComponent(schedulername));
		    
		    //scheduler1 = alma.scheduling.Interactive_PI_to_SchedulingHelper.narrow(
            //        m_containerservices.getComponent(schedulername1));
	   	    for(int i=0;i<ms.getSBLites().length;i++) {
 
			System.out.println("SB queue length:"+ms.getSBLites().length);
			//for(int i=0;i<ms.getSBLites().length;i++){
			String id= (ms.getSBLites())[i].schedBlockRef;
			System.out.println("id="+id);
			//String id1= (ms.getSBLites())[1].schedBlockRef;
			//System.out.println("id1="+id1);
			
		    String currentSBId = id;
		    System.out.println("get project for specified sbid");
			ProjectLite project = ms.getProjectLiteForSB(id);
			
			//String currentSBId1 = id1;
		    //System.out.println("get project for specified sbid");
			//ProjectLite project1 = ms.getProjectLiteForSB(id1);
			
			System.out.println("start a session");
			scheduler.startSession(project.piName,project.uid);
			
			//System.out.println("start a session");
			//scheduler1.startSession(project1.piName,project1.uid);
			
			System.out.println("execute a sb");
			scheduler.executeSB(id);
			//System.out.println("execute a sb");
			//scheduler1.executeSB(id1);
			
			System.out.println("set current id");
			scheduler.setCurrentSB(id);	
			
			//System.out.println("set current id");
			//scheduler1.setCurrentSB(id);	
			
			
			}
		} 
		catch (Exception e ) {
			System.out.println("Simulator Interactive SB fail");
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
