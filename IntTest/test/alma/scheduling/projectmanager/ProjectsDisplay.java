/**
 * Display information about the projects that are in the archive and
 * allow some manipulation of them.
 */
package alma.scheduling.projectmanager;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JFrame;
import javax.swing.JPanel;

import org.omg.CORBA.UserException;

import alma.JavaContainerError.wrappers.AcsJContainerServicesEx;
import alma.acs.component.client.ComponentClient;
import alma.acs.logging.AcsLogger;
import alma.projectlifecycle.StateSystemHelper;
import alma.projectlifecycle.StateSystemOperations;
import alma.scheduling.SchedulingException;
import alma.xmlstore.ArchiveConnection;
import alma.xmlstore.ArchiveConnectionHelper;
import alma.xmlstore.OperationalOperations;

/**
 * @author dclarke
 *
 */
public class ProjectsDisplay extends ComponentClient {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5079320498378017854L;
	
	private StateArchiveDAO sa;

	private static final String archiveIFName
					= "IDL:alma/xmlstore/ArchiveConnection:1.0";
	private static final String stateIFName
					= "IDL:alma/projectlifecycle/StateSystem:1.0";
	
	private AcsLogger             logger;
	private OperationalOperations xmlStore;
	private StateSystemOperations stateSystem;
	


	/*
	 * ================================================================
	 * Construction
	 * ================================================================
	 */
	/**
	 * @throws Exception
	 */
	protected ProjectsDisplay(String clientName) throws Exception {
		this(null, // Always null, retained in ACS for compatibility
				getManagerLocation(),
				clientName);
	}

	/**
	 * @param logger
	 * @param managerLoc
	 * @param clientName
	 * @throws Exception
	 */
	protected ProjectsDisplay(Logger willBeNull,
			                  String managerLoc,
			                  String clientName)
											throws Exception {
		super(willBeNull, managerLoc, clientName);
		this.logger = getContainerServices().getLogger();
		this.xmlStore = getArchive(archiveIFName);
		this.stateSystem = getStateSystem(stateIFName);
		sa = new StateArchiveDAO(this.logger, xmlStore, stateSystem);
	}
	/* End Construction
	 * ============================================================= */



	/*
	 * ================================================================
	 * ACS Bookkeeping
	 * ================================================================
	 */
	private static String getManagerLocation()
										   throws SchedulingException {
		final String result = System.getProperty("ACS.manager");
		if (result == null) {
			throw new SchedulingException("Java property 'ACS.manager' is not set. It must be set to the corbaloc of the ACS manager!");
		}
		return result;
	}
    
   private OperationalOperations getArchive(String name)
   						throws AcsJContainerServicesEx, UserException {
	   final org.omg.CORBA.Object obj
	   				= getContainerServices().getDefaultComponent(name);
	   final ArchiveConnection con = ArchiveConnectionHelper.narrow(obj);
	   final OperationalOperations ops = con.getOperational("SCHEDULING");
	   if (ops == null) {
		   logger.warning(String.format(
				   "SCHEDULING: Cannot find ALMA Archive component called %s.",
				   name));
	   }
	   return ops;
   }
   
	private StateSystemOperations getStateSystem(String name)
						throws AcsJContainerServicesEx {
		final org.omg.CORBA.Object obj
					= getContainerServices().getDefaultComponent(name);
		final StateSystemOperations ops = StateSystemHelper.narrow(obj);
		if (ops == null) {
			logger.warning(String.format(
					"SCHEDULING: Cannot find ALMA State System component called %s.",
					name));
		}
		return ops;
	}
	/* End ACS Bookkeeping
	 * ============================================================= */
	
    /**
     * Create the GUI and show it.  For thread safety,
     * this method should be invoked from the
     * event-dispatching thread.
     */
    private void createAndShowGUI() {
        //Create and set up the window.
        JFrame frame = new JFrame("Projects Display");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel gui = new ProjectDisplayGUI(sa);
		frame.getContentPane().add(gui);

        //Display the window.
        frame.pack();
        frame.setVisible(true);
    }

    public void testGUI() throws Exception {
		final Runnable r = new Runnable() {
			public void run() {
				createAndShowGUI();
			}
		};
		
        javax.swing.SwingUtilities.invokeLater(r);
        do {
    		Thread.sleep(3600*1000);
        } while (true);
	}

	/**
	 * Checks whether the Java property 'ACS.manager' is set and calls the
	 * other methods from this class.
	 */
	public static void main(String[] args) {
		String managerLoc = System.getProperty("ACS.manager");
		if (managerLoc == null) {
			System.out
					.println("Java property 'ACS.manager' must be set to the corbaloc of the ACS manager!");
			System.exit(-1);
		}
		String clientName = "Projects Display";
		ProjectsDisplay pd = null;
		try {
			pd = new ProjectsDisplay(null, managerLoc, clientName);
			pd.createAndShowGUI();
		} catch (Exception e) {
            try {
                Logger logger = pd.getContainerServices().getLogger();
                logger.log(Level.SEVERE, "Client application failure", e);
            } catch (Exception e2) {
                e.printStackTrace(System.err);
            }
		} finally {
			if (pd != null) {
				try {
					pd.tearDown();
				}
				catch (Exception e3) {
					// bad luck
                    e3.printStackTrace();
				}
			}
		}
	}

}
