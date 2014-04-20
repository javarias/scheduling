package alma.scheduling.psm.util;

import java.rmi.RemoteException;
import java.util.List;

import alma.scheduling.datamodel.obsproject.ObsProject;
import alma.scheduling.psm.util.ProposalComparison;

public interface Ph1mSynchronizer {
	
	public void setWorkDir(String workDir);
	
	public void syncrhonizeProject(ObsProject p) throws RemoteException;
	
	public void synchPh1m() throws RemoteException;
	
	public List<ProposalComparison> listPh1mProposals() throws RemoteException;
}
