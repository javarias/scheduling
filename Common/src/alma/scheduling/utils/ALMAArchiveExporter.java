package alma.scheduling.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;

import javax.xml.transform.TransformerException;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;

import alma.archive.exceptions.general.DatabaseException;
import alma.archive.xml.ObsProjectEntity;
import alma.archive.xml.ObsProposalEntity;
import alma.archive.xml.ObsReviewEntity;
import alma.archive.xml.SchedBlockEntity;
import alma.archive.xml.XmlEntity;
import alma.archive.xml.dao.HibernateXmlStoreDaoImpl;
import alma.entity.xmlbinding.obsproject.ObsUnitSetT;
import alma.entity.xmlbinding.obsreview.ObsReview;
import alma.entity.xmlbinding.schedblock.SchedBlockRefT;

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
	
	public Set<String> loadObsReviews() {
		File destSubDir = new File(destDir, "ObsReview");
		if (!destSubDir.exists())
			throw new IllegalArgumentException(destSubDir.getAbsolutePath() + " doesn't exists. Cannot continue.");
		if (!destSubDir.isDirectory() )
			throw new IllegalArgumentException(destSubDir.getAbsolutePath() + " is not a directory. Cannot continue.");
		if (!destSubDir.canRead()) 
			throw new IllegalArgumentException(destSubDir.getAbsolutePath() + " is not a readeable. Cannot continue.");
		obsReviewUids = new HashSet<String>();
		schedBlockUids = new HashSet<String>();
		for (File f: destSubDir.listFiles()) {
			alma.entity.xmlbinding.obsreview.ObsReview review;
			try {
				System.out.println("Reading " + f);
				review = alma.entity.xmlbinding.obsreview.ObsReview.unmarshalObsReview(new FileReader(f));
				obsReviewUids.add(review.getObsReviewEntity().getEntityId());
				schedBlockUids.addAll(processObsUnitSet(review.getObsPlan()));
			} catch (MarshalException e) {
				e.printStackTrace(); continue;
			} catch (ValidationException e) {
				e.printStackTrace(); continue;
			} catch (FileNotFoundException e) {
				e.printStackTrace(); continue;
			}
		}
		return obsReviewUids;
	}
	
	public Set<String> loadObsProjects() {
		File destSubDir = new File(destDir, "ObsProject");
		if (!destSubDir.exists())
			throw new IllegalArgumentException(destSubDir.getAbsolutePath() + " doesn't exists. Cannot continue.");
		if (!destSubDir.isDirectory() )
			throw new IllegalArgumentException(destSubDir.getAbsolutePath() + " is not a directory. Cannot continue.");
		if (!destSubDir.canRead()) 
			throw new IllegalArgumentException(destSubDir.getAbsolutePath() + " is not a readeable. Cannot continue.");
		obsProjectUids = new HashSet<String>();
		for (File f: destSubDir.listFiles()) {
			alma.entity.xmlbinding.obsproject.ObsProject project;
			FileReader r = null;
			try {
				System.out.println("Reading " + f);
				r = new FileReader(f);
				project = alma.entity.xmlbinding.obsproject.ObsProject.unmarshalObsProject(r);
				obsProjectUids.add(project.getObsProjectEntity().getEntityId());
			} catch (MarshalException e) {
				e.printStackTrace(); continue;
			} catch (ValidationException e) {
				e.printStackTrace(); continue;
			} catch (FileNotFoundException e) {
				e.printStackTrace(); continue;
			} finally {
				if (r != null)
					try {
						r.close();
					} catch (IOException e) {
					}
			}
		}
		return obsProjectUids;
	}
	
	/**
	 * 
	 * @param obsUnit
	 * @return a set containing the SchedBlocks UIDs
	 */
	private Set<String> processObsUnitSet(alma.entity.xmlbinding.obsproject.ObsUnitSetT obsUnit) {
		HashSet<String> ret = new HashSet<String>();
		for(ObsUnitSetT ou: obsUnit.getObsUnitSetTChoice().getObsUnitSet())
			ret.addAll(processObsUnitSet(ou));
		for(SchedBlockRefT sbr: obsUnit.getObsUnitSetTChoice().getSchedBlockRef()) {
			if (sbr == null) continue;
			System.out.println("Found SB: " + sbr.getEntityId());
			ret.add(sbr.getEntityId());
		}
		return ret;
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
		archiveDao.closeSession();
		return retval;
	}
	
	public Set<String> saveObsProjects() {
		Set<String> retval = new HashSet<String>();
		for(String uid: obsProposalUids) {
			String filename = uid.replace("/", "_").replace(":", "_");
			File destSubDir = new File(destDir, "ObsProposal");
			FileReader r = null;
			try {
				r = new FileReader(new File(destSubDir, filename + ".xml"));
				alma.entity.xmlbinding.obsproposal.ObsProposal proposal = 
				alma.entity.xmlbinding.obsproposal.ObsProposal.unmarshalObsProposal(r);
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
			} finally {
				if (r !=null )
					try {
						r.close();
					} catch (IOException e) {
					}
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
			retval.addAll(getMax1000ObsProjectsFromArchive(uids));
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
		archiveDao.closeSession();
		return retval;
	}
	
	public Set<String> saveObsReviews() {
		Set<String> retval = new HashSet<String>();
		for(String uid: obsProjectUids) {
			String filename = uid.replace("/", "_").replace(":", "_");
			File destSubDir = new File(destDir, "ObsProject");
			FileReader r = null;
			try {
				r =  new FileReader(new File(destSubDir, filename));
				alma.entity.xmlbinding.obsproject.ObsProject project = 
						alma.entity.xmlbinding.obsproject.ObsProject.unmarshalObsProject(r);
				alma.entity.xmlbinding.obsreview.ObsReviewRefT reviewRef = project.getObsReviewRef();
				if (reviewRef == null) continue;
				String  reviewUid = reviewRef.getEntityId();
				retval.add(reviewUid);
			} catch (MarshalException e) {
				e.printStackTrace();
				continue;
			} catch (ValidationException e) {
				e.printStackTrace();
				continue;
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				continue;
			} finally {
				if (r != null)
					try {
						r.close();
					} catch (IOException e) {
					}
			}
		}
		
		retval = getObsReviewsFromArchive(retval);
		
		return retval;
	}
	
	private Set<String> getSchedBlocksFromArchive(Set<String> uids) {
		Set<String> retval = new HashSet<String>();
		Set<String> tmpSet = null;
		if (uids.size() > 1000) {
			int i = 0;
			tmpSet = new HashSet<String>();
			for(String uid: uids) {
				tmpSet.add(uid);
				i++;
				if (i == 1000) {
					retval.addAll(getMax1000SchedBlocksFromArchive(tmpSet));
					i = 0;
					tmpSet.clear();
				}
			}
			if (tmpSet.size() > 0)
				retval.addAll(getMax1000SchedBlocksFromArchive(tmpSet));
		}
		else
			retval.addAll(getMax1000SchedBlocksFromArchive(uids));
		schedBlockUids = retval;
		return retval;
	}
	
	private Set<String> getMax1000SchedBlocksFromArchive(Set<String> uids) {
		Set<String> retval = new HashSet<String>();
		for(SchedBlockEntity sbe: archiveDao.getSchedBlocksIterator(uids)) {
			File destSubDir = new File(destDir, "SchedBlock");
			if (!destSubDir.exists())
				destSubDir.mkdir();
			
			System.out.println("Writing: " + sbe.getUid());
			try {
				saveEntityOnFile(destSubDir, sbe);
				retval.add(sbe.getUid());
			} catch (IOException e) {
				e.printStackTrace();
				continue;
			} catch (TransformerException e) {
				e.printStackTrace();
				continue;
			}
		}
		archiveDao.closeSession();
		return retval;
	}
	
	public Set<String> saveSchedBlocks() {
		Set<String> retval = new HashSet<String>();
		for(String uid: obsReviewUids) {
			String filename = uid.replace("/", "_").replace(":", "_") + ".xml";
			File destSubDir = new File(destDir, "ObsReview");
			FileReader r = null;
			try {
				r = new FileReader(new File(destSubDir, filename));
				ObsReview review = 
						alma.entity.xmlbinding.obsreview.ObsReview.unmarshalObsReview(r);
				retval.addAll(processObsUnitSet(review.getObsPlan()));
			} catch (MarshalException e) {
				e.printStackTrace();
				continue;
			} catch (ValidationException e) {
				e.printStackTrace();
				continue;
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				continue;
			} finally {
				if (r != null)
					try {
						r.close();
					} catch (IOException e) {
					}
			}
		}
		getSchedBlocksFromArchive(retval);
		schedBlockUids = retval;
		return retval;
	}
	
	private Set<String> getObsReviewsFromArchive(Set<String> uids) {
		Set<String> retval = new HashSet<String>();
		Set<String> tmpSet = null;
		if (uids.size() > 1000) {
			int i = 0;
			tmpSet = new HashSet<String>();
			for(String uid: uids) {
				tmpSet.add(uid);
				i++;
				if (i == 1000) {
					retval.addAll(getMax1000ObsReviewsFromArchive(tmpSet));
					i = 0;
					tmpSet.clear();
				}
			}
			if (tmpSet.size() > 0)
				retval.addAll(getMax1000ObsReviewsFromArchive(tmpSet));
		}
		else
			retval.addAll(getMax1000ObsReviewsFromArchive(uids));
		obsReviewUids = retval;
		return retval;
	}
	
	private Set<String> getMax1000ObsReviewsFromArchive(Set<String> uids) {
		Set<String> retval = new HashSet<String>();
		for(ObsReviewEntity ore: archiveDao.getObsReviewsIterator(uids)) {
			File destSubDir = new File(destDir, "ObsReview");
			if (!destSubDir.exists())
				destSubDir.mkdir();
			
			System.out.println("Writing: " + ore.getUid());
			try {
				saveEntityOnFile(destSubDir, ore);
				retval.add(ore.getUid());
			} catch (IOException e) {
				e.printStackTrace();
				continue;
			} catch (TransformerException e) {
				e.printStackTrace();
				continue;
			}
		}
		archiveDao.closeSession();
		return retval;
	}
	
	
	private void saveEntityOnFile(File dir, XmlEntity e) throws IOException, TransformerException {
		String filename = e.getUid().replace("/", "_").replace(":", "_") + ".xml";
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
	
	public String synchronizeProjectGrade(String uid) throws SQLException {
		Connection conn = archiveDao.getConnection();
		Statement stmt = null;
		try {
			stmt = conn.createStatement();
			String qString = "select APRC_LETTER_GRADE from proposal where ARCHIVE_UID = '" + uid +"'";
			System.out.println("Query: " + qString);
			ResultSet rs = stmt.executeQuery(qString);
			if (!rs.next()) {
				System.err.println("No Result");
				return "D";
			}
			String grade = rs.getString("APRC_LETTER_GRADE");
			if (grade == null)
				return "D";
			if (grade.equals("A") || grade.equals("B") || grade.equals("C"))
				return grade;
			return "D";
		}finally {
			if (stmt != null)
				stmt.close();
			archiveDao.closeSession();
		}
	}
	
	public void synchronizeProjectGrade() {
		File propDir = new File(destDir, "ObsProposal");
		File prjDir = new File(destDir, "ObsProject");
		if (!propDir.exists() || !prjDir.exists())
			throw new IllegalArgumentException(propDir.getAbsolutePath() + " doesn't exists. Cannot continue.");
		if (!propDir.isDirectory() )
			throw new IllegalArgumentException(propDir.getAbsolutePath() + " is not a directory. Cannot continue.");
		if (!propDir.canRead()) 
			throw new IllegalArgumentException(propDir.getAbsolutePath() + " is not a readeable. Cannot continue.");
		alma.entity.xmlbinding.obsproposal.ObsProposal proposal;
		alma.entity.xmlbinding.obsproject.ObsProject project;
		FileReader r = null;
		FileWriter w = null;
		for (String uid : obsProposalUids) {
			try {
				File fprop = new File(propDir, uid.replace(":", "_").replace("/", "_") + ".xml");
				String grade = synchronizeProjectGrade(uid);
				System.out.println("Reading " + fprop);
				r = new FileReader(fprop);
				proposal = alma.entity.xmlbinding.obsproposal.ObsProposal
						.unmarshalObsProposal(r);
				r.close();
				if (proposal.getObsProjectRef() == null)
					continue;
				String prjuid = proposal.getObsProjectRef().getEntityId();
				File prjF = new File(prjDir, prjuid.replace(":", "_").replace("/", "_") + ".xml");
				r = new FileReader(prjF);
				project = alma.entity.xmlbinding.obsproject.ObsProject.unmarshalObsProject(r);
				project.setLetterGrade(grade);
				System.out.println("grade: " + grade);
				w = new FileWriter(prjF);
				project.marshal(w);
			} catch (SQLException e) {
				e.printStackTrace();
				continue;
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				continue;
			} catch (MarshalException e) {
				e.printStackTrace();
				continue;
			} catch (ValidationException e) {
				e.printStackTrace();
				continue;
			} catch (IOException e) {
				e.printStackTrace();
				continue;
			} finally {
				proposal = null;
				project = null;
				if (r != null) {
					try {
						r.close();
					} catch (IOException e) {
					} 
					r = null;
				}
				if (w != null) {
					try {
						w.close();
					} catch (IOException e) {
					}
					w = null;
				}
			}
		}
	}
	
	public static void main(String[] args) throws DatabaseException {
		ALMAArchiveExporter exporter = new ALMAArchiveExporter(new File("./export/"));
		exporter.saveObsProposals("2013.1");
//		exporter.loadObsProposals();
		exporter.saveObsProjects();
		exporter.saveObsReviews();
//		exporter.loadObsReviews();
		exporter.saveSchedBlocks();		
		exporter.synchronizeProjectGrade();
	}
	
}
