package alma.scheduling.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import javax.xml.transform.TransformerException;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;

import alma.archive.exceptions.general.DatabaseException;
import alma.archive.xml.ObsProjectEntity;
import alma.archive.xml.ObsProposalEntity;
import alma.archive.xml.XmlEntity;
import alma.archive.xml.dao.HibernateXmlStoreDaoImpl;

public class ALMAArchiveExporter {

	private File destDir;
	private HibernateXmlStoreDaoImpl archiveDao;
	public Set<String> obsProposalUids, obsProjectUids, obsReviewUids, schedBlockUids;
	
	public ALMAArchiveExporter(File destDir) throws DatabaseException {
		if (destDir == null)
			throw new IllegalArgumentException("Destination directory cannot be null");
		if (!destDir.exists())
			destDir.mkdirs();
		if (!destDir.isDirectory())
			throw new IllegalArgumentException("Destination file is not a directory: " + destDir.getAbsolutePath());
		
		this.destDir = destDir;
		archiveDao = new HibernateXmlStoreDaoImpl();
	}
	
	public Set<String> saveObsProposals(String cycle) {
		Set<String> retval = new HashSet<String>();
		for (ObsProposalEntity ope : archiveDao.getObsProposalsIterator("/prp:ObsProposal[prp:cycle=\"" + cycle +"\"]")) {
			File destSubDir = new File(destDir, "ObsProposal");
			if (!destSubDir.exists())
				destSubDir.mkdir();
			
			System.out.println("Writing: " + ope.getUid());
			try {
				saveEntityOnFile(destSubDir, ope);
				retval.add(ope.getUid());
			} catch (IOException e) {
				e.printStackTrace();
				continue;
			} catch (TransformerException e) {
				e.printStackTrace();
				continue;
			}
		}
		obsProposalUids = retval;
		return retval;
	}
	
	public Set<String> loadObsProposals() {
		File destSubDir = new File(destDir, "ObsProposal");
		if (!destSubDir.exists())
			throw new IllegalArgumentException(destSubDir.getAbsolutePath() + " doesn't exists. Cannot continue.");
		if (!destSubDir.isDirectory() )
			throw new IllegalArgumentException(destSubDir.getAbsolutePath() + " is not a directory. Cannot continue.");
		if (!destSubDir.canRead()) 
			throw new IllegalArgumentException(destSubDir.getAbsolutePath() + " is not a readeable. Cannot continue.");
		obsProjectUids = new HashSet<String>();
		obsProposalUids = new HashSet<String>();
		for (File f: destSubDir.listFiles()) {
			alma.entity.xmlbinding.obsproposal.ObsProposal proposal;
			try {
				System.out.println("Reading " + f);
				proposal = alma.entity.xmlbinding.obsproposal.ObsProposal.unmarshalObsProposal(new FileReader(f));
				obsProposalUids.add(proposal.getObsProposalEntity().getEntityId());
				alma.entity.xmlbinding.obsproject.ObsProjectRefT projectRef = proposal.getObsProjectRef();
				if (projectRef == null) continue;
				String  projUid = projectRef.getEntityId();
				obsProjectUids.add(projUid);
			} catch (MarshalException e) {
				e.printStackTrace(); continue;
			} catch (ValidationException e) {
				e.printStackTrace(); continue;
			} catch (FileNotFoundException e) {
				e.printStackTrace(); continue;
			}
		}
		return obsProposalUids;
	}
	
	public Set<String> saveObsProjects() {
		Set<String> retval = new HashSet<String>();
		for(String uid: obsProposalUids) {
			String filename = uid.replace("/", "_").replace(":", "_");
			File destSubDir = new File(destDir, "ObsProposal");
			try {
				alma.entity.xmlbinding.obsproposal.ObsProposal proposal = 
				alma.entity.xmlbinding.obsproposal.ObsProposal.unmarshalObsProposal(new FileReader(new File(destSubDir, filename)));
				alma.entity.xmlbinding.obsproject.ObsProjectRefT projectRef = proposal.getObsProjectRef();
				if (projectRef == null) continue;
				String  projUid = projectRef.getEntityId();
				retval.add(projUid);
			} catch (MarshalException e) {
				e.printStackTrace();
				continue;
			} catch (ValidationException e) {
				e.printStackTrace();
				continue;
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				continue;
			}
		}
		
		retval = getObsProjectsFromArchive(retval);
		
		return retval;
	}
	
	private Set<String> getObsProjectsFromArchive(Set<String> uids) {
		Set<String> retval = new HashSet<String>();
		Set<String> tmpSet = null;
		if (uids.size() > 1000) {
			int i = 0;
			tmpSet = new HashSet<String>();
			for(String uid: uids) {
				tmpSet.add(uid);
				i++;
				if (i == 1000) {
					retval.addAll(getMax1000ObsProjectsFromArchive(tmpSet));
					i = 0;
					tmpSet.clear();
				}
			}
			if (tmpSet.size() > 0)
				retval.addAll(getMax1000ObsProjectsFromArchive(tmpSet));
		}
		else
			retval.addAll(getMax1000ObsProjectsFromArchive(tmpSet));
		obsProjectUids = retval;
		return retval;
	}
	
	private Set<String> getMax1000ObsProjectsFromArchive(Set<String> uids) {
		Set<String> retval = new HashSet<String>();
		for(ObsProjectEntity oprje: archiveDao.getObsProjectsIterator(uids)) {
			File destSubDir = new File(destDir, "ObsProject");
			if (!destSubDir.exists())
				destSubDir.mkdir();
			
			System.out.println("Writing: " + oprje.getUid());
			try {
				saveEntityOnFile(destSubDir, oprje);
				retval.add(oprje.getUid());
			} catch (IOException e) {
				e.printStackTrace();
				continue;
			} catch (TransformerException e) {
				e.printStackTrace();
				continue;
			}
		}
		return retval;
	}
	
//	private Set<String> getObsReviewsFromArchive(Set<String> obsProjectsUid) {
//		Set<String> retval = new HashSet<String>();
//		for(ObsReviewEntity ore: archiveDao.getObs(obsProjectsUid)) {
//			File destSubDir = new File(destDir, "ObsReview");
//			if (!destSubDir.exists())
//				destSubDir.mkdir();
//			
//			System.out.println("Writing: " + oprje.getUid());
//			try {
//				saveEntityOnFile(destSubDir, oprje);
//				retval.add(oprje.getUid());
//			} catch (IOException e) {
//				e.printStackTrace();
//				continue;
//			} catch (TransformerException e) {
//				e.printStackTrace();
//				continue;
//			}
//		}
//		obsProjectsUid = retval;
//		return retval;
//		
//	}
	
	
	private void saveEntityOnFile(File dir, XmlEntity e) throws IOException, TransformerException {
		String filename = e.getUid().replace("/", "_").replace(":", "_");
		File destFile = new File(dir, filename);
		if (!destFile.exists())
			destFile.createNewFile();
		FileOutputStream os = null;
		try {
		os = new FileOutputStream(destFile);
		os.write(e.domToString().getBytes());
		System.out.println(destFile.getAbsolutePath());
		} finally {
			if (os != null)
				try {
					os.close();
				} catch (IOException ex) {
				}
		}
	}
	
	public static void main(String[] args) throws DatabaseException {
		ALMAArchiveExporter exporter = new ALMAArchiveExporter(new File("./export/"));
//		exporter.saveObsProposals("2012.1");
		exporter.loadObsProposals();
		exporter.saveObsProjects();
	}
	
}
