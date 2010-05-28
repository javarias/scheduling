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
    	alma.entity.xmlbinding.valuetypes.types.StatusTStateType.APPROVED.toString(),
    	alma.entity.xmlbinding.valuetypes.types.StatusTStateType.PHASE2SUBMITTED.toString(),
    	alma.entity.xmlbinding.valuetypes.types.StatusTStateType.READY.toString(),              
    	alma.entity.xmlbinding.valuetypes.types.StatusTStateType.PARTIALLYOBSERVED.toString()               
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
                                          logger);
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

	/**
	 * Fetch all the ObsProposals from the archive.
	 * 
	 * @return a <code>List</code> of all the ObsProposals found.
	 */
	private List<alma.entity.xmlbinding.obsproposal.ObsProposal> fetchAllAPDMProposals() {
		List<alma.entity.xmlbinding.obsproposal.ObsProposal> result
				= new Vector<alma.entity.xmlbinding.obsproposal.ObsProposal>();

		String query = new String("/prp:ObsProposal");
		String schema = new String("ObsProposal");
		
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
								"Succesfully got APDM ObsProposal %s",
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
		
		apdmProposals = fetchAllAPDMProposals();
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

}
