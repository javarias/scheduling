/**
 * 
 */
package alma.scheduling.datamodel.obsproject.dao;

import java.util.List;
import java.util.Vector;
import java.util.logging.Logger;

import org.omg.CORBA.UserException;

import alma.acs.entityutil.EntityException;
import alma.entity.xmlbinding.obsproject.ObsUnitSetT;
import alma.scheduling.datamodel.obsproject.ObsProject;
import alma.xmlstore.ArchiveInternalError;
import alma.xmlstore.Cursor;
import alma.xmlstore.CursorPackage.QueryResult;

/**
 * @author dclarke
 *
 */
public class Phase1XMLStoreProjectDao extends AbstractXMLStoreProjectDao {
	
    final public static String[] OPPhase1RunnableStates = {
    	alma.entity.xmlbinding.valuetypes.types.StatusTStateType.PHASE1SUBMITTED.toString(),
//    	alma.entity.xmlbinding.valuetypes.types.StatusTStateType.APPROVED.toString(),
//    	alma.entity.xmlbinding.valuetypes.types.StatusTStateType.PHASE2SUBMITTED.toString(),
//    	alma.entity.xmlbinding.valuetypes.types.StatusTStateType.READY.toString(),              
//    	alma.entity.xmlbinding.valuetypes.types.StatusTStateType.PARTIALLYOBSERVED.toString()               
    };

	public Phase1XMLStoreProjectDao() throws Exception {
		super(Phase1XMLStoreProjectDao.class.getSimpleName());
	}

	@Override
	protected List<ObsProject> convertAPDMProjectsToDataModel(
			ArchiveInterface archive,
			Logger           logger) {
		final APDMtoSchedulingConverter converter =
			new APDMtoSchedulingConverter(archive,
                                          APDMtoSchedulingConverter.Phase.PHASE1,
                                          logger, notifier);
		return converter.convertAPDMProjectsToDataModel();
	}

	/**
	 * Make sure that all the APDM ObsProjects corresponding to the
	 * given APDM ObsProposals are in the given ArchiveInterface's
	 * cache.
	 * 
	 * @param archive
	 * @param apdmProposals
	 */
	protected void getAPDMProjectsFor(
			ArchiveInterface                                     archive,
			List<alma.entity.xmlbinding.obsproposal.ObsProposal> apdmProposals) {
	
		for (alma.entity.xmlbinding.obsproposal.ObsProposal apdmProposal : apdmProposals ) {
			alma.entity.xmlbinding.obsproject.ObsProjectRefT projectRef = apdmProposal.getObsProjectRef();
			String                                           projectId = projectRef.getEntityId();
			try {
				archive.getObsProject(projectId);
				logger.info(String.format(
						"Succesfully got APDM ObsProject %s for APDM ObsProposal %s",
						projectId,
						apdmProposal.getObsProposalEntity().getEntityId()));
			} catch (EntityException e) {
				logger.warning(String.format(
						"Cannot get APDM ObsProject %s for APDM ObsProposal %s - %s",
						projectId,
						apdmProposal.getObsProposalEntity().getEntityId(),
						e.getMessage()));
			} catch (UserException e) {
				logger.warning(String.format(
						"Cannot get APDM ObsProject %s for APDM ObsProposal %s - %s",
						projectId,
						apdmProposal.getObsProposalEntity().getEntityId(),
						e.getMessage()));
			}
		}
	}

	private static String makePredicate(String... states) {
		final StringBuffer sb = new StringBuffer();
		String sep = "";
		
		for (final String state : states) {
			sb.append(sep);
			sb.append("@status=\"");
			sb.append(state);
			sb.append('"');
			sep = " or ";
		}
		
		return sb.toString();
	}

	private static String makeQuery(String... states) {
		return String.format(
				"/prp:ObsProposal/prj:ObsPlan[%s]",
				makePredicate(states));
	}
	
	/**
	 * Fetch all the ObsProposals from the archive.
	 * 
	 * @return a <code>List</code> of all the ObsProposals found.
	 */
	private List<alma.entity.xmlbinding.obsproposal.ObsProposal> fetchInterestingAPDMProposals() {
		List<alma.entity.xmlbinding.obsproposal.ObsProposal> result
				= new Vector<alma.entity.xmlbinding.obsproposal.ObsProposal>();

//		String query = new String("/prp:ObsProposal");
		String query = makeQuery(OPPhase1RunnableStates);
		String schema = new String("ObsProposal");
		
		logger.info(String.format(
				"Getting interesting proposals, query = %s, schema = %s",
				query, schema));
		try {
			Cursor cursor = xmlStore.query(query, schema);
			if (cursor == null) {
				logger.severe(String.format(
						"Cannot get APDM ObsProposals - cursor returned by query is null"));
			} else {
				while (cursor.hasNext()) {
					QueryResult res = cursor.next();
					try {
						alma.entity.xmlbinding.obsproposal.ObsProposal proposal =
							archive.getObsProposal(res.identifier);
						result.add(proposal);
						archive.cache(proposal);
						logger.info(String.format(
								"Succesfully got %d APDM ObsProposal %s", result.size(), 
								res.identifier));
						
					} catch (EntityException e) {
						logger.warning(String.format(
								"Cannot get APDM ObsProposal %s - %s (skipping)",
								res.identifier,
								e.getMessage()));
					} catch (UserException e) {
						logger.warning(String.format(
								"Cannot get APDM ObsProposal %s - %s (skipping)",
								res.identifier,
								e.getMessage()));
					}
				}
				cursor.close();
			}
		} catch(ArchiveInternalError e) {
			logger.severe(String.format(
					"Cannot get APDM ObsProposals - %s",
					e.getMessage()));
		}
		return result;
	}

	@Override
	protected void getInterestingProjects(ArchiveInterface archive) {
		List<alma.entity.xmlbinding.obsproposal.ObsProposal> apdmProposals;
		
		apdmProposals = fetchInterestingAPDMProposals();
		getAPDMProjectsFor(archive, apdmProposals);
	}

	@Override
	protected ObsUnitSetT getTopLevelOUSForProject(
			ArchiveInterface                             archive,
			alma.entity.xmlbinding.obsproject.ObsProject apdmProject) {
		alma.entity.xmlbinding.obsproposal.ObsProposal proposal =
			archive.cachedObsProposal(apdmProject.getObsProposalRef().getEntityId());
		
		return proposal.getObsPlan();
	}

	protected void getAPDMSchedBlocksFor(
			ArchiveInterface                              archive,
			alma.entity.xmlbinding.obsproject.ObsUnitSetT apdmOUS) {
		
		// Get the choice object for convenience
		final alma.entity.xmlbinding.obsproject.ObsUnitSetTChoice choice =
			apdmOUS.getObsUnitSetTChoice();

		if (choice != null) {
			// Recurse down child ObsUnitSetTs
			for (final alma.entity.xmlbinding.obsproject.ObsUnitSetT childOUS : choice.getObsUnitSet()) {
				getAPDMSchedBlocksFor(archive, childOUS);
			}

			// Get any referred SchedBlocks
			for (final alma.entity.xmlbinding.schedblock.SchedBlockRefT childSBRef : choice.getSchedBlockRef()) {
				final String id = childSBRef.getEntityId();
				try {
					archive.getSchedBlock(id);
					logger.info(String.format(
							"Succesfully got APDM SchedBlock %s",
							id));
				} catch (EntityException deserialiseEx) {
					logger.warning(String.format(
							"can not get APDM SchedBlock %s from XML Store - %s, (skipping it)",
							id,
							deserialiseEx.getMessage()));
				} catch (UserException retrieveEx) {
					logger.warning(String.format(
							"can not get APDM SchedBlock %s from XML Store - %s, (skipping it)",
							id,
							retrieveEx.getMessage()));
				}
			}
		} else {
			String projectLabel;
			
			if (apdmOUS.getObsProjectRef() != null) {
				projectLabel = String.format("APDM ObsProject %s", apdmOUS.getObsProjectRef().getEntityId());
			} else {
				projectLabel = "unknown APDM ObsProject";
			}

			logger.warning(String.format(
					"APDM ObsUnitSet %s in %s has no children, (skipping it)",
					apdmOUS.getEntityPartId(),
					projectLabel
			));
		}	
	}
	
    
    /**
	 * Ensure that all the APDM ScheBlocks that correspond to the given
	 * APDM ObsProject are cached in the given ArchiveInterface.
	 * 
     * @param archive
     * @param apdmProject
     */
	@Override
	protected void getAPDMSchedBlocksFor(
			ArchiveInterface                             archive,
			alma.entity.xmlbinding.obsproject.ObsProject apdmProject) {
		
		final alma.entity.xmlbinding.obsproject.ObsUnitSetT top =
			getTopLevelOUSForProject(archive, apdmProject);
		
		getAPDMSchedBlocksFor(archive, top);
	}

	@Override
	protected boolean interestedInObsProject(String whoCares) {
		return true;
	}
	
	
	public static void main(String[] args) {
		String query = makeQuery(OPPhase1RunnableStates);
		System.out.println(query);
		
		final String[] others = {"first", "second", "third"};
		query = makeQuery(others);
		System.out.println(query);

		query = makeQuery();
		System.out.println(query);

	}

}
