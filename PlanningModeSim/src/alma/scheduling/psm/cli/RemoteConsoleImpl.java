package alma.scheduling.psm.cli;

import java.rmi.RemoteException;

public class RemoteConsoleImpl implements RemoteConsole {

	private Console console;
	
	public RemoteConsoleImpl(){
		console = Console.getConsole();
	}
	
	@Override
	public void runTask(String[] args) throws RemoteException {
		console.run(args);
	}

}
