/*
 * ALMA - Atacama Large Millimiter Array
 * (c) Associated Universities Inc., 2005 
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307  USA
 *
 * $Id: SchedulingTemplateTest.java,v 1.5 2010/03/13 00:39:57 dclarke Exp $
 */

package alma.scheduling.inttest;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import alma.ACS.MasterComponent;
import alma.ACS.MasterComponentHelper;
import alma.ACS.ROstringSeq;
import alma.ACS.MasterComponentPackage.SubsystemStateEvent;
import alma.ACSErr.Completion;
import alma.ACSErr.CompletionHolder;
import alma.ACSSim.Simulator;
import alma.ACSSim.SimulatorHelper;
import alma.Control.CorrelatorType;
import alma.acs.component.client.ComponentClientTestCase;
import alma.acs.container.ContainerServices;
import alma.acs.container.archive.UIDLibrary;
import alma.acs.entityutil.EntitySerializer;
import alma.entity.xmlbinding.obsproject.ObsProject;
import alma.entity.xmlbinding.obsproject.ObsProjectEntityT;
import alma.entity.xmlbinding.obsproposal.ObsProposal;
import alma.entity.xmlbinding.obsproposal.ObsProposalEntityT;
import alma.entity.xmlbinding.schedblock.SchedBlock;
import alma.entity.xmlbinding.schedblock.SchedBlockEntityT;
import alma.scheduling.ArrayModeEnum;
import alma.scheduling.Interactive_PI_to_Scheduling;
import alma.scheduling.MasterSchedulerIF;
import alma.xmlentity.XmlEntityStruct;
import alma.xmlstore.ArchiveConnection;
import alma.xmlstore.Identifier;
import alma.xmlstore.IdentifierJ;
import alma.xmlstore.IdentifierOperations;
import alma.xmlstore.Operational;

/**
 * Template for Scheduling tests.
 * 
 */
public class SchedulingTemplateTest extends ComponentClientTestCase {

	private ContainerServices container;
    private Logger logger;
    private Simulator simulator;
    private Utils utils;

    private ArchiveConnection archConnectionComp;
    private Operational archOperational;
    private Identifier archIdentifierComp;
    
    private MasterComponent schedulingMC;
    private MasterSchedulerIF masterScheduler;
    
    public SchedulingTemplateTest() throws Exception {
        super(SchedulingTemplateTest.class.getName());
    }

    /**
     * Test case fixture setup.
     */
    protected void setUp() throws Exception {
        super.setUp();

        container = getContainerServices();
        logger = container.getLogger();
        utils = new Utils(container, logger);

        archConnectionComp = alma.xmlstore.ArchiveConnectionHelper.narrow(
                container.getComponent("ARCHIVE_CONNECTION"));
        
        archIdentifierComp = alma.xmlstore.IdentifierHelper.narrow(
                container.getComponent("ARCHIVE_IDENTIFIER"));

        archOperational = archConnectionComp
                .getOperational("ObservationTest");
        assertNotNull(archOperational);
        
        simulator = 
            SimulatorHelper.narrow(container.getDefaultComponent("IDL:alma/ACSSim/Simulator:1.0"));

        logger.info("Initializing SCHEDULING...");
        schedulingMC = MasterComponentHelper.narrow(container.getComponent("SCHEDULING_MASTER_COMP"));
        schedulingMC.doTransition(SubsystemStateEvent.SUBSYSEVENT_INITPASS1);
        if (!waitForSubsystemState(schedulingMC.currentStateHierarchy(), "AVAILABLE.OFFLINE.PREINITIALIZED", 300)) fail();
        schedulingMC.doTransition(SubsystemStateEvent.SUBSYSEVENT_INITPASS2);
        if (!waitForSubsystemState(schedulingMC.currentStateHierarchy(), "AVAILABLE.ONLINE", 300)) fail();
        
    }

    /**
     * Test case fixture clean up.
     */
    protected void tearDown() throws Exception {
        logger.info("Shutting down SCHEDULING...");
        schedulingMC.doTransition(SubsystemStateEvent.SUBSYSEVENT_SHUTDOWNPASS1);
        if (waitForSubsystemState(schedulingMC.currentStateHierarchy(), "AVAILABLE.OFFLINE.PRESHUTDOWN", 300)) {
            schedulingMC.doTransition(SubsystemStateEvent.SUBSYSEVENT_SHUTDOWNPASS2);
            waitForSubsystemState(schedulingMC.currentStateHierarchy(), "AVAILABLE.OFFLINE.SHUTDOWN", 300);
        }
        container.releaseComponent(archConnectionComp.name());
        container.releaseComponent(archIdentifierComp.name());
        container.releaseComponent(simulator.name());
        super.tearDown();
    }

    public void testSomething() throws Exception {
        ProjectInfo pinfo = storeProject("projects/01SBin1OUS.aot");
    	
    	masterScheduler = alma.scheduling.MasterSchedulerIFHelper.narrow(
                container.getComponent("SCHEDULING_MASTERSCHEDULER"));
        String arrayName = masterScheduler.createArray(
        		new String[] {"DV01"},
                new String[] {"PhotonicReference1"},
                CorrelatorType.BL,
                ArrayModeEnum.INTERACTIVE);
        logger.info("Array name: "+arrayName);
        
        logger.info("Creating Scheduler");
        String schedulerName = masterScheduler.startInteractiveScheduling1(arrayName);
        Interactive_PI_to_Scheduling scheduler =
            alma.scheduling.Interactive_PI_to_SchedulingHelper.narrow(
                container.getComponent(schedulerName));
        
        logger.info("Executing scheduling block(scheduler.startSession)");
        scheduler.startSession(pinfo.getPI(), pinfo.getProjectID());
        logger.info("ExecuteSB:");
        scheduler.executeSB(pinfo.getSchedBlockID());
        logger.info("SetCurrentSB:");
        scheduler.setCurrentSB(pinfo.getSchedBlockID());
        
        container.releaseComponent(scheduler.name());
    	masterScheduler.destroyArray(arrayName);
        container.releaseComponent(masterScheduler.name());
        deleteProject(pinfo);

    }    

    /**
     * Waits for the subystems property to reach a given state.
     * @param stateProp Subsystem Master state property
     * @param expected Expected state
     * @param timeout timeout in seconds
     * 
     */
    private boolean waitForSubsystemState(ROstringSeq stateProp, String expected, int timeout)
        throws Exception {
        
        String state = "";
        int sleepInterval = 1000;
        int timeoutCount = (int) 1000.0 * timeout / sleepInterval;
        int count = 0;
        logger.info("Waiting for subsystem to reach state "+expected+". Timeout is " +timeout+" (s).");
        do {
            Thread.sleep(sleepInterval);
            count++;
            Completion c = new Completion(0, 0, 0, new alma.ACSErr.ErrorTrace[] {});
            CompletionHolder ch = new CompletionHolder(c);
            String[] substates = stateProp.get_sync(ch);
            
            state = "";
            for (String s : substates)
                state += s+".";
            state = state.substring(0, state.length()-1);
            // logger.info("Current state is " + state);
            if (state.equals("AVAILABLE.ERROR")) {
                logger.severe("Subsystem went to error state");
                return false;
            }
        } while(!state.equals(expected) && count < timeoutCount);
        if (!state.equals(expected)) {
            logger.severe("Timeout waiting for state "+expected+"; real state is "+state);
            return true;
        }
        logger.info("Subsystem state is now "+state);
        return true;
    }

    /**
     * A little class to hold the UIDs of the different entities that
     * are related with an AOT project.
     */
    private class ProjectInfo {
        private String schedBlockID;
        private String projectID;
        private String proposalID;
        private String PI;
        public ProjectInfo(String sblID, String prjID, String propID, String pi) {
            schedBlockID = sblID;
            projectID = prjID;
            proposalID = propID;
            PI = pi;
        }
        public String getSchedBlockID() {
            return schedBlockID;
        }
        public String getProjectID() {
            return projectID;
        }
        public String getProposalID() {
            return proposalID;
        }
        public String getPI() {
            return PI;
        }
    }
    
    /**
     * Reads an AOT project from a file and stores it into the ARCHIVE.
     * This function does the same thing that import-save.py ALMA-OT batch script
     * does.
     * TODO: some of this logic needs to be completed. For now it will work for
     * simple projects with a single scheduling block.
     * @param aotFile AOT relative file name
     */
    private ProjectInfo storeProject(String aotFile) throws Exception {
        
        ObsProject prj = getObsProjectFromFile(aotFile);
        ObsProposal prp = getObsProposalFromFile(aotFile);
        SchedBlock sbl = getSchedBlockFromFile(aotFile);
        
        logger.info("SchedBlock ID = "+sbl.getSchedBlockEntity().getEntityId());
        logger.info("ObsProject ID = "+prj.getObsProjectEntity().getEntityId());
        logger.info("ObsProposal ID = "+prp.getObsProposalEntity().getEntityId());

        String schedblockId = sbl.getSchedBlockEntity().getEntityId();
        String projectId = prj.getObsProjectEntity().getEntityId();
        String proposalId = prp.getObsProposalEntity().getEntityId();
        String principalInvestigatorId = prj.getPI();
        sbl.getObsProjectRef().setEntityId(projectId);
        
        prj.getObsProposalRef().setEntityId(proposalId);
        prj.getObsProgram().getObsPlan().getObsProjectRef().setEntityId(projectId);
        // TODO: this should be recursive
        for (int i=0; i<prj.getObsProgram().getObsPlan().getObsUnitSetTChoice()
                        .getObsUnitSetCount(); i++) {
            prj.getObsProgram().getObsPlan().getObsUnitSetTChoice()
            .getObsUnitSet(i).getObsProjectRef().setEntityId(projectId);
            for (int j = 0; j < prj.getObsProgram().getObsPlan().getObsUnitSetTChoice()
            .getObsUnitSet(i).getObsUnitSetTChoice().getSchedBlockRefCount(); j++) {
                prj.getObsProgram().getObsPlan().getObsUnitSetTChoice()
                .getObsUnitSet(i).getObsUnitSetTChoice().getSchedBlockRef(i)
                .setEntityId(schedblockId);
            }
        }

        for (int i=0; i<prj.getObsProgram().getObsPlan().getObsUnitSetTChoice()
                        .getSchedBlockRefCount(); i++) {
            prj.getObsProgram().getObsPlan().getObsUnitSetTChoice()
            .getSchedBlockRef(i).setEntityId(schedblockId);
        }
        
        prp.getObsProjectRef().setEntityId(prj.getObsProjectEntity().getEntityId());
        sbl.getSchedBlockEntity().setDocumentVersion("1.0");
        
        logger.fine("\n\n\n\n");
        logger.fine(utils.printProjectHierarchy(prj, prp, sbl));
        logger.fine("\n\n\n\n");
//        Map<String, StatusBaseT> statuses = utils.makeStatusHierarchy(prj, prp, sbl);
        
        EntitySerializer serializer = EntitySerializer.getEntitySerializer(logger);
        XmlEntityStruct sblent = serializer.serializeEntity(sbl, sbl.getSchedBlockEntity());
        logger.fine("SchedBlock doc: " + sblent.xmlString);
        archOperational.store(sblent);
        XmlEntityStruct prjent = serializer.serializeEntity(prj, prj.getObsProjectEntity());
        logger.fine("ObsPrject doc: " + prjent.xmlString);
        archOperational.store(prjent);
        XmlEntityStruct prpent = serializer.serializeEntity(prp, prp.getObsProposalEntity());
        logger.fine("ObsProposal doc: " + prpent.xmlString);
        archOperational.store(prpent);
        
        return new ProjectInfo(schedblockId, projectId, proposalId, principalInvestigatorId);
    }

    /**
     * Deletes a project in the ARCHIVE.
     * @param pinfo Project information
     */
    private void deleteProject(ProjectInfo pinfo) throws Exception {
        archOperational.delete(pinfo.getSchedBlockID());
        archOperational.delete(pinfo.getProjectID());
        archOperational.delete(pinfo.getProposalID());
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
        UIDLibrary uidlib = new UIDLibrary(logger);
        uidlib.replaceUniqueEntityId(entity, getContainerServices()
                .getTransparentXmlComponent(IdentifierJ.class,
                        archIdentifierComp, IdentifierOperations.class));

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
        UIDLibrary uidlib = new UIDLibrary(logger);
        uidlib.replaceUniqueEntityId(entity, getContainerServices()
                .getTransparentXmlComponent(IdentifierJ.class,
                        archIdentifierComp, IdentifierOperations.class));

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
        UIDLibrary uidlib = new UIDLibrary(logger);
        uidlib.replaceUniqueEntityId(entity, getContainerServices()
                .getTransparentXmlComponent(IdentifierJ.class,
                        archIdentifierComp, IdentifierOperations.class));

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
//    
//    /**
//     * Populate an entity reference such that it becomes a reference to
//     * the supplied entity.
//     * 
//     * @param ent The entity to which we wish to refer
//     * @param ref The reference to populate
//     */
//    private void populateReference(EntityT ent, EntityRefT ref) {
//    	populateReference(ent, ref, null);
// 	}
//	
//    /**
//     * Populate an entity reference such that it becomes a reference to
//     * the a given part within the supplied entity.
//     * 
//     * @param ent The entity to which we wish to refer
//     * @param ref The reference to populate
//     * @param partId The id of the entity part we wish to reference
//     */
//    private void populateReference(EntityT ent, EntityRefT ref, String partId) {
//		ref.setDocumentVersion(ent.getDocumentVersion());
//		ref.setEntityId(ent.getEntityId());
//		ref.setEntityTypeName(ent.getEntityTypeName());
//		ref.setPartId(partId);
// 	}
//    
//    /**
//     * Make a new StatusT object which is set to our default initial
//     * state (currently PHASE2SUBMITTED)
//     * 
//     * @return The created StatusT
//     */
//    private StatusT makeStatus() {
//    	final StatusT result = new StatusT();
//    	result.setState(StatusTStateType.PHASE2SUBMITTED);
//    	return result;
//    }
//    
//    /**
//     * Make a ProjectStatus entity for the given ObsProject. Assigns an
//     * entityId to the status object and links it to the domain object.
//     * It does not, though, wire the new status object to the existing
//     * tree of status objects.
//     *  
//     * @param prj - the ObsProject for which to create a status
//     * @return the resultant status entity
//     * @throws AcsJContainerServicesEx
//     */
//    private ProjectStatus makeProjectStatus(ObsProject prj) throws AcsJContainerServicesEx {
//    	final ProjectStatus        result = new ProjectStatus();
//    	final ProjectStatusEntityT ent    = result.getProjectStatusEntity();
//    	final ObsProjectRefT       ref    = new ObsProjectRefT();
//    	
//    	getContainerServices().assignUniqueEntityId(ent);
//    	populateReference(ent, ref);
//    	result.setObsProjectRef(ref);
//    	result.setStatus(makeStatus());
//    	
//    	result.setName(prj.getProjectName());
//    	result.setPI(prj.getPI());
//    	
//    	return result;
//    }
//    
//    
//    /**
//     * Make an OUSStatus entity for the given ObsUnitSetT. Assigns an
//     * entityId to the status object and links it to the domain object.
//     * It does not, though, wire the new status object to the existing
//     * tree of status objects.
//     *  
//     * @param prj - the ObsUnitSetT for which to create a status
//     * @return the resultant status entity
//     * @throws AcsJContainerServicesEx
//     */
//    private OUSStatus makeOUSStatus(ObsUnitSetT ous) throws AcsJContainerServicesEx {
//    	final OUSStatus        result = new OUSStatus();
//    	final OUSStatusEntityT ent    = result.getOUSStatusEntity();
//    	final ObsProjectRefT   ref    = new ObsProjectRefT();
//    	
//    	getContainerServices().assignUniqueEntityId(ent);
//    	populateReference(ent, ref, ous.getEntityPartId());
//    	result.setObsUnitSetRef(ref);
//    	result.setStatus(makeStatus());
//   	
//    	result.setNumberObsUnitSetsCompleted(0);
//    	result.setNumberObsUnitSetsFailed(0);
//    	result.setNumberSBsCompleted(0);
//    	result.setNumberSBsFailed(0);
//    	
//    	return result;
//    }
//    
//    
//    /**
//     * Make an SBStatus entity for the given SchedBlock. Assigns an
//     * entityId to the status object and links it to the domain object.
//     * It does not, though, wire the new status object to the existing
//     * tree of status objects.
//     *  
//     * @param prj - the SchedBlock for which to create a status
//     * @return the resultant status entity
//     * @throws AcsJContainerServicesEx
//     */
//    private SBStatus makeSBStatus(SchedBlock sb) throws AcsJContainerServicesEx {
//    	final SBStatus        result = new SBStatus();
//    	final SBStatusEntityT ent    = result.getSBStatusEntity();
//    	final SchedBlockRefT  ref    = new SchedBlockRefT();
//    	
//    	getContainerServices().assignUniqueEntityId(ent);
//    	populateReference(ent, ref);
//    	result.setSchedBlockRef(ref);
//    	result.setStatus(makeStatus());
//   	
//    	return result;
//    }
}

