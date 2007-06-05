package alma.scheduling.AlmaScheduling.GUI.PIWebApp;

import alma.scheduling.AlmaScheduling.ALMAArchive;
import alma.scheduling.AlmaScheduling.ALMAClock;
import alma.scheduling.Define.*;
import alma.acs.container.ContainerServices;
import alma.acs.component.client.ComponentClient;
import alma.acs.component.client.AdvancedComponentClient;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.*;
import java.io.IOException;

public class testACS {

	public testACS() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		 ContainerServices cs;
		 String manager;
		 Logger m_logger = null;
		 ComponentClient m_componentClient = null;
		 ALMAArchive archive;
		
		m_logger = Logger.getLogger("PIWebPage");
		manager = System.getProperty("ACS.manager");
		Properties props = System.getProperties();
		System.out.println("testACS:properties:"+props);
		System.out.println("testACS:manager:"+manager);
		try {
		m_componentClient = new AdvancedComponentClient(m_logger,manager,"PIWebPage");
		cs = m_componentClient.getContainerServices();
		archive = new ALMAArchive(cs, new ALMAClock());
		}
		catch (Exception e) {
		    System.out.println ("QLSESSIONGUI_TEST: Contructor error " +
		        e.toString());
		    //e.printStackTrace();
		    m_componentClient = null;
		}
	}
}
