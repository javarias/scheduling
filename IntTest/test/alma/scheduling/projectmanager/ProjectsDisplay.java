/*******************************************************************************
 * ALMA - Atacama Large Millimeter Array
 * Copyright (c) AUI - Associated Universities Inc., 2011
 * (in the framework of the ALMA collaboration).
 * All rights reserved.
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307  USA
 *******************************************************************************/
/**
 * Display information about the projects that are in the archive and
 * allow some manipulation of them.
 */
package alma.scheduling.projectmanager;

import static alma.lifecycle.config.SpringConstants.STATE_SYSTEM_SPRING_CONFIG;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JFrame;
import javax.swing.JPanel;

import org.omg.CORBA.UserException;

import alma.JavaContainerError.wrappers.AcsJContainerServicesEx;
import alma.acs.component.client.ComponentClient;
import alma.acs.logging.AcsLogger;
import alma.lifecycle.config.StateSystemContextFactory;
import alma.lifecycle.persistence.StateArchive;
import alma.lifecycle.stateengine.StateEngine;
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

	private AcsLogger             logger;
	private StateArchive          stateArchive;
	private StateEngine           stateEngine;
	


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
		getStateSystem(this.logger);
		sa = new StateArchiveDAO(this.logger, stateArchive, stateEngine);
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
   
	private void getStateSystem(Logger logger)
						throws AcsJContainerServicesEx {
		
		if (!StateSystemContextFactory.INSTANCE.isInitialized()) {
			StateSystemContextFactory.INSTANCE.init(STATE_SYSTEM_SPRING_CONFIG, logger);
		}
		
		stateArchive = StateSystemContextFactory.INSTANCE.getStateArchive();
		stateEngine = StateSystemContextFactory.INSTANCE.getStateEngine();
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
