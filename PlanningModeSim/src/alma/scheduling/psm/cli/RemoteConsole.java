package alma.scheduling.psm.cli;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RemoteConsole extends Remote {

	public void runTask(String[] args) throws RemoteException;
	
}
