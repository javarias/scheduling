package alma.scheduling.psm.cli;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class RMIServer{

	RemoteConsole console;
	
	public RMIServer() {
		console = new RemoteConsoleImpl();
	}
	
	public void start(){
		if (System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());
        }
        try {
        	RemoteConsole stub = 
        		 (RemoteConsole) UnicastRemoteObject.exportObject(console, 0);
        	 Registry registry = LocateRegistry.getRegistry();
        	 registry.rebind("aprcSimConsoleService", stub);
        	 System.err.println("aprcSimConsoleService bound");
        } catch (Exception e) {
            System.err.println("aprcSimConsoleService exception:");
            e.printStackTrace();
        }

		
		while (true){
			synchronized(this){
				try {
					this.wait();
				} catch (InterruptedException e) {
					break;
				}
			}
		}
	}

}
