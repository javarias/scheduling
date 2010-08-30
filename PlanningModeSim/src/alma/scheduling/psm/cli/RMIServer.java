package alma.scheduling.psm.cli;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

import alma.scheduling.datamodel.obsproject.ObsProject;
import alma.scheduling.psm.util.Ph1mSynchronizer;
import alma.scheduling.psm.util.Ph1mSynchronizerImpl;
import alma.scheduling.psm.util.ProposalComparison;

public class RMIServer{

	RemoteConsole console;
	Ph1mSynchronizer ph1m;
	
	public RMIServer() {
		console = new RemoteConsoleImpl();
		ph1m = new Ph1mSynchronizerImpl(System.getenv("APRC_WORK_DIR"));
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
        try {
        	Ph1mSynchronizer stub = 
        		 (Ph1mSynchronizer) UnicastRemoteObject.exportObject(ph1m, 0);
        	 Registry registry = LocateRegistry.getRegistry();
        	 registry.rebind("Ph1mSynchronizerService", stub);
        	 System.err.println("Ph1mSynchronizerService bound");
        } catch (Exception e) {
            System.err.println("Ph1mSynchronizerService exception:");
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
