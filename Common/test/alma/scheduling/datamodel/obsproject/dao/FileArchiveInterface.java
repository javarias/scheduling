package alma.scheduling.datamodel.obsproject.dao;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.omg.CORBA.UserException;

import alma.ACSErrTypeCommon.IllegalArgumentEx;
import alma.acs.entityutil.EntityException;
import alma.entity.xmlbinding.obsproject.ObsProject;
import alma.entity.xmlbinding.obsproposal.ObsProposal;
import alma.entity.xmlbinding.obsreview.ObsReview;
import alma.entity.xmlbinding.ousstatus.OUSStatus;
import alma.entity.xmlbinding.projectstatus.ProjectStatus;
import alma.entity.xmlbinding.sbstatus.SBStatus;
import alma.entity.xmlbinding.schedblock.SchedBlock;
import alma.scheduling.datamodel.obsproject.dao.ArchiveInterface;
import alma.scheduling.utils.SchedulingProperties.Phase1SBSourceValue;

public class FileArchiveInterface implements ArchiveInterface {

	private final File baseDir;
	private static final String OBS_PROPOSAL_DIRNAME = "ObsProposal";
	private static final String OBS_PROJECT_DIRNAME = "ObsProject";
	private static final String OBS_REVIEW_DIRNAME = "ObsReview";
	private static final String SCHED_BLOCK_DIRNAME = "SchedBlock";
	
	public FileArchiveInterface(File dir) {
		if(!dir.isDirectory() && !dir.canRead())
			throw new RuntimeException("The file passed as argument is not a directory or cannot be read.");
		baseDir = dir;
	}
	
	public FileArchiveInterface(String path) {
		this(new File(path));
	}
	
	@Override
	public boolean hasObsProposal(String id) {
		File prpF = getObsProposalFile(id);
		return prpF.exists() && prpF.canRead();
	}

	@Override
	public void cache(ObsProposal op) {
		// NO-OP
	}

	@Override
	public ObsProposal getObsProposal(String id) throws EntityException,
			UserException {
		FileReader r = null; 
		try {
			r = new FileReader(getObsProposalFile(id));
			return ObsProposal.unmarshalObsProposal(r);
		} catch (MarshalException e) {
			throw new EntityException(e);
		} catch (ValidationException e) {
			throw new EntityException(e);
		} catch (FileNotFoundException e) {
			throw new EntityException(e);
		} finally {
			if (r != null)
				try {
					r.close();
				} catch (IOException e) {
					//Nothing to do
				}
		}
	}

	@Override
	public ObsProposal cachedObsProposal(String id) {
		try {
			return getObsProposal(id);
		} catch (EntityException e) {
			e.printStackTrace();
			return null;
		} catch (UserException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public void forgetObsProposal(String id) {
		//NO-OP

	}

	@Override
	public void forgetObsProposal(ObsProposal op) {
		// NO-OP

	}

	@Override
	public ObsProposal refreshObsProposal(String id) throws EntityException,
			UserException {
		throw new RuntimeException("Not implemented");
	}

	@Override
	public ObsProposal refreshObsProposal(ObsProposal op)
			throws EntityException, UserException {
		throw new RuntimeException("Not implemented");
	}

	@Override
	public int numObsProposals() {
		File prpDir = new File(baseDir, OBS_PROPOSAL_DIRNAME);
		File[] files = prpDir.listFiles(new XmlFileFilter());
		return files.length;
	}
	
	private File getObsProposalFile(String id) {
		return new File(baseDir, OBS_PROPOSAL_DIRNAME + "/" + id.replace(":", "_").replace("/", "_") + ".xml");
	}

	@Override
	public boolean hasObsReview(String id) {
		File prpF = getObsProposalFile(id);
		return prpF.exists() && prpF.canRead();
	}

	@Override
	public void cache(ObsReview or) {
		//NO-OP

	}

	@Override
	public ObsReview getObsReview(String id) throws EntityException,
			UserException {
		FileReader r = null; 
		try {
			r = new FileReader(getObsReviewFile(id));
			return ObsReview.unmarshalObsReview(r);
		} catch (MarshalException e) {
			throw new EntityException(e);
		} catch (ValidationException e) {
			throw new EntityException(e);
		} catch (FileNotFoundException e) {
			throw new EntityException(e);
		} finally {
			if (r != null)
				try {
					r.close();
				} catch (IOException e) {
					//Nothing to do
				}
		}
	}

	@Override
	public ObsReview cachedObsReview(String id) {
		try {
			return getObsReview(id);
		} catch (EntityException e) {
			e.printStackTrace();
			return null;
		} catch (UserException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public void forgetObsReview(String id) {
		//NO-OP

	}

	@Override
	public void forgetObsReview(ObsReview or) {
		//NO-OP

	}

	@Override
	public ObsReview refreshObsReview(String id) throws EntityException,
			UserException {
		throw new RuntimeException("Not implemented");
	}

	@Override
	public ObsReview refreshObsReview(ObsReview or) throws EntityException,
			UserException {
		throw new RuntimeException("Not implemented");
	}

	@Override
	public int numObsReviews() {
		File prpDir = new File(baseDir, OBS_REVIEW_DIRNAME);
		File[] files = prpDir.listFiles(new XmlFileFilter());
		return files.length;
	}
	
	private File getObsReviewFile(String id) {
		return new File(baseDir, OBS_REVIEW_DIRNAME + "/" + id.replace(":", "_").replace("/", "_") + ".xml");
	}

	@Override
	public boolean hasObsProject(String id) {
		File prjF = getObsProjectFile(id);
		return prjF.exists() && prjF.canRead();
	}

	@Override
	public void cache(ObsProject op) {
		//NO-OP

	}

	@Override
	public ObsProject getObsProject(String id) throws EntityException,
			UserException {
		FileReader r = null; 
		try {
			r = new FileReader(getObsProjectFile(id));
			return ObsProject.unmarshalObsProject(r);
		} catch (MarshalException e) {
			throw new EntityException(e);
		} catch (ValidationException e) {
			throw new EntityException(e);
		} catch (FileNotFoundException e) {
			throw new EntityException(e);
		} finally {
			if (r != null)
				try {
					r.close();
				} catch (IOException e) {
					//Nothing to do
				}
		}
	}

	@Override
	public ObsProject cachedObsProject(String id) {
		try {
			return getObsProject(id);
		} catch (EntityException e) {
			e.printStackTrace();
			return null;
		} catch (UserException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public void forgetObsProject(String id) {
		//NO-OP

	}

	@Override
	public void forgetObsProject(ObsProject op) {
		//NO-OP

	}

	@Override
	public ObsProject refreshObsProject(String id) throws EntityException,
			UserException {
		throw new RuntimeException("Not implemented");
	}

	@Override
	public ObsProject refreshObsProject(ObsProject op) throws EntityException,
			UserException {
		throw new RuntimeException("Not implemented");
	}

	@Override
	public int numObsProjects() {
		File prpDir = new File(baseDir, OBS_PROJECT_DIRNAME);
		File[] files = prpDir.listFiles(new XmlFileFilter());
		return files.length;
	}
	
	private File getObsProjectFile(String id) {
		return new File(baseDir, OBS_PROJECT_DIRNAME + "/" + id.replace(":", "_").replace("/", "_") + ".xml");
	}

	@Override
	public boolean hasSchedBlock(String id) {
		File prjF = getSchedBlockFile(id);
		return prjF.exists() && prjF.canRead();
	}

	@Override
	public void cache(SchedBlock sb) {
		//NO-OP

	}

	@Override
	public SchedBlock getSchedBlock(String id) throws EntityException,
			UserException {
		FileReader r = null; 
		try {
			r = new FileReader(getSchedBlockFile(id));
			return SchedBlock.unmarshalSchedBlock(r);
		} catch (MarshalException e) {
			throw new EntityException(e);
		} catch (ValidationException e) {
			throw new EntityException(e);
		} catch (FileNotFoundException e) {
			throw new EntityException(e);
		} finally {
			if (r != null)
				try {
					r.close();
				} catch (IOException e) {
					//Nothing to do
				}
		}
	}

	@Override
	public SchedBlock cachedSchedBlock(String id) {
		try {
			return getSchedBlock(id);
		} catch (EntityException e) {
			e.printStackTrace();
			return null;
		} catch (UserException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public void forgetSchedBlock(String id) {
		// NO-OP

	}

	@Override
	public void forgetSchedBlock(SchedBlock op) {
		// NO-OP

	}

	@Override
	public SchedBlock refreshSchedBlock(String id) throws EntityException,
			UserException {
		throw new RuntimeException("Not implemented");
	}

	@Override
	public SchedBlock refreshSchedBlock(SchedBlock op) throws EntityException,
			UserException {
		throw new RuntimeException("Not implemented");
	}

	@Override
	public int numSchedBlocks() {
		File prpDir = new File(baseDir, SCHED_BLOCK_DIRNAME);
		File[] files = prpDir.listFiles(new XmlFileFilter());
		return files.length;
	}
	
	private File getSchedBlockFile(String id) {
		return new File(baseDir, SCHED_BLOCK_DIRNAME + "/" + id.replace(":", "_").replace("/", "_") + ".xml");
	}

	@Override
	public boolean hasProjectStatus(String id) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void cache(ProjectStatus ps) {
		// TODO Auto-generated method stub

	}

	@Override
	public ProjectStatus getProjectStatus(String id) throws EntityException,
			UserException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ProjectStatus cachedProjectStatus(String id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void forgetProjectStatus(String id) {
		// TODO Auto-generated method stub

	}

	@Override
	public void forgetProjectStatus(ProjectStatus op) {
		// TODO Auto-generated method stub

	}

	@Override
	public ProjectStatus refreshProjectStatus(String id)
			throws EntityException, UserException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ProjectStatus refreshProjectStatus(ProjectStatus op)
			throws EntityException, UserException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int numProjectStatuses() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void write(ProjectStatus status) throws EntityException,
			UserException {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean hasOUSStatus(String id) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void cache(OUSStatus os) {
		// TODO Auto-generated method stub

	}

	@Override
	public OUSStatus getOUSStatus(String id) throws EntityException,
			UserException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public OUSStatus cachedOUSStatus(String id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void forgetOUSStatus(String id) {
		// TODO Auto-generated method stub

	}

	@Override
	public void forgetOUSStatus(OUSStatus op) {
		// TODO Auto-generated method stub

	}

	@Override
	public OUSStatus refreshOUSStatus(String id) throws EntityException,
			UserException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public OUSStatus refreshOUSStatus(OUSStatus op) throws EntityException,
			UserException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int numOUSStatuses() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void write(OUSStatus status) throws EntityException, UserException {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean hasSBStatus(String id) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void cache(SBStatus sb) {
		// TODO Auto-generated method stub

	}

	@Override
	public SBStatus getSBStatus(String id) throws EntityException,
			UserException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SBStatus cachedSBStatus(String id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void forgetSBStatus(String id) {
		// TODO Auto-generated method stub

	}

	@Override
	public void forgetSBStatus(SBStatus op) {
		// TODO Auto-generated method stub

	}

	@Override
	public SBStatus refreshSBStatus(String id) throws EntityException,
			UserException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SBStatus refreshSBStatus(SBStatus op) throws EntityException,
			UserException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int numSBStatuses() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void write(SBStatus status) throws EntityException, UserException {
		// TODO Auto-generated method stub

	}

	@Override
	public Collection<String> getProjectStatusIdsByState(String[] states)
			throws IllegalArgumentEx {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> getIdsOfChangedProjects(Date since)
			throws UserException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> getIdsOfChangedSBs(Date since) throws UserException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Iterable<ObsProposal> obsProposals() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Iterable<ObsReview> obsReviews() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Iterable<ObsProject> obsProjects() {
		return new ObsProjectFileIterator();
	}

	@Override
	public Iterable<SchedBlock> schedBlocks() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<ProjectStatus> projectStatuses() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Iterable<OUSStatus> ousStatuses() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Iterable<SBStatus> sbStatuses() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void rememberPhase1Location(String projectId,
			Phase1SBSourceValue location) {
		// TODO Auto-generated method stub

	}

	@Override
	public Phase1SBSourceValue getPhase1Location(String projectId) {
		return Phase1SBSourceValue.REVIEW_ONLY;
	}

	@Override
	public Collection<SBStatus> getSBStatusesForProjectStatus(
			String projectStatusId) throws EntityException {
		// TODO Auto-generated method stub
		return null;
	}
	
	private class XmlFileFilter implements FilenameFilter {

		@Override
		public boolean accept(File dir, String name) {
			if (name.toLowerCase().endsWith(".xml"))
				return true;
			return false;
		}
	}
	
	private class ObsProjectFileIterator implements Iterable<ObsProject> {
		private final ArrayList<ObsProject> projects;
		
		public ObsProjectFileIterator() {
			projects = new ArrayList<ObsProject>();
			File prjDir = new File(baseDir, OBS_PROJECT_DIRNAME);
			for (File f: prjDir.listFiles(new XmlFileFilter())) {
				FileReader r = null; 
				try {
					r = new FileReader(f);
					projects.add(ObsProject.unmarshalObsProject(r));
				} catch (MarshalException e) {
					e.printStackTrace();
				} catch (ValidationException e) {
					e.printStackTrace();
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} finally {
					if (r != null)
						try {
							r.close();
						} catch (IOException e) {
							//Nothing to do
						}
				}
			}
				
		}
		
		@Override
		public Iterator<ObsProject> iterator() {
			return projects.iterator();
		}
		
	}

	@Override
	public void tidyUp() {
		// NO-OP
	}

}
