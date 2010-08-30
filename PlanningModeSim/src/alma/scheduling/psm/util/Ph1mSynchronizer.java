package alma.scheduling.psm.util;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

import alma.scheduling.datamodel.obsproject.ObsProject;

public interface Ph1mSynchronizer extends Remote {
	
	public void syncrhonizeProject(ObsProject p) throws RemoteException;
	
	public void synchPh1m() throws RemoteException;
	
	public List<ProposalComparison> listPh1mProposals() throws RemoteException;
}
