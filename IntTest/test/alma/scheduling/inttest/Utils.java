/**
 * 
 */
package alma.scheduling.inttest;

import java.util.Formatter;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import alma.JavaContainerError.wrappers.AcsJContainerServicesEx;
import alma.acs.container.ContainerServices;
import alma.entities.commonentity.EntityRefT;
import alma.entities.commonentity.EntityT;
import alma.entity.xmlbinding.obsproject.ObsProgramT;
import alma.entity.xmlbinding.obsproject.ObsProject;
import alma.entity.xmlbinding.obsproject.ObsProjectRefT;
import alma.entity.xmlbinding.obsproject.ObsUnitSetT;
import alma.entity.xmlbinding.obsproposal.ObsProposal;
import alma.entity.xmlbinding.obsproposal.ObsProposalRefT;
import alma.entity.xmlbinding.ousstatus.OUSStatus;
import alma.entity.xmlbinding.ousstatus.OUSStatusChoice;
import alma.entity.xmlbinding.ousstatus.OUSStatusEntityT;
import alma.entity.xmlbinding.ousstatus.OUSStatusRefT;
import alma.entity.xmlbinding.projectstatus.ProjectStatus;
import alma.entity.xmlbinding.projectstatus.ProjectStatusEntityT;
import alma.entity.xmlbinding.projectstatus.ProjectStatusRefT;
import alma.entity.xmlbinding.projectstatus.StatusBaseT;
import alma.entity.xmlbinding.sbstatus.SBStatus;
import alma.entity.xmlbinding.sbstatus.SBStatusEntityT;
import alma.entity.xmlbinding.sbstatus.SBStatusRefT;
import alma.entity.xmlbinding.schedblock.SchedBlock;
import alma.entity.xmlbinding.schedblock.SchedBlockRefT;
import alma.entity.xmlbinding.valuetypes.StatusT;
import alma.entity.xmlbinding.valuetypes.types.StatusTStateType;

/**
 * @author dclarke
 *
 */
public class Utils {

	/*
	 * ================================================================
	 * Fields
	 * ================================================================
	 */
	/** the container services we should use */
	private ContainerServices container;
	/** the logger we should use */
    private Logger logger;
    /* end Fields
     * ============================================================= */
    
	

	/*
	 * ================================================================
	 * Construction
	 * ================================================================
	 */
	/**
	 * @param container
	 */
	public Utils(ContainerServices container, Logger logger) {
		this.container = container;
		this.logger = logger;
	}
    /* end Construction
     * ============================================================= */
    
	

	/*
	 * ================================================================
	 * getters/setters
	 * ================================================================
	 */
	private ContainerServices getContainerServices() {
		return container;
	}
    /* end getters/setters
     * ============================================================= */
    
	

	/*
	 * ================================================================
	 * Reference Creation
	 * ================================================================
	 */
	/**
     * Populate an entity reference such that it becomes a reference to
     * the supplied entity.
     * 
     * @param ref The reference to populate
     * @param ent The entity to which we wish to refer
     */
    public void populateReference(EntityRefT ref, EntityT ent) {
    	populateReference(ref, ent, null);
 	}
	
    /**
     * Populate an entity reference such that it becomes a reference to
     * a given part within the supplied entity.
     * 
     * @param ref The reference to populate
     * @param ent The entity to which we wish to refer
     * @param partId The id of the entity part we wish to reference
     */
    public void populateReference(EntityRefT ref, EntityT ent, String partId) {
		ref.setDocumentVersion(ent.getDocumentVersion());
		ref.setEntityId(ent.getEntityId());
		ref.setEntityTypeName(ent.getEntityTypeName());
		ref.setPartId(partId);
 	}
    
    /**
     * Populate an entity reference such that it becomes a reference to
     * the same entity to which the other reference refers.
     * 
     * @param ref The reference to populate
     * @param original The entity reference which we wish to clone
     */
    public void populateReference(EntityRefT ref, EntityRefT original) {
    	populateReference(original, ref, null);
 	}
	
    /**
     * Populate an entity reference such that it becomes a reference to
     * a given part within the same entity to which the other reference
     * refers.
     * 
     * @param ref The reference to populate
     * @param ent The entity to which we wish to refer
     * @param partId The id of the entity part we wish to reference
     */
    public void populateReference(EntityRefT ref, EntityRefT original, String partId) {
		ref.setDocumentVersion(original.getDocumentVersion());
		ref.setEntityId(original.getEntityId());
		ref.setEntityTypeName(original.getEntityTypeName());
		ref.setPartId(partId);
 	}
    
    /**
     * Create and populate a reference to the given ObsProject
     * 
     * @param to - the entity to which we need a reference
     * @return a new reference to the supplied entity
     */
    public ObsProjectRefT createReferenceTo(ObsProject to) {
    	final ObsProjectRefT result = new ObsProjectRefT();
    	
    	populateReference(result, to.getObsProjectEntity());
    	return result;
    }
    
    /**
     * Create and populate a reference to the given ObsUnitSetT
     * 
     * @param to - the OUS to which we need a reference
     * @return a new reference to the supplied OUS
     */
    public ObsProjectRefT createReferenceTo(ObsUnitSetT to) {
    	final ObsProjectRefT result = new ObsProjectRefT();
    	
    	populateReference(result, to.getObsProjectRef(), to.getEntityPartId());
    	return result;
    }
    
    /**
     * Create and populate a reference to the given SchedBlock
     * 
     * @param to - the entity to which we need a reference
     * @return a new reference to the supplied entity
     */
    public SchedBlockRefT createReferenceTo(SchedBlock to) {
    	final SchedBlockRefT result = new SchedBlockRefT();
    	
    	populateReference(result, to.getSchedBlockEntity());
    	return result;
    }
    
    /**
     * Create and populate a reference to the given ObsProposal
     * 
     * @param to - the entity to which we need a reference
     * @return a new reference to the supplied entity
     */
    public ObsProposalRefT createReferenceTo(ObsProposal to) {
    	final ObsProposalRefT result = new ObsProposalRefT();
    	
    	populateReference(result, to.getObsProposalEntity());
    	return result;
    }
    
    /**
     * Create and populate a reference to the given ProjectStatus
     * 
     * @param to - the entity to which we need a reference
     * @return a new reference to the supplied entity
     */
    public ProjectStatusRefT createReferenceTo(ProjectStatus to) {
    	final ProjectStatusRefT result = new ProjectStatusRefT();
    	
    	populateReference(result, to.getProjectStatusEntity());
    	return result;
    }
    
    /**
     * Create and populate a reference to the given OUSStatus
     * 
     * @param to - the entity to which we need a reference
     * @return a new reference to the supplied entity
     */
    public OUSStatusRefT createReferenceTo(OUSStatus to) {
    	final OUSStatusRefT result = new OUSStatusRefT();
    	
    	populateReference(result, to.getOUSStatusEntity());
    	return result;
    }
    
    /**
     * Create and populate a reference to the given SBStatus
     * 
     * @param to - the entity to which we need a reference
     * @return a new reference to the supplied entity
     */
    public SBStatusRefT createReferenceTo(SBStatus to) {
    	final SBStatusRefT result = new SBStatusRefT();
    	
    	populateReference(result, to.getSBStatusEntity());
    	return result;
    }
    /* end Reference Creation
     * ============================================================= */
    
	

	/*
	 * ================================================================
	 * Reference Conversion
	 * ================================================================
	 */
    private void convertReference(
    		EntityRefT ref,
    		Map<String, String> dictionary) {
    	if (ref != null &&
    			dictionary.containsKey(ref.getEntityId())) {
    		ref.setEntityId(dictionary.get(ref.getEntityId()));
    	}
    }

    private void convertReferences(
    		ObsUnitSetT ous,
    		Map<String, String> dictionary) {
    	if (ous != null) {
    		convertReference(ous.getObsProjectRef(), dictionary);
    		for (ObsUnitSetT sub : ous.getObsUnitSetTChoice().getObsUnitSet()) {
    			convertReferences(sub, dictionary);
    		}
    		for (SchedBlockRefT sb : ous.getObsUnitSetTChoice().getSchedBlockRef()) {
    			convertReference(sb, dictionary);
    		}
    	}
    }

    public void convertReferences(
    		ObsProject op,
    		Map<String, String> dictionary) {
    	convertReference(op.getObsProposalRef(), dictionary);
    	convertReference(op.getObsReviewRef(), dictionary);
    	convertReference(op.getProjectStatusRef(), dictionary);
    	convertReferences(op.getObsProgram().getObsPlan(), dictionary);
    }

    public void convertReferences(
    		ObsProposal op,
    		Map<String, String> dictionary) {
    	convertReference(op.getDocumentsRef(), dictionary);
    	convertReference(op.getObsProjectRef(), dictionary);
    	convertReferences(op.getObsPlan(), dictionary);
    }

    public void convertReferences(
    		SchedBlock sb,
    		Map<String, String> dictionary) {
    	convertReference(sb.getObsProjectRef(), dictionary);
    	convertReference(sb.getSBStatusRef(), dictionary);
    }

    public void convertReferences(
    		ProjectStatus ps,
    		Map<String, String> dictionary) {
    	convertReference(ps.getObsProjectRef(), dictionary);
    	convertReference(ps.getObsProposalRef(), dictionary);
    	convertReference(ps.getObsProgramStatusRef(), dictionary);
    }

    public void convertReferences(
    		OUSStatus ouss,
    		Map<String, String> dictionary) {
    	convertReference(ouss.getContainingObsUnitSetRef(), dictionary);
    	convertReference(ouss.getObsUnitSetRef(), dictionary);
    	convertReference(ouss.getProjectStatusRef(), dictionary);
    }

    public void convertReferences(
    		SBStatus sbs,
    		Map<String, String> dictionary) {
    	convertReference(sbs.getContainingObsUnitSetRef(), dictionary);
    	convertReference(sbs.getProjectStatusRef(), dictionary);
    	convertReference(sbs.getSchedBlockRef(), dictionary);
    }
    /* end Reference Conversion
     * ============================================================= */
    
	

	/*
	 * ================================================================
	 * Status Object Creation
	 * ================================================================
	 */
    /**
     * Make a new StatusT object which is set to the supplied initial
     * state.
     * 
     * @param initial - the initial state to which the result is set
     * @return The created StatusT
     */
    private StatusT makeStatus(StatusTStateType initial) {
    	final StatusT result = new StatusT();
    	result.setState(initial);
    	return result;
    }
    
    /**
     * Make a new StatusT object which is set to our default initial
     * state (currently PHASE2SUBMITTED)
     * 
     * @return The created StatusT
     */
    private StatusT makeStatus() {
    	return makeStatus(StatusTStateType.PHASE2SUBMITTED);
    }
    
    /**
     * Make a ProjectStatus entity for the given ObsProject. Assigns an
     * entityId to the status object and links it to the domain object.
     * It does not, though, wire the new status object to the existing
     * tree of status objects.
     *  
     * @param prj - the ObsProject for which to create a status
     * @return the resultant status entity
     * @throws AcsJContainerServicesEx
     */
    public ProjectStatus makeProjectStatus(ObsProject prj) throws AcsJContainerServicesEx {
    	// Create the status object and give it an ID
    	final ProjectStatus        result = new ProjectStatus();
    	final ProjectStatusEntityT entity = new ProjectStatusEntityT();
    	result.setProjectStatusEntity(entity);
    	
    	getContainerServices().assignUniqueEntityId(result.getProjectStatusEntity());

    	// Link it (both ways) with the domain object
    	result.setObsProjectRef(createReferenceTo(prj));
    	prj.setProjectStatusRef(createReferenceTo(result));
    	
    	// Populate the new status
    	result.setStatus(makeStatus());
    	
    	result.setName(prj.getProjectName());
    	result.setPI(prj.getPI());
    	
    	return result;
    }
    
    
    /**
     * Make an OUSStatus entity for the given ObsUnitSetT. Assigns an
     * entityId to the status object and links it to the domain object.
     * It does not, though, wire the new status object to the existing
     * tree of status objects.
     *  
     * @param prj - the ObsUnitSetT for which to create a status
     * @return the resultant status entity
     * @throws AcsJContainerServicesEx
     */
    public OUSStatus makeOUSStatus(ObsUnitSetT ous) throws AcsJContainerServicesEx {
    	// Create the status object and give it an ID
    	final OUSStatus        result = new OUSStatus();
    	final OUSStatusEntityT entity = new OUSStatusEntityT();
    	final OUSStatusChoice  choice = new OUSStatusChoice();
    	result.setOUSStatusEntity(entity);
    	result.setOUSStatusChoice(choice);
    	
    	getContainerServices().assignUniqueEntityId(result.getOUSStatusEntity());

    	// Link it with the domain object
    	result.setObsUnitSetRef(createReferenceTo(ous));
    	
    	// Populate the new status
    	result.setStatus(makeStatus());
   	
    	result.setNumberObsUnitSetsCompleted(0);
    	result.setNumberObsUnitSetsFailed(0);
    	result.setNumberSBsCompleted(0);
    	result.setNumberSBsFailed(0);
    	
    	return result;
    }
    
    
    /**
     * Make an SBStatus entity for the given SchedBlock. Assigns an
     * entityId to the status object and links it to the domain object.
     * It does not, though, wire the new status object to the existing
     * tree of status objects.
     *  
     * @param prj - the SchedBlock for which to create a status
     * @return the resultant status entity
     * @throws AcsJContainerServicesEx
     */
    public SBStatus makeSBStatus(SchedBlock sb) throws AcsJContainerServicesEx {
    	// Create the status object and give it an ID
    	final SBStatus        result = new SBStatus();
    	final SBStatusEntityT entity = new SBStatusEntityT();
    	result.setSBStatusEntity(entity);
    	
    	getContainerServices().assignUniqueEntityId(result.getSBStatusEntity());

    	// Link it (both ways) with the domain object
    	result.setSchedBlockRef(createReferenceTo(sb));
    	
    	// Populate the new status
    	result.setStatus(makeStatus());
   	
    	return result;
    }
    /* end Status Object Creation
     * ============================================================= */
    
	

	/*
	 * ================================================================
	 * Status Hierarchy Creation
	 * ================================================================
	 */
    /**
     * Recurse our way down the OUS structure, creating
     * OUSStatus and SBStatus entities as needed, and
     * completing their references to their corresponding
     * ObsUnitSets and SchedBlocks. It seems we also need
     * to create the reference to the ContainingObsUnitSet,
     * and to the ProjectStatus.
     * 
     * @param ps - the ProjectStatus for the ObsProject of which ous is
     *             part
     * @param ous - the ObsUnitSetT for which we need to make a status
     *              hierarchy
     * @param parent - the OUSStatus of which this hierarchy should be
     *                 made a child
     * @param collection - the status entities associated with this
     *                     project (a <code>Map</code> from  EntityId
     *                     to status entity. New entities made in this
     *                     method are added to it.
     * @param sbDictionary - the SBs associated with this project (a
     *                       <code>Map</code> from SB EntityId to SB.
     *                       Unchanged by this method
     * @return the OUSStatus hierarchy for ous and descendants. 
     * @throws AcsJContainerServicesEx
     */
    private OUSStatus makeOUSHierarchy(
    		ProjectStatus            ps,
    		ObsUnitSetT              ous,
    		OUSStatus                parent,
    		Map<String, StatusBaseT> collection,
    		Map<String, SchedBlock>  sbDictionary) throws AcsJContainerServicesEx {
    	
    	// Create the OUSStatus for the current OUS (ous) and add it to
    	// to the collection of Statuses
    	final OUSStatus result = makeOUSStatus(ous);
    	collection.put(result.getOUSStatusEntity().getEntityId(), result);

    	// Point the newly created OUSStatus to its containing OUSStatus
    	// (if there is one) and to the top level ProjectStatus. 
    	result.setProjectStatusRef(createReferenceTo(ps));
    	if (parent != null) {
    		result.setContainingObsUnitSetRef(createReferenceTo(parent));
    	}
    	
    	// Recurse down the ObsUnitSet hierarchy.
		// for each OUS which is a child of ous..
    	for (final ObsUnitSetT subSet : ous.getObsUnitSetTChoice().getObsUnitSet()) {
    		// .. create the sub-hierarchy for it...
    		final OUSStatus subStatus = makeOUSHierarchy(
    				ps, subSet, result, collection, sbDictionary);
    		// .. and make that sub-hierarchy a child of the current status.
    		result.getOUSStatusChoice().addOUSStatusRef(createReferenceTo(subStatus));
    	}
    	
    	// Create any necessary SBStatuses for this ObsUnitSet.
    	// for each SchedBlockRefT which ous holds..
    	for (final SchedBlockRefT sbRef : ous.getObsUnitSetTChoice().getSchedBlockRef()) {
    		try {
    			// ... find the SchedBlock referred to and make an
    			// SBStatus for it. Also point it to its containing
    			// OUSStatus (i.e. the current status) and to the top
    			// level ProjectStatus.
    			final SchedBlock sb = sbDictionary.get(sbRef.getEntityId());
    			final SBStatus subStatus = makeSBStatus(sb);
       			subStatus.setContainingObsUnitSetRef(createReferenceTo(result));
    			subStatus.setProjectStatusRef(createReferenceTo(ps));
    			
    			// Make the new SBStatus a child of the current status.
    			result.getOUSStatusChoice().addSBStatusRef(createReferenceTo(subStatus));
    			
    			// Finally, add the new SBStatus to the dictionary of status object
    	    	collection.put(subStatus.getSBStatusEntity().getEntityId(), subStatus);

     		} catch (NullPointerException e) {
    			// sb == null, I suspect.
    			e.printStackTrace();
    			logger.warning(
    					String.format("NullPointerException trying to create SBStatus for SB %s in OUS %s(%s)",
    							sbRef.getEntityId(),
    							ous.getObsProjectRef().getEntityId(),
    							ous.getEntityPartId()));
    		}
    	}
    	return result;
    }
    
    /**
     * Make and populate the status hierarchy for the given project.
     * 
     * @param project - the top level <code>ObsProject</code> for which
     *                  we are creating a status hierarchy
     * @param proposal - the corresponding <code>ObsProposal</code>
     * @param blocks   - the <code>SchedBlock</code>s in project
     * @return A homogeneous <code>Map</code> of the created status
     *         entities. it is keyed by the EntityId of the status
     *         entity and the status entity is the value.
     * @throws AcsJContainerServicesEx
     */
    public Map<String, StatusBaseT> makeStatusHierarchy(
    		ObsProject project,
    		ObsProposal proposal,
    		SchedBlock... blocks) throws AcsJContainerServicesEx {
    	/*
    	 * Comments tagged with [AB] are from Alan Bridger's Twiki page
    	 * on status hierarchy creation http://almasw.hq.eso.org/almasw/bin/view/HLA/EntityStructureCreation.
    	 */
    	final Map<String, StatusBaseT> result = new HashMap<String, StatusBaseT>();
    	
    	// [AB] Create the ProjectStatus.
    	final ProjectStatus ps = makeProjectStatus(project);
    	result.put(ps.getProjectStatusEntity().getEntityId(), ps);
    	
    	// [AB] fill in its references to ObsProject and ObsProposal.
    	// The reference to ObsProject is set in makeProjectStatus(). 
    	ps.setObsProposalRef(createReferenceTo(proposal));

    	final Map<String, SchedBlock> sbDictionary =
    		new HashMap<String, SchedBlock>();
    	
    	for (SchedBlock sb : blocks) {
    		sbDictionary.put(sb.getSchedBlockEntity().getEntityId(), sb);
    	}
    	
    	// [AB] Create the top level OUSStatus, and complete its
    	// [AB] reference to the ObsPlan identifiable part.
    	// This also adds the created status entities to result.
    	// [AB] *see javadoc comment for makeOUSHierarchy()* 
    	final OUSStatus programStatus = makeOUSHierarchy(
    			ps,
    			project.getObsProgram().getObsPlan(),
    			null,
    			result,
    			sbDictionary);
    	
    	ps.setObsProgramStatusRef(createReferenceTo(programStatus));
    	
    	return result;
    }

    private void print(Formatter f, ObsProposal proposal, String prefix) {
    	f.format("%s<ObsProposal id = %s>%n", prefix,
    			proposal.getObsProposalEntity().getEntityId());
    	f.format("%s</ObsProposal>%n", prefix);
    }

    private void print(Formatter f, ObsUnitSetT ous, String prefix, String role) {
    	f.format("%s<%s partid = %s>%n", prefix, role, 
    			ous.getEntityPartId());
    	for (ObsUnitSetT sub : ous.getObsUnitSetTChoice().getObsUnitSet()) {
    		print(f, sub, prefix + "   ", "ObsUnitSet");
    	}
    	for (SchedBlockRefT sbRef : ous.getObsUnitSetTChoice().getSchedBlockRef()) {
    		f.format("%s<SchedBlockRef id = %s />%n", prefix + "   ", sbRef.getEntityId());
    	}
    	f.format("%s<%s>%n", prefix, role);
    }

    private void print(Formatter f, ObsProgramT program, String prefix) {
    	f.format("%s<ObsProgram>%n", prefix);
    	print(f, program.getObsPlan(), prefix + "   ", "ObsPlan");
    	f.format("%s</ObsProgram>%n", prefix);
    }
    
    /**
     * Print the project hierarchy for the given project.
     * 
     * @param project - the top level <code>ObsProject</code> to which
     *                  <code>statuses</code> apply
     * @param proposal - the corresponding <code>ObsProposal</code>
     * @param blocks   - the <code>SchedBlock</code>s in project
     */
    public String printProjectHierarchy(
    		ObsProject project,
    		ObsProposal proposal,
    		SchedBlock... blocks) {

    	final StringBuilder b = new StringBuilder();
    	final Formatter     f = new Formatter(b);

    	f.format("%n<ObsProject id = %s name = %s>%n",
    			project.getObsProjectEntity().getEntityId(),
    			project.getProjectName());
    	ProjectStatusRefT psRef = project.getProjectStatusRef();
    	if (psRef == null) {
    		f.format("   ProjectStatus: NULL%n");
    	} else {
    		f.format("   ProjectStatus: %s%n", psRef.getEntityId());
    	}
    	print(f, proposal, "   ");
    	print(f, project.getObsProgram(), "   ");
    	b.append("</ObsProject>");

    	return b.toString();
    }

    /**
     * Print the status hierarchy for the given project.
     * 
     * @param statuses - A homogeneous <code>Map</code> of the created
     *                   status entities. it is keyed by the EntityId
     *                   of the status entity and the status entity is
     *                   the value.
     * @param project - the top level <code>ObsProject</code> to which
     *                  <code>statuses</code> apply
     * @param proposal - the corresponding <code>ObsProposal</code>
     * @param blocks   - the <code>SchedBlock</code>s in project
     */
    public void printStatusHierarchy(
    		Map<String, StatusBaseT> statuses,
    		ObsProject project,
    		ObsProposal proposal,
    		SchedBlock... blocks) {
//    	/*
//    	 * Comments tagged with [AB] are from Alan Bridger's Twiki page
//    	 * on status hierarchy creation http://almasw.hq.eso.org/almasw/bin/view/HLA/EntityStructureCreation.
//    	 */
//    	final Map<String, StatusBaseT> result = new HashMap<String, StatusBaseT>();
//    	
//    	// [AB] Create the ProjectStatus.
//    	final ProjectStatus ps = makeProjectStatus(project);
//    	result.put(ps.getProjectStatusEntity().getEntityId(), ps);
//    	
//    	// [AB] fill in its references to ObsProject and ObsProposal.
//    	// The reference to ObsProject is set in makeProjectStatus(). 
//    	ps.setObsProposalRef(createReferenceTo(proposal));
//
//    	final Map<String, SchedBlock> sbDictionary =
//    		new HashMap<String, SchedBlock>();
//    	
//    	for (SchedBlock sb : blocks) {
//    		sbDictionary.put(sb.getSchedBlockEntity().getEntityId(), sb);
//    	}
//    	
//    	// [AB] Create the top level OUSStatus, and complete its
//    	// [AB] reference to the ObsPlan identifiable part.
//    	// This also adds the created status entities to result.
//    	// [AB] *see javadoc comment for makeOUSHierarchy()* 
//    	final OUSStatus programStatus = makeOUSHierarchy(
//    			ps,
//    			project.getObsProgram().getObsPlan(),
//    			null,
//    			result,
//    			sbDictionary);
//    	
//    	ps.setObsProgramStatusRef(createReferenceTo(programStatus));
    }
    /* end Status Hierarchy Creation
     * ============================================================= */

    public String showProjectStatus(ProjectStatus ps) {
    	String result;
    	final StatusT status = ps.getStatus();
    	
    	result = String.format("<ProjectStatus status=%s psid=%16s opref=%16s/>",
    			(status != null)? status.getState(): "null",
    					ps.getProjectStatusEntity().getEntityId(),
    					ps.getObsProjectRef().getEntityId());

			return result;
    }

    public String showSBStatus(SBStatus sbs) {
    	String result;
    	final StatusT status = sbs.getStatus();
    	
    	result = String.format("<SBStatus status=%s sbsid=%16s sbref=%16s remaining=%d/>",
    			(status != null)? status.getState(): "null",
    					sbs.getSBStatusEntity().getEntityId(),
    					sbs.getSchedBlockRef().getEntityId(),
    					sbs.getExecutionsRemaining());

		return result;
    }
    
}
