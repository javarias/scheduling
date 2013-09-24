package alma.scheduling.psm.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;

import javax.xml.transform.TransformerException;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;

import alma.archive.exceptions.general.DatabaseException;
import alma.archive.xml.ObsProjectEntity;
import alma.archive.xml.ObsProposalEntity;
import alma.archive.xml.dao.HibernateXmlStoreDaoImpl;

/**
 * Save complete projects to disk
 * 
 * @author javarias
 *
 */
public class AlmaArchiveObsProjectExporter {

	private HibernateXmlStoreDaoImpl xmlStoreDao;
	private ArrayList<String> projectUids;
	
	public AlmaArchiveObsProjectExporter() throws DatabaseException {
		xmlStoreDao = new HibernateXmlStoreDaoImpl();
		projectUids = new ArrayList<String>();
	}
	
	/**
	 * Retrieve all projects given the XQuery for ObsProposals
	 * @param XQuery
	 */
	public void saveProposalsToDisk(String XQuery, File dir) {
		if (dir == null || !dir.isDirectory())
			throw new IllegalArgumentException("dir must be not null and a directory");
		if (!dir.exists())
			dir.mkdir();
		for (ObsProposalEntity prp: xmlStoreDao.getObsProposalsIterator(XQuery)) {
			try {
				alma.entity.xmlbinding.obsproposal.ObsProposal proposal = 
						alma.entity.xmlbinding.obsproposal.ObsProposal.unmarshalObsProposal(new StringReader(prp.domToString()));
				String propuid = proposal.getObsProposalEntity().getEntityId();
				propuid = propuid.replace(":", "_").replace("/", "_");
				File xmlFile = new File(dir, propuid);
				xmlFile.createNewFile();
				FileOutputStream fous = new FileOutputStream(xmlFile);
				fous.write(prp.domToString().getBytes());
				fous.close();
				System.out.println("Saved file " + xmlFile.getCanonicalPath());
				projectUids.add(proposal.getObsProjectRef().getEntityId());
			} catch (MarshalException e) {
				e.printStackTrace();
			} catch (ValidationException e) {
				e.printStackTrace();
			} catch (TransformerException e) {
				e.printStackTrace();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void saveProjectsToDisk(String XQuery, File dir, ArrayList<String> uids) {
		if (dir == null || !dir.isDirectory())
			throw new IllegalArgumentException("dir must be not null and a directory");
		if (!dir.exists())
			dir.mkdir();
		for (ObsProjectEntity prj: xmlStoreDao.getObsProjectsIterator(uids)) {
			try {
				alma.entity.xmlbinding.obsproject.ObsProject project = 
						alma.entity.xmlbinding.obsproject.ObsProject.unmarshalObsProject(new StringReader(prj.domToString()));
				String propuid = project.getObsProjectEntity().getEntityId();
				propuid = propuid.replace(":", "_").replace("/", "_");
				File xmlFile = new File(dir, propuid);
				xmlFile.createNewFile();
				FileOutputStream fous = new FileOutputStream(xmlFile);
				fous.write(prj.domToString().getBytes());
				fous.close();
				System.out.println("Saved file " + xmlFile.getCanonicalPath());
			} catch (MarshalException e) {
				e.printStackTrace();
			} catch (ValidationException e) {
				e.printStackTrace();
			} catch (TransformerException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private Collection<String> lookForSBsUids(alma.entity.xmlbinding.obsproject.ObsProject project) {
		return null;
	}
	
	public static void main (String[] args) throws DatabaseException {
		File dir = new File("projects");
		AlmaArchiveObsProjectExporter exporter = new AlmaArchiveObsProjectExporter();
		String cycle = "2012.1";
		exporter.saveProposalsToDisk("/prp:ObsProposal[prp:cycle=\""+ cycle +"\"]", dir);
	}
}
