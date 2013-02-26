package alma.scheduling.psm.cli;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

//import alma.scheduling.psm.ph1m.Ph1mSynchronizer;
//import alma.scheduling.psm.ph1m.Ph1mSynchronizerImpl;

public class RMIServer{

	RemoteConsole console;
//	Ph1mSynchronizer ph1m;
	
	public RMIServer() {
		console = new RemoteConsoleImpl();
//		try{
//			ph1m = new Ph1mSynchronizerImpl(System.getenv("APRC_WORK_DIR"));
//		}catch(Exception ex){ 
//			ph1m = null;
//			System.out.println("Ph1mSynchronizerService will not be available");
//		}
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
        } catch (Exception e) { // TODO: General Exception catch, change this.
            System.err.println("aprcSimConsoleService exception:");
            e.printStackTrace();
        }
//		if (ph1m != null) {
//			try {
//				Ph1mSynchronizer stub = (Ph1mSynchronizer) UnicastRemoteObject
//						.exportObject(ph1m, 0);
//				Registry registry = LocateRegistry.getRegistry();
//				registry.rebind("Ph1mSynchronizerService", stub);
//				System.err.println("Ph1mSynchronizerService bound");
//			} catch (Exception e) { // TODO: General Exception catch, change this. 
//				System.err.println("Ph1mSynchronizerService exception:");
//				e.printStackTrace();
//			}
//		}

		
		while (true){
			synchronized(this){
				try {
					this.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
					break;
				}
			}
		}
	}

}
