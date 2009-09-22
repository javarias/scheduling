/**
 * 
 */
package alma.scheduling.inttest;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import alma.JavaContainerError.wrappers.AcsJContainerServicesEx;
import alma.acs.container.ContainerServices;
import alma.entity.xmlbinding.obsproject.ObsProject;
import alma.entity.xmlbinding.obsproject.ObsProjectEntityT;
import alma.entity.xmlbinding.obsproposal.ObsProposal;
import alma.entity.xmlbinding.obsproposal.ObsProposalEntityT;
import alma.entity.xmlbinding.projectstatus.StatusBaseT;
import alma.entity.xmlbinding.schedblock.SchedBlock;
import alma.entity.xmlbinding.schedblock.SchedBlockEntityT;

/**
 * @author dclarke
 *
 */
public class ReadAndPrintProject {

	private ContainerServices containerServices;
	private Map<String, String> entityIdConversion;
	
	public ReadAndPrintProject(ContainerServices containerServices) {
		this.containerServices = containerServices;
		entityIdConversion = new TreeMap<String, String>();
	}
    
	private ContainerServices getContainerServices() {
		return containerServices;
	}
    
	private void rememberConversion(String oldId, String newId) {
		entityIdConversion.put(oldId, newId);
	}
    
	private String conversionFor(String oldId) {
		return entityIdConversion.get(oldId);
	}
	
    /**
     * Reads the SchedBlock XML document from an AOT file and creates a SchedBlock
     * object from it.
     * 
     * @param filePath AOT relative file path
     * @return SchedBlock object
     * @throws Exception
     */
    private SchedBlock getSchedBlockFromFile(String filePath) throws Exception {

        String xmlDoc = readCompressedXMLFile(filePath, "SchedBlock0.xml");

        SchedBlock schedBlock = SchedBlock
                .unmarshalSchedBlock(new StringReader(xmlDoc));
        
        SchedBlockEntityT entity = schedBlock.getSchedBlockEntity();
        final String oldId = entity.getEntityId();
        getContainerServices().assignUniqueEntityId(entity);
        rememberConversion(oldId, entity.getEntityId());

        schedBlock.setSchedBlockEntity(entity);
        return schedBlock;
    }

    /**
     * Reads the ObsProject XML document from an AOT file and creates a ObsProject
     * object from it.
     * 
     * @param filePath AOT relative file path
     * @return ObsProject object
     * @throws Exception
     */
    private ObsProject getObsProjectFromFile(String filePath) throws Exception {

        String xmlDoc = readCompressedXMLFile(filePath, "ObsProject.xml");

        ObsProject obsProject = ObsProject
                .unmarshalObsProject(new StringReader(xmlDoc));
        
        ObsProjectEntityT entity = obsProject.getObsProjectEntity();
        final String oldId = entity.getEntityId();
        getContainerServices().assignUniqueEntityId(entity);
        rememberConversion(oldId, entity.getEntityId());

        obsProject.setObsProjectEntity(entity);
        return obsProject;
    }    

    /**
     * Reads the ObsProposal XML document from an AOT file and creates a ObsProposal
     * object from it.
     * 
     * @param filePath AOT relative file path
     * @return ObsProposal object
     * @throws Exception
     */
    private ObsProposal getObsProposalFromFile(String filePath) throws Exception {

        String xmlDoc = readCompressedXMLFile(filePath, "ObsProposal.xml");

        ObsProposal obsProposal = ObsProposal
                .unmarshalObsProposal(new StringReader(xmlDoc));
        
        ObsProposalEntityT entity = obsProposal.getObsProposalEntity();
        final String oldId = entity.getEntityId();
        getContainerServices().assignUniqueEntityId(entity);
        rememberConversion(oldId, entity.getEntityId());

        obsProposal.setObsProposalEntity(entity);
        return obsProposal;
    }    
    
    
    /**
     * Read XML document from a ZIP-compressed file.
     * 
     * @param dirName Directory name
     * @param fileName File name
     * @return XML document
     * @throws Exception
     */
    private String readCompressedXMLFile(String zipFile, String fileName) throws Exception {

        FileInputStream in;
        ZipInputStream zipin;
        StringBuffer xmlDoc;
        
        try {
            in = new FileInputStream(zipFile);
            zipin = new ZipInputStream(in);
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
            return null;
        }
        
        try {
            ZipEntry ze = zipin.getNextEntry();
            while(ze != null && !ze.getName().equals(fileName))
                ze = zipin.getNextEntry();
            if (ze == null) {
                return null;
            }
                        
            InputStreamReader converter = new InputStreamReader(zipin);
            BufferedReader reader = new BufferedReader(converter);
            
            xmlDoc = new StringBuffer();
            String line = reader.readLine();
            while (line != null) {
                xmlDoc.append(line + "\n");
                line = reader.readLine();
            }
            reader.close();            
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        
        return new String(xmlDoc);
    }

    /**
	 * @param args
	 */
	public static void main(String[] args) {
		final ReadAndPrintProject rapp = new ReadAndPrintProject(new FakeContainerServices());
	    final Logger logger = Logger.getLogger(rapp.getClass().getSimpleName());

		try {
		    final File cwd = new File(".");
			final String here = cwd.getCanonicalPath();
			
			logger.info(String.format("Current directory = %s", here));
		} catch (Exception e) {
			logger.warning("Could not get current directory");
			e.printStackTrace();
		}
        final String aotFile = "test/projects/SchedTest.aot";

        ObsProject prj = null;
        ObsProposal prp = null;
        SchedBlock sbl = null;
		try {
			prj = rapp.getObsProjectFromFile(aotFile);
		} catch (Exception e) {
			logger.warning("Could not get ObsProject from aot file");
			e.printStackTrace();
		}
		try {
			prp = rapp.getObsProposalFromFile(aotFile);
		} catch (Exception e1) {
			logger.warning("Could not get ObsProposal from aot file");
			e1.printStackTrace();
		}
		try {
			sbl = rapp.getSchedBlockFromFile(aotFile);
		} catch (Exception e) {
			logger.warning("Could not get SchedBlock from aot file");
			e.printStackTrace();
		}
        
		try {
			logger.info("ObsProject ID = "+prj.getObsProjectEntity().getEntityId());
		} catch (NullPointerException e) {
			logger.info("No ObsProject, ergo no ObsProject ID");
		}
		try {
			logger.info("ObsProposal ID = "+prp.getObsProposalEntity().getEntityId());
		} catch (NullPointerException e) {
			logger.info("No ObsProposal, ergo no ObsProposal ID");
		}
		try {
			logger.info("SchedBlock ID = "+sbl.getSchedBlockEntity().getEntityId());
		} catch (NullPointerException e) {
			logger.info("No SchedBlock, ergo no SchedBlock ID");
		}
		
		final Utils u = new Utils(rapp.getContainerServices(), logger);
		u.convertReferences(prp, rapp.entityIdConversion);
		u.convertReferences(prj, rapp.entityIdConversion);
		u.convertReferences(sbl, rapp.entityIdConversion);
		logger.info(String.format("%n%s%n", u.printProjectHierarchy(prj, prp, sbl)));
		
	    try {
			final Map<String, StatusBaseT> hierarchy = 
				u.makeStatusHierarchy(prj, prp, sbl);
			logger.info(String.format("%n%s%n", u.printProjectHierarchy(prj, prp, sbl)));
		} catch (AcsJContainerServicesEx e) {
			logger.warning("Don't appear to be able to make the status hierarchy");
			e.printStackTrace();
		}

	}

}
