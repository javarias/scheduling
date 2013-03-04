package alma.scheduling.psm.util;


import alma.acs.component.client.ComponentClient;
import alma.acs.container.ContainerServices;

public class SimpleClient extends ComponentClient {
	
	private static SimpleClient INSTANCE = null;
	
	private SimpleClient(String managerLoc, String clientName)
			throws Exception {
		super(null, managerLoc, clientName);
		
	}
	
	public ContainerServices getContainerServices() {
		return super.getContainerServices();
	}

	public static synchronized SimpleClient getInstance() throws Exception {
		String managerLoc = System.getProperty("ACS.manager");
        if (managerLoc == null) {
            System.err.println("Java property 'ACS.manager' must be set to the corbaloc of the ACS manager!");
            System.exit(-1);
        }
        
        if (INSTANCE == null)
        	INSTANCE = new SimpleClient(managerLoc, "Scheduling client");
        return INSTANCE;
	}

}
