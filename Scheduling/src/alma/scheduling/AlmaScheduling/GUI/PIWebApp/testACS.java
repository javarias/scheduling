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
		readvalue();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param args
	 */
	public  String readvalue() {
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
		try {
            Project[] p = archive.getAllProject();
            //assertNotNull(p);
            m_logger.info("Got "+p.length+" projects");
            String uid1;
            for (int i=0; i < p.length;i++){
                uid1 = p[i].getId();
                m_logger.info(uid1);
            }

            
        } catch (Exception e) {
            m_logger.severe("SCHED_TEST: Error");
            e.printStackTrace();
            throw new Exception(e);
        }

		}
		catch (Exception e) {
		    System.out.println ("TestACS: Contructor error " +
		        e.toString());
		    //e.printStackTrace();
		    m_componentClient = null;
		}
		return manager;
	}
	public static void main(String[] args){
		testACS aaa = new testACS();
	}

	
}
