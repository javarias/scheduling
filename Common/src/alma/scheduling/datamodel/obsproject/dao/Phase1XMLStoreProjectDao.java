/*******************************************************************************
 * ALMA - Atacama Large Millimeter Array
 * Copyright (c) AUI - Associated Universities Inc., 2011
 * (in the framework of the ALMA collaboration).
 * All rights reserved.
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307  USA
 *******************************************************************************/
/**
 * 
 */
package alma.scheduling.datamodel.obsproject.dao;

import java.util.Date;
import java.util.List;
import java.util.Vector;
import java.util.logging.Logger;

import org.omg.CORBA.UserException;

import alma.acs.entityutil.EntityException;
import alma.entity.xmlbinding.obsproject.ObsUnitSetT;
import alma.scheduling.datamodel.obsproject.ObsProject;
import alma.scheduling.utils.SchedulingProperties;
import alma.scheduling.utils.SchedulingProperties.Phase1SBSourceValue;
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
    
    private Phase1SBSourceValue sbLocation;

	public Phase1XMLStoreProjectDao() throws Exception {
		super(Phase1XMLStoreProjectDao.class.getSimpleName());
		
		// The call to getPhase1SBSource() will throw an InvalidPropertyValueException
		// if the user has specified an invalid value for the property. Thus, the rest
		// of the code for importing Phase 1 projects from the XMLStore can assume that
		// the flag is set correctly.
		sbLocation = SchedulingProperties.getPhase1SBSource();
		logger.info("Source for Phase 1 Projects' SchedBlocks will be " + sbLocation);
	}

	@Override
	protected List<ObsProject> convertAPDMProjectsToDataModel(
			ArchiveInterface archive,
			Logger           logger) {
		final APDMtoSchedulingConverter converter =
			new APDMtoSchedulingConverter(archive,
                                          APDMtoSchedulingConverter.Phase.PHASE1,
                                          logger, notifier, bookie);
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
	protected List<alma.entity.xmlbinding.obsproject.ObsProject> getAPDMProjectsFor(
			ArchiveInterface                                     archive,
			List<alma.entity.xmlbinding.obsproposal.ObsProposal> apdmProposals) {
		
		List<alma.entity.xmlbinding.obsproject.ObsProject> result
					= new Vector<alma.entity.xmlbinding.obsproject.ObsProject>();
		
		for (alma.entity.xmlbinding.obsproposal.ObsProposal apdmProposal : apdmProposals ) {
			alma.entity.xmlbinding.obsproject.ObsProjectRefT projectRef = apdmProposal.getObsProjectRef();
			String                                           projectId = projectRef.getEntityId();
			try {
				alma.entity.xmlbinding.obsproject.ObsProject apdmProject = archive.getObsProject(projectId);
				result.add(apdmProject);
				logger.info(String.format(
						"Succesfully got APDM ObsProject %s (%s) for APDM ObsProposal %s",
						apdmProject.getCode(),
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
		
		return result;
	}

	/**
	 * Make sure that all the APDM ObsReviews corresponding to the
	 * given APDM ObsProjects are in the given ArchiveInterface's
	 * cache.
	 * 
	 * @param archive
	 * @param apdmProjects
	 */
	protected void getAPDMReviewsFor(
			ArchiveInterface                                   archive,
			List<alma.entity.xmlbinding.obsproject.ObsProject> apdmProjects) {
		for (alma.entity.xmlbinding.obsproject.ObsProject apdmProject : apdmProjects ) {
			alma.entity.xmlbinding.obsreview.ObsReviewRefT reviewRef = apdmProject.getObsReviewRef();
			if (reviewRef != null) {
				String reviewId = reviewRef.getEntityId();
				try {
					archive.getObsReview(reviewId);
					logger.info(String.format(
							"Succesfully got APDM ObsReview %s for APDM ObsProject %s (%s)",
							reviewId,
							apdmProject.getCode(),
							apdmProject.getObsProjectEntity().getEntityId()));
				} catch (EntityException e) {
					logger.warning(String.format(
							"Cannot get APDM ObsReview %s for APDM ObsProject %s (%s) - %s",
							reviewId,
							apdmProject.getCode(),
							apdmProject.getObsProjectEntity().getEntityId(),
							e.getMessage()));
				} catch (UserException e) {
					logger.warning(String.format(
							"Cannot get APDM ObsReview %s for APDM ObsProject %s (%s) - %s",
							reviewId,
							apdmProject.getCode(),
							apdmProject.getObsProjectEntity().getEntityId(),
							e.getMessage()));
				}
			} else {
				logger.warning(String.format(
						"APDM ObsProject %s (%s) has no APDM ObsReview",
						apdmProject.getCode(),
						apdmProject.getObsProjectEntity().getEntityId()));
			}
		}
	}

	/**
	 * Make sure that where necessary the APDM ObsReviews corresponding
	 * to the given APDM ObsProjects are in the cache of the given
	 * ArchiveInterface's. "Where necessary" means, basically, when we
	 * have been told to look in the ObsProposal first, but that there
	 * are no references to any SchedBlocks in the ObsProposal.
	 * 
	 * @param archive
	 * @param apdmProjects
	 */
	protected void getNecessaryAPDMReviewsFor(
			ArchiveInterface                                   archive,
			List<alma.entity.xmlbinding.obsproject.ObsProject> apdmProjects) {
		for (alma.entity.xmlbinding.obsproject.ObsProject apdmProject : apdmProjects ) {
			
			alma.entity.xmlbinding.obsproposal.ObsProposalRefT proposalRef = apdmProject.getObsProposalRef();
			alma.entity.xmlbinding.obsproposal.ObsProposal     apdmProposal = archive.cachedObsProposal(proposalRef.getEntityId());
			
			if (apdmProposal == null || !hasSchedBlocks(apdmProposal.getObsPlan())) {
				// Either there isn't an ObsProposal, or it has no SchedBlocks, ergo
				// get the ObsReview.
				alma.entity.xmlbinding.obsreview.ObsReviewRefT reviewRef = apdmProject.getObsReviewRef();
				if (reviewRef != null) {
					String reviewId = reviewRef.getEntityId();
					
					try {
						archive.getObsReview(reviewId);
						logger.info(String.format(
								"Succesfully got APDM ObsReview %s for APDM ObsProject %s (%s)",
								reviewId,
								apdmProject.getCode(),
								apdmProject.getObsProjectEntity().getEntityId()));
					} catch (EntityException e) {
						logger.warning(String.format(
								"Cannot get APDM ObsReview %s for APDM ObsProject %s (%s) - %s",
								reviewId,
								apdmProject.getCode(),
								apdmProject.getObsProjectEntity().getEntityId(),
								e.getMessage()));
					} catch (UserException e) {
						logger.warning(String.format(
								"Cannot get APDM ObsReview %s for APDM ObsProject %s (%s) - %s",
								reviewId,
								apdmProject.getCode(),
								apdmProject.getObsProjectEntity().getEntityId(),
								e.getMessage()));
					}
				} else {
					logger.warning(String.format(
							"APDM ObsProject %s (%s) has no APDM ObsReview",
							apdmProject.getCode(),
							apdmProject.getObsProjectEntity().getEntityId()));
				}
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
	private List<alma.entity.xmlbinding.obsproposal.ObsProposal> fetchInterestingAPDMProposals(String cycle) {
		List<alma.entity.xmlbinding.obsproposal.ObsProposal> result
				= new Vector<alma.entity.xmlbinding.obsproposal.ObsProposal>();

//		String query = new String("/prp:ObsProposal");
//		String query = makeQuery(OPPhase1RunnableStates); //No longer useful
		String query = "/prp:ObsProposal[prp:cycle=\""+ cycle +"\"]";
		String schema = new String("ObsProposal");
		Cursor cursor = null;
		
		logger.info(String.format(
				"Getting interesting proposals, query = %s, schema = %s",
				query, schema));
		try {
			cursor = xmlStore.query(query, schema);
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
			}
		} catch(ArchiveInternalError e) {
			logger.severe(String.format(
					"Cannot get APDM ObsProposals - %s",
					e.getMessage()));
		} finally {
			if (cursor != null) {
				cursor.close();
				cursor = null;
			}
		}
		return result;
	}

	/* (non-Javadoc)
	 * @see alma.scheduling.datamodel.obsproject.dao.AbstractXMLStoreProjectDao#getInterestingProjects(alma.scheduling.datamodel.obsproject.dao.ArchiveInterface)
	 */
	@Override
	protected void getInterestingProjects(ArchiveInterface archive) {
		
		List<alma.entity.xmlbinding.obsproposal.ObsProposal> apdmProposals;
		List<alma.entity.xmlbinding.obsproject.ObsProject>   apdmProjects;
		
		Date start = new Date();
		apdmProposals = fetchInterestingAPDMProposals("2012.1");
		Date end = new Date();
		logger.info("Fetching APDM Proposals from archive took " + (end.getTime() - start.getTime()) + " ms");
		
		start = new Date();
		apdmProjects = getAPDMProjectsFor(archive, apdmProposals);
		end = new Date();
		logger.info("Getting APDM Projects from archive took " + (end.getTime() - start.getTime()) + " ms");
		
		switch (sbLocation) {
			case REVIEW_ONLY:
			case REVIEW_THEN_PROPOSAL:
				// In these cases we have to get the ObsReview.
				start = new Date();
				getAPDMReviewsFor(archive, apdmProjects);
				end = new Date();
				logger.info("Getting APDM Reviews from archive took " + (end.getTime() - start.getTime()) + " ms");
				if (sbLocation == Phase1SBSourceValue.REVIEW_ONLY) {
				    setAllLocations(apdmProjects, sbLocation);
				} else {
					setConditionalLocations(apdmProjects, sbLocation);
				}
				break;
			case PROPOSAL_ONLY:
				// Never any need for the ObsReviews.
				logger.info("No need to get APDM Reviews from archive");
			    setAllLocations(apdmProjects, sbLocation);
				break;
			case PROPOSAL_THEN_REVIEW:
				// In these cases we might have to get the ObsReview.
				start = new Date();
				getNecessaryAPDMReviewsFor(archive, apdmProjects); // Sets the locations
				end = new Date();
				logger.info("Getting necessary APDM Reviews from archive took " + (end.getTime() - start.getTime()) + " ms");
				setConditionalLocations(apdmProjects, sbLocation);
				break;
		}
	}

	/**
	 * @param apdmProjects
	 * @param location
	 */
	private void setAllLocations(
			List<alma.entity.xmlbinding.obsproject.ObsProject> apdmProjects,
			Phase1SBSourceValue location) {
		logger.info(String.format(
				"Using APDM %ss as source for Phase 1 SchedBlocks for all APDM ObsProjects",
				location));
		for (alma.entity.xmlbinding.obsproject.ObsProject apdmProject : apdmProjects) {
			archive.rememberPhase1Location(
					apdmProject.getObsProjectEntity().getEntityId(),
					location);
		}
	}

	/**
	 * @param apdmProjects
	 * @param location
	 */
	private void setConditionalLocations(
			List<alma.entity.xmlbinding.obsproject.ObsProject> apdmProjects,
			Phase1SBSourceValue location) {
		
		for (alma.entity.xmlbinding.obsproject.ObsProject apdmProject : apdmProjects) {
			
			final String projectId = apdmProject.getObsProjectEntity().getEntityId();
			final Phase1SBSourceValue resolvedLocation;
			
			switch (location) {
				case REVIEW_THEN_PROPOSAL:
					alma.entity.xmlbinding.obsreview.ObsReviewRefT ref = apdmProject.getObsReviewRef();
					if (ref != null) {
						alma.entity.xmlbinding.obsreview.ObsReview apdmReview = archive.cachedObsReview(ref.getEntityId());
						if (apdmReview != null && hasSchedBlocks(apdmReview.getObsPlan())) {
							resolvedLocation = Phase1SBSourceValue.REVIEW_ONLY;
						} else {
							resolvedLocation = Phase1SBSourceValue.PROPOSAL_ONLY;
						}
					} else {
						// No review found, use the proposal
						resolvedLocation = Phase1SBSourceValue.PROPOSAL_ONLY;
					}
					break;
				case PROPOSAL_THEN_REVIEW:
					alma.entity.xmlbinding.obsproposal.ObsProposalRefT proposalRef = apdmProject.getObsProposalRef();
					if (proposalRef != null) {
						alma.entity.xmlbinding.obsproposal.ObsProposal apdmProposal = archive.cachedObsProposal(proposalRef.getEntityId());
						if (apdmProposal != null && hasSchedBlocks(apdmProposal.getObsPlan())) {
							resolvedLocation = Phase1SBSourceValue.PROPOSAL_ONLY;
						} else {
							resolvedLocation = Phase1SBSourceValue.REVIEW_ONLY;
						}
					} else {
						// No proposal found, use the review
						resolvedLocation = Phase1SBSourceValue.PROPOSAL_ONLY;
					}
					break;
				default:
					// This is a bit weird, shouldn't really get called with these
					// arguments. However, just set the location accordingly.
					resolvedLocation = location;
					break;
			}
			
			if (resolvedLocation == Phase1SBSourceValue.PROPOSAL_ONLY) {
				logger.info(String.format(
						"Using APDM ObsProposal %s as source for Phase 1 SchedBlocks for ObsProject %s (%s)",
						apdmProject.getObsProposalRef().getEntityId(),
						apdmProject.getCode(),
						projectId));
			} else {
				logger.info(String.format(
						"Using APDM ObsReview %s as source for Phase 1 SchedBlocks for ObsProject %s (%s)",
						apdmProject.getObsReviewRef().getEntityId(),
						apdmProject.getCode(),
						projectId));
			}
			archive.rememberPhase1Location(projectId, resolvedLocation);
		}
	}

	@Override
	protected ObsUnitSetT getTopLevelOUSForProject(
			ArchiveInterface                             archive,
			alma.entity.xmlbinding.obsproject.ObsProject apdmProject) {

		alma.entity.xmlbinding.obsproject.ObsPhaseT phase = null;
		
		switch (archive.getPhase1Location(apdmProject.getObsProjectEntity().getEntityId())) {
			case REVIEW_ONLY:
				if (apdmProject.getObsReviewRef() != null)
					phase = archive.cachedObsReview(apdmProject.getObsReviewRef().getEntityId());
				break; 
			case PROPOSAL_ONLY:
				phase = archive.cachedObsProposal(apdmProject.getObsProposalRef().getEntityId());
				break; 
			default:
				return null;
		}
		
		if (phase == null)
			return null;
		return phase.getObsPlan();
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
		
		if (top == null) {
			logger.warning("Could not found ObsUnitSet for ObsProject: " + apdmProject.getObsProjectEntity().getEntityId());
			return;
		}
		
		getAPDMSchedBlocksFor(archive, top);
	}

	@Override
	protected boolean interestedInObsProject(String whoCares) {
		return true;
	}
	

	/**
	 * Look down the given ObsUnitSet for SchedBlock refs, return true
	 * if you find some and false it you don't.
	 *  
	 * @param apdmOUS - the ObsUnitSet to search
	 * @return <code>true</code> if you find a SchedBlockRefT and <code>false</code> if you do not.
	 */
	private boolean hasSchedBlocks(
			alma.entity.xmlbinding.obsproject.ObsUnitSetT apdmOUS) {
		
		alma.entity.xmlbinding.obsproject.ObsUnitSetTChoice
				choice = apdmOUS.getObsUnitSetTChoice();
		
		if (choice == null) {
			// No children at all
			return false;
		}
		
		if (choice.getSchedBlockRefCount() > 0) {
			// Oh look, SchedBlockRefs!
			return true;
		}

		
		for (alma.entity.xmlbinding.obsproject.ObsUnitSetT
					childOUS : choice.getObsUnitSet()) {
			if (hasSchedBlocks(childOUS)) {
				return true;
			}
		}
		
		return false;
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
