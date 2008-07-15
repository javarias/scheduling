/**
 * 
 */
package alma.scheduling.test;

import java.util.logging.Logger;

import alma.JavaContainerError.wrappers.AcsJContainerServicesEx;
import alma.acs.component.client.AdvancedComponentClient;
import alma.acs.container.ContainerServices;
import alma.acs.logging.ClientLogManager;
import alma.exec.extension.subsystemplugin.PluginContainerServices;
import alma.scheduling.ArrayModeEnum;
import alma.scheduling.MasterSchedulerIF;
import alma.scheduling.Queued_Operator_to_Scheduling;
import alma.scheduling.SBLite;
import alma.scheduling.AlmaScheduling.ALMAArchive;

/**
 * @author wlin
 *
 */
public class TestSimulatorQueueSB {

	/**
	 * 
	 */
	String  managerLoc;
	private AdvancedComponentClient m_componentClient = null;
	private ContainerServices m_containerservices;
	private Logger m_logger = null;
	private Queued_Operator_to_Scheduling qsComp;
	private ALMAArchive archive;
	
	public TestSimulatorQueueSB() {
	    initialize();
	}
	
	public void initialize()  {
		// TODO Auto-generated constructor stub
		m_logger =ClientLogManager.getAcsLogManager().getLoggerForApplication("SimulatorQueueSBTest",true);
		managerLoc = System.getProperty("ACS.manager");
		if(managerLoc==null) {
			m_logger.fine("JAVA property 'ACS manager' must set to the corbaloc");
			System.exit(-1);
		}
		
		try {
			m_componentClient = new AdvancedComponentClient(m_logger,managerLoc,"SchedulingQueuedTestClient");
			if(m_componentClient==null)
				System.exit(-1);
			m_containerservices = m_componentClient.getContainerServices();
		    MasterSchedulerIF ms = alma.scheduling.MasterSchedulerIFHelper.narrow(
		    		m_containerservices.getComponent("SCHEDULING_MASTERSCHEDULER"));
		    // create a array
		    String[] antenna = {"DV01","DA41"};
		    String arrayName = "Control/Array001";
		    ms.createArray(antenna,ArrayModeEnum.QUEUED);
		    String schedulerName = ms.createQueuedSchedulingComponent(arrayName);
		    qsComp = alma.scheduling.Queued_Operator_to_SchedulingHelper.narrow(
		    		m_containerservices.getComponent(schedulerName));
            qsComp.setArray(arrayName);
            
			System.out.println("SB queue length:"+ms.getSBLites().length);
			String id= (ms.getSBLites())[0].schedBlockRef;
			SBLite[] sbs = ms.getSBLites();
	            String[] ids= new String[sbs.length];
	            for(int i=0; i < sbs.length; i++){
	                ids[i] = sbs[i].schedBlockRef;
	                qsComp.addSB(ids[i]);
	                m_logger.info("SB id = "+ids[i]);
	            }
	            
			//System.out.println("id="+id);
		    //String currentSBId = id;
		    //System.out.println("add sb uid into the the sbqueue");
			//qsComp.addSB(id);
			//System.out.println("start a session");
			//scheduler.startSession(project.piName,project.uid);
			System.out.println("execute a Queue Scheduling");
			qsComp.runQueue();
			System.out.println("set current id");
			//scheduler.setCurrentSB(id);	
		} catch (AcsJContainerServicesEx e) {
			m_logger.fine("Can not get the MasterSchedulerIF"+e.toString());
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
			TestSimulatorQueueSB test = new TestSimulatorQueueSB();
			} 
			catch (Exception e ) {
				System.out.println(e.toString());
			}
				}
}
