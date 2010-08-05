package alma.scheduling.AlmaScheduling;

import java.io.StringWriter;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Vector;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;

import alma.SchedulingExceptions.wrappers.AcsJObsProjectRejectedEx;
import alma.acs.container.ContainerServices;
import alma.acs.logging.AcsLogger;
import alma.alarmsystem.source.ACSAlarmSystemInterface;
import alma.alarmsystem.source.ACSAlarmSystemInterfaceFactory;
import alma.alarmsystem.source.ACSFaultState;
import alma.entities.commonentity.EntityRefT;
import alma.entity.xmlbinding.obsproject.ObsProject;
import alma.entity.xmlbinding.ousstatus.OUSStatusChoice;
import alma.entity.xmlbinding.ousstatus.OUSStatusRefT;
import alma.entity.xmlbinding.schedblock.SchedBlock;
import alma.entity.xmlbinding.valuetypes.types.StatusTStateType;
import alma.lifecycle.persistence.domain.StateEntityType;
import alma.projectlifecycle.StateChangeData;
import alma.scheduling.ProjectLite;
import alma.scheduling.SBLite;
import alma.scheduling.AlmaScheduling.statusIF.OUSStatusI;
import alma.scheduling.AlmaScheduling.statusIF.ProjectStatusI;
import alma.scheduling.AlmaScheduling.statusIF.SBStatusI;
import alma.scheduling.AlmaScheduling.statusImpl.CachedStatusFactory;
import alma.scheduling.Define.DateTime;
import alma.scheduling.Define.Program;
import alma.scheduling.Define.Project;
import alma.scheduling.Define.ProjectQueue;
import alma.scheduling.Define.SB;
import alma.scheduling.Define.SBQueue;
import alma.scheduling.Define.SchedulingException;
import alma.scheduling.utils.Profiler;

/**
 * I have taken the ArchivePoller that is in the ALMAProjectManager as
 * a nested class and separated it with the purpose of optimizing the
 * loading of ObsProjects and SchedBlocks.
 * 
 * Between other things, I found that the queries were being executed twice
 * in different places. I modified the class so the results of the first
 * round of queries are cached and used afterwards.
 * 
 * The other purpose of pulling out this class is to run it separetely from
 * the SCHEDULING subsystem, to facilitate testing/debugging.
 * 
 * I tried to avoid as much as possible to cut and paste code from other
 * places, but it was not always possible. We should address this when we
 * refactor the subsystem (hopefully soon).
 * 
 * @author rhiriart
 *
 */
public class ALMAArchivePoller {

    /**
     * We are interested in ObsProjects with the following states -
     * getting them from the archive and showing them to the user.
     */
   final private static String[] OPInterestingStates = {
        StatusTStateType.READY.toString(),              
        StatusTStateType.PARTIALLYOBSERVED.toString(),          
        StatusTStateType.CSVREADY.toString()             
    };
    /**
     * We are interested in SchedBlocks with the following states -
     * getting them from the archive and showing them to the user.
     */
    final private static String[] SBInterestingStates = {
        StatusTStateType.READY.toString(),            
        StatusTStateType.RUNNING.toString(),          
        StatusTStateType.CSVREADY.toString(),            
        StatusTStateType.CSVRUNNING.toString()             
    };
    /**
     * We can execute SchedBlocks with the following states.
     */
    final private static String[] SBRunnableStates = {
        StatusTStateType.READY.toString(),             
        StatusTStateType.CSVREADY.toString()             
    };
    /*
     * Set versions of the above.
     */
    public static Set<String> opInterestingStates = toSet(OPInterestingStates);
    public static Set<String> sbInterestingStates = toSet(SBInterestingStates);
    public static Set<String> sbRunnableStates    = toSet(SBRunnableStates);
    
    private AcsLogger                   logger;
    private SBQueue                     sbQueue;
    private ProjectQueue                projectQueue;
    private StatusEntityQueueBundle     statusQs;
    private ProjectUtil                 projectUtil;
    private ALMAArchive                 archive;
    private ALMAClock                   clock;
    private Map<String, SchedBlock[]>   schedBlockBuffer;
    private Map<String, ObsProject>     obsProjectBuffer;
    private LiteFactory                 liteFactory;
    
    /**
     * Constructor to be used when creating the ALMAArchivePoller in the
     * ALMAProjectManager. In this case most of the classes that the ALMAArchivePoller
     * needs have been already created.
     * 
     * @param archive The ARCHIVE interface class
     * @param sbQueue Scheduling block queue
     * @param projectQueue ObsProject queue
     * @param statusQs The queue for all statuses (ObsProject, ObsUnitSet, and SchedBlock)
     * @param projectUtil Project utilities
     * @param clock Clock utility
     * @param logger ACS Logger
     */
    public ALMAArchivePoller(ALMAArchive archive, SBQueue sbQueue,
            ProjectQueue projectQueue, StatusEntityQueueBundle statusQs,
            ProjectUtil projectUtil, ALMAClock clock, AcsLogger logger) {
        this.logger = logger;
        this.sbQueue = sbQueue;
        this.projectQueue = projectQueue;
        this.statusQs = statusQs;
        this.clock = clock;
        this.archive = archive;
        this.projectUtil = projectUtil;
        schedBlockBuffer = new HashMap<String, SchedBlock[]>();
        obsProjectBuffer = new HashMap<String, ObsProject>();
        CachedStatusFactory.getInstance().setStatusQueue(this.statusQs);
        liteFactory = new LiteFactory();
    }
    
    /**
     * Constructor to be used when creating the ALMAArchivePoller in a client
     * application.
     * 
     * @param container ACS ContainerServices
     * @throws Exception
     */
    public ALMAArchivePoller(ContainerServices container) throws Exception {
        this.logger = container.getLogger();
        this.sbQueue = new SBQueue();
        this.projectQueue = new ProjectQueue();
        this.statusQs = new StatusEntityQueueBundle(logger);
        this.clock = new ALMAClock();
        this.archive = new ALMAArchive(container, clock);
        this.projectUtil = archive.getProjectUtil();
        schedBlockBuffer = new HashMap<String, SchedBlock[]>();
        obsProjectBuffer = new HashMap<String, ObsProject>();
        CachedStatusFactory.getInstance().setStatusQueue(this.statusQs);        
        liteFactory = new LiteFactory();
    }

    public void pollArchive() throws SchedulingException {
        pollArchive(null);
    }

    private static Set<String> toSet(String[] strings) {
    	final Set<String> result = new HashSet<String>();
    	for (final String string : strings) {
    		result.add(string);
    	}
    	return result;
    }


    
    /*
     * ================================================================
     * The initial poll of the archive
     * ================================================================
     */
    // Some of these methods are also used during the incremental poll.
	/**
	 * Clear the pools of domain and status objects
	 */
	private void clearPools() {
        statusQs.clear();
        sbQueue.clear();
        projectQueue.clear();
	}
    
	/**
	 * Clear the buffers used during polling of the archive
	 */
	private void clearBuffers() {
        schedBlockBuffer.clear();
        obsProjectBuffer.clear();
	}
    
    /**
	 * Work out the EntityId of the ObsProject to which the SchedBlock
	 * corresponding to the given SBStatusI is affiliated.
	 * 
     * @param sbs - the status of the SchedBlock in who's project we're
     *              interested.
     * @param statusQs - all the various status entities through which
     *                   to navigate.
     * @return - the resultant ObsProject Id (or null if something went
     *           wrong - in which case we log something pertinent).
     */
    private String findProjectId(SBStatusI               sbs,
    		                     StatusEntityQueueBundle statusQs) {
    	String         psId = sbs.getProjectStatusRef().getEntityId();
    	ProjectStatusI ps   = statusQs.getProjectStatusQueue().get(psId);
    	String result;
    	
    	try {
    		result = ps.getDomainEntityId();
    	} catch (NullPointerException e) {
    		result = null;
    	}
    	
    	return result;
	}

	/**
	 * Get, from the Archive, the ObsProjects corresponding to the
	 * ProjectStatusIs in the supplied StatusEntityQueueBundle. Put
	 * them into the supplied Map. Report any errors, but carry on
	 * anyway. 
	 * 
	 * @param statusQs
	 * @param projectSchedBlocks
	 */
	private void getObsProjects(
			final StatusEntityQueueBundle statusQs,          
			final Map<String, ObsProject> obsProjectBuffer) {
		
		for (final ProjectStatusI ps : statusQs.getProjectStatusQueue().getAll()) {
        	final String projectId = ps.getDomainEntityId();

        	logger.fine(String.format(
        			"getting ObsProject %s for ProjectStatus %s",
        			projectId, ps.getUID()));
			try {
				final ObsProject obsProject = archive.getObsProject(projectId);
	        	obsProjectBuffer.put(projectId, obsProject);
			} catch (SchedulingException e) {
				logger.warning(String.format(
						"Could not get ObsProject %s from the archive - %s",
						projectId,
						e.getMessage()));
			}
        }
	}

	/**
	 * Map the given ObsProject and SchedBlocks in a Project and some
	 * SBs, then add the new objects to the project and SB queues.
	 * 
	 * @param projectId
	 * @param op
	 * @param sb
	 * @param ps
	 * @param now
	 */
	private void mapAndAddProject(final String         projectId,
			                      final ObsProject     op,
			                      final SchedBlock[]   sb,
			                      final ProjectStatusI ps,
			                      final DateTime       now) {
		Project project;
		
		// Map the project
		try {
			logger.info(String.format(
					"Mapping ObsProject %s and %d SchedBlock%s",
					projectId,
					sb.length,
					(sb.length == 1)? "": "s"));
			project = projectUtil.map(op, sb, ps, now);

			if (project != null) {
				// Add the objects to the queues
				projectQueue.add(project);
				sbQueue.add(project.getAllSBs());
			} else {
				logger.warning(String.format(
						"Skipping ObsProject %s",
						projectId));
			}
		} catch (SchedulingException e) {
			logger.warning(String.format(
					"Could not map ObsProject %s - %s",
					projectId,
					e.getMessage()));
		}
	}

	/**
	 * Get, from the Archive, the SchedBlocks corresponding to the
	 * SBStatusIs in the supplied StatusEntityQueueBundle. Don't get
	 * SchedBlocks for ObsProjects which are not in the supplied Map,
	 * obsProjectBuffer. Bundle the SchedBlocks up by ObsProject and
	 * put them into schedBlockBuffer. Report any errors, but carry on
	 * anyway. 
	 * 
     * @param statusQs
     * @param obsProjectBuffer
     * @param schedBlockBuffer
     */
    private void getSchedBlocks(StatusEntityQueueBundle statusQs,
			Map<String, ObsProject> obsProjectBuffer,
			Map<String, SchedBlock[]> schedBlockBuffer) {
    	
    	// Variable to make bundling of the SchedBlocks easier.
    	final Map<String, Set<SchedBlock>> bundles
    						= new HashMap<String, Set<SchedBlock>>();
    	for (final String pid: obsProjectBuffer.keySet()) {
    		bundles.put(pid, new HashSet<SchedBlock>());
    	}
    	
    	
    	// Now actually get the things...
        for (final SBStatusI sbs : statusQs.getSBStatusQueue().getAll()) {
        	final String sbId      = sbs.getDomainEntityId();
        	final String projectId = findProjectId(sbs, statusQs);

        	if (obsProjectBuffer.containsKey(projectId)) {
        		final SchedBlock schedBlock = archive.getSchedBlock(sbId);
        		bundles.get(projectId).add(schedBlock);
        	} else {
				logger.warning(String.format(
						"Not getting SchedBlock %s from archive as we didn't get its containing ObsProject %s",
						sbId,
						projectId));
        	}
        }
        
        // Finally, convert the bundles of SchedBlocks to arrays (sigh!).
    	for (final String pid: bundles.keySet()) {
    		final Set<SchedBlock> bundle = bundles.get(pid);
    		schedBlockBuffer.put(pid, bundle.toArray(new SchedBlock[0]));
    	}
	}
	
    /**
     * Get all projects that we're interested in, starting afresh.
     * 
     * @param now - timestamp to use for any created objects.
     * @throws SchedulingException
     */
    synchronized public void initialPollArchive(DateTime now) {
     	logger.info("Starting initial poll of the archive");
       
        clearPools();
        clearBuffers();
        
        // Get the status objects for domain objects in which we're
        // interested.
        try {
			final StatusEntityQueueBundle newQs
				= archive.determineRunnablesByStatus(
						            OPInterestingStates,
									SBInterestingStates);
			statusQs.updateWith(newQs);
		} catch (SchedulingException e) {
			logger.warning(String.format(
					"Unable to get Status Entities from State Archive - %s",
					e.getMessage()));
		}
		
        archive.setProjectUtilStatusQueue(statusQs);
        logger.info(String.format(
        		"Found %d ProjectStatus%s, %d OUSStatus%s and %d SBStatus%s",
        		statusQs.getProjectStatusQueue().size(),
        		(statusQs.getProjectStatusQueue().size() == 1)? "": "es",
                statusQs.getOUSStatusQueue().size(),
                (statusQs.getOUSStatusQueue().size() == 1)? "": "es",
                statusQs.getSBStatusQueue().size(),
                (statusQs.getSBStatusQueue().size() == 1)? "": "es"));
     
        // Now get all the corresponding domain objects.
        getObsProjects(statusQs, obsProjectBuffer);
        getSchedBlocks(statusQs, obsProjectBuffer, schedBlockBuffer);
        
        // Map the domain objects to the scheduling.Define stuff
        for (final ProjectStatusI ps : statusQs.getProjectStatusQueue().getAll()) {
        	final String projectId = ps.getDomainEntityId();
        	
        	if (obsProjectBuffer.containsKey(projectId)) {
        		final ObsProject op = obsProjectBuffer.get(projectId);
        		final SchedBlock[] sb = schedBlockBuffer.get(projectId);
        		
        		mapAndAddProject(projectId, op, sb, ps, now);
        	}
        }
    }
    /* End The initial poll of the archive
     * ============================================================= */

    

	/*
     * ================================================================
     * Incremental polling of the archive
     * ================================================================
     */
    /**
     * Remove the given Project, affiliated SBs and status entities
     * from the relevant queues.
     * 
     * @param project
     */
    private void disposeOf(Project project) {
    	// Shortcut the various status queues
		final ProjectStatusQueue psQ   = statusQs.getProjectStatusQueue();
		final OUSStatusQueue     oussQ = statusQs.getOUSStatusQueue();
		final SBStatusQueue      sbsQ  = statusQs.getSBStatusQueue();

		// Get rid of the Project and the ProjectStatus. Hold on to the
		// ProjectStatus for now so we can find the OUSStatuses.
		projectQueue.remove(project.getId());
		final String psId = project.getProjectStatusId();
		final ProjectStatusI ps = psQ.get(psId);
		psQ.remove(psId);
		
		// Get rid of the OUSStatuses. Start at the topmost and iterate
		// down until there are no more left.
		final List<String> oussIds = new ArrayList<String>();
		oussIds.add(ps.getObsProgramStatusRef().getEntityId());
		
		while (!oussIds.isEmpty()) {
			final String oussId = oussIds.remove(0);
			final OUSStatusI ouss = oussQ.get(oussId);
			final OUSStatusChoice choice = ouss.getOUSStatusChoice();
			for (final OUSStatusRefT childRef : choice.getOUSStatusRef()) {
				oussIds.add(childRef.getEntityId());
			}
			
			oussQ.remove(oussId);
		}
		
		// Get rid of the SBs and the SBStatuses.
		final SB[] schedBlocks = project.getAllSBs();
		for (final SB sb : schedBlocks) {
			sbQueue.remove(sb.getId());
			sbsQ.remove(sb.getSbStatusId());
		}
    }

    /**
     * Load the given Project, affiliated SBs and status entities
     * into the relevant queues.
     * 
     * @param projectId - the entity id of the project to load
     * @param ps        - the ProjectStatus of the project (if we have
     *                    it, if not, then <code>null</code>)
     * @param sbCache   - a cache of SchedBlocks we've got so far
     * @param now       - the timestamp to use as the creation time
     */
    private void loadProject(String                  projectId,
    						 Map<String, SchedBlock> sbCache,
    						 DateTime                now) {
    	
    	final ObsProject obsProject;
    	
    	// Get the ObsProject from the archive
    	try {
			obsProject = archive.getObsProject(projectId);
		} catch (SchedulingException e) {
			logger.warning(String.format(
					"Could not get ObsProject %s from the archive - %s, skipping project",
					projectId,
					e.getMessage()));
			return;
		}
		
		// Get the affiliated OUSStatuses and SBStatuses, filtering out
		// the SBStatuses which are in an inactive state.
		final String psId = obsProject.getProjectStatusRef().getEntityId();
		final StatusEntityQueueBundle bundle
								=  archive.getActiveStatusesFor(psId);
		
		if (bundle.getProjectStatusQueue().size() > 0) {
			// Don't bother if we didn't get the ProjectStatus - which
			// might be due to an already logged fault or because the
			// project is not active.
			statusQs.updateIncrWith(bundle);
		
			// Now get the SBs
			final SchedBlock[] sb = archive.getSelectedSBsFromObsProject(
												obsProject,
												bundle.getSBStatusQueue(),
												sbCache);

			mapAndAddProject(projectId,
					obsProject,
					sb,
					bundle.getProjectStatusQueue().get(psId),
					now);
		}
    }

	/**
	 * Refresh our view of the given project.
	 * 
	 * @param now
	 * @param sbCache
	 * @param projectId
	 */
	private void refreshProject(final String                  projectId,
								final Map<String, SchedBlock> sbCache,
								final DateTime                now) {
		final Project project = projectQueue.get(projectId);
		if (project != null) {
			disposeOf(project);
		}
		
		// Load the new project bumf - does nothing if the project is
		// not in a suitable state.
		loadProject(projectId, sbCache, now);
	}
    
    /**
     * Work out what relevant changes have happened in the XML Store
     * and the State Archive and then change the Scheduler's pool of
     * Projects and SBs to match.
     * 
     * "Relevant changes" is a strangely complicated thing. As a first
     * stab we'll update any project which:
     * <ul>
     * <li>has a SchedBlock whose XML has changed;
     * <li>has changed XML;
     * <li>has an SBStatus which has changed state;
     * <li>has an OUSStatus which has changed state;
     * <li>has changed its status.
     * </ul>
     * To "update" a project we
     * <ol>
     * <li>unload any current information we are holding on that
     * project;
     * <li>load it from the archives <code>if</code> it is active.
     * </ol>
     * 
     * We also cache SchedBlocks we've just got in order to avoid
     * fetching them multiple times, which would be a real performance
     * hit. Although it might look like we could apply the same sort of
     * caching to SBStatuses and OUSStatuses, the payback for so doing
     * would be very slight as the SBStatuses and OUSStatuses for each
     * ObsProjects are obtained with one CORBA call regardless of how
     * many of them the project has. It is the CORBA calls which are
     * the performance hit.
     * 
     * @param now - timestamp to use for any created objects.
     * @throws SchedulingException
     */
    synchronized public void incrementalPollArchive(DateTime now) {
    	logger.info("Starting incremental poll of the archive");
		
		// Initialisation
		//	In order to prevent duplicate retrieval of SchedBlocks,
    	//	SBStatuses and OUSStatuses we cache ones we have just got.
		final Map<String, SchedBlock> sbCache
								= new HashMap<String, SchedBlock>();
		
		//	The store for the ids of the changed projects.
		final Set<String> changedProjects = new HashSet<String>();
		
		clearBuffers();
		
		// Work out which projects have changed and record their ids.
		changedProjects.addAll(
				archive.getProjectIdsForChangedSchedBlocks(sbCache));
		changedProjects.addAll(
				archive.getProjectIdsForChangedObsProjects());
		final Map<String, StateChangeData> sbsChanges
					= archive.getStatusChanges(StateEntityType.SBK);
		final Map<String, StateChangeData> oussChanges
					= archive.getStatusChanges(StateEntityType.OUT);
		final Map<String, StateChangeData> psChanges
					= archive.getStatusChanges(StateEntityType.PRJ);

		for (String psId : psChanges.keySet()) {
			String projectId = psChanges.get(psId).domainEntityId;
			changedProjects.add(projectId);
		}
		for (String oussId : oussChanges.keySet()) {
			String projectId = oussChanges.get(oussId).domainEntityId;
			changedProjects.add(projectId);
		}
		for (String sbsId : sbsChanges.keySet()) {
			String sbId = sbsChanges.get(sbsId).domainEntityId;
			SchedBlock sb = archive.getSchedBlock(sbId, sbCache);
			String projectId = sb.getObsProjectRef().getEntityId();
			changedProjects.add(projectId);
		}

		for (final String projectId : changedProjects) {
			try {
				logger.info(String.format(
						"Initialising ObsProject %s",
						projectId));
				refreshProject(projectId, sbCache, now);
				logger.info(String.format(
						"Initialised ObsProject %s succesfully",
						projectId));
			} catch (Exception e) {
				logger.warning(String.format(
						"Could not initialise ObsProject %s - %s, skipping",
						projectId,
						e.getMessage()));
			}
		}
		
    }
    /* End Incremental polling of the archive
     * ============================================================= */
    
    private boolean donePollArchive = false;
    
    synchronized public void pollArchive(String prjuid) throws SchedulingException {
        // Use the State Archive to convert all the Phase2Submitted
    	// ObsProjects and SchedBlocks to Ready
		archive.convertProjects(StatusTStateType.PHASE2SUBMITTED,
		                        StatusTStateType.READY);

		logger.fine(String.format(
        		"polling archive for runnable projects, prjuid = %s",
        		prjuid==null? "null": prjuid));
        final DateTime now = clock.getDateTime();
        
        if (prjuid != null) {
         	logger.info(String.format(
         			"Polling the archive for project %s", prjuid));
        	refreshProject(prjuid, null, now);
        } else if (!donePollArchive) {
    		initialPollArchive(now);
    		donePollArchive = true;
    	} else if (prjuid == null) {
    		incrementalPollArchive(now);
    	}
        archive.setLastQueryTime(now);
        logger.info("The Scheduling Subsystem is currently managing "
                + projectQueue.size() + " projects, "
                + sbQueue.size() + " SBs, "
                + statusQs.getProjectStatusQueue().size() + " project statuses and "
                + statusQs.getSBStatusQueue().size() + " SB statuses");
//        logNumbers(String.format("at end of pollArchive(%s)", prjuid));
//        logDetails(String.format("at end of pollArchive(%s)", prjuid));
    }


    @SuppressWarnings("unused") // Sometimes we don't log, and that's OK
    private String formatArray(String[] prjuids) {
    	final StringBuilder b = new StringBuilder();
    	String sep = "";
    	b.append("[");
    	for (final String s : prjuids) {
    		b.append(sep);
    		b.append(s);
    		sep = ", ";
    	}
    	b.append("]");
    	return b.toString();
    }

   @SuppressWarnings("unused") // Sometimes we don't log, and that's OK
   private void logNumbers(String when) {
       logger.fine(String.format(
               "ObsProject    queue size %s = %d",
               when,
               projectQueue.size()));
       logger.fine(String.format(
               "SchedBlock    queue size %s = %d",
               when,
               sbQueue.size()));
       logger.fine(String.format(
               "ProjectStatus queue size %s = %d",
               when,
               statusQs.getProjectStatusQueue().size()));
       logger.fine(String.format(
               "OUSStatus     queue size %s = %d",
               when,
               statusQs.getOUSStatusQueue().size()));
       logger.fine(String.format(
               "SBStatus      queue size %s = %d",
               when,
               statusQs.getSBStatusQueue().size()));
   }
   
   @SuppressWarnings("unused") // Sometimes we don't log, and that's OK
   private void logDetails(String when) {
	   if (projectQueue.size() <= 30) {
		   logProjectsAndStatuses(projectQueue, statusQs.getProjectStatusQueue());
		   logOUSsAndStatuses(projectQueue, statusQs.getOUSStatusQueue());
		   logSBsAndStatuses(sbQueue, statusQs.getSBStatusQueue());
	   } else {
	       logger.fine("Too many projects to log all the gory details");
	   }
       logger.fine(String.format(
    		   "StatusEntityQueueBundle @ %h",
    		   statusQs.hashCode()));
       logger.fine(String.format(
    		   "ProjectStatusQueue @ %h",
    		   statusQs.getProjectStatusQueue().hashCode()));
       logger.fine(String.format(
    		   "OUSStatusQueue @ %h",
    		   statusQs.getOUSStatusQueue().hashCode()));
       logger.fine(String.format(
    		   "SBStatusQueue @ %h",
    		   statusQs.getSBStatusQueue().hashCode()));
   }

   private void logProjectsAndStatuses(ProjectQueue       domainQueue,
           ProjectStatusQueue statusQueue) {
       final StringBuilder b = new StringBuilder();
       final Formatter     f = new Formatter(b);

       try {
           f.format("%nProjects and Statuses in queues%n");

           final SortedSet<String> haveLogged = new TreeSet<String>();

           f.format("Domain to Status%n");
           for (final Project p : domainQueue.getAll()) {
               final String domainId = p.getObsProjectId();
               final String statusId = p.getProjectStatusId();
               f.format("\tProject ID = %s, Status ID = %s", domainId, statusId);
               final ProjectStatusI status = statusQueue.get(statusId);
               if (status != null) {
                   f.format(", Status = %s", status.getStatus().getState().toString());
                   final EntityRefT ref = status.getObsProjectRef();
                   if (ref != null) {
                       final String id = ref.getEntityId();
                       if (id.equals(domainId)) {
                           f.format(", status and domain ids match");
                       } else {
                           f.format(", loopback and domain id MISMATCH %s vs %s",
                                   id, domainId);
                       }
                   } else {
                       f.format(", status has missing domain reference");
                   }
               } else {
                   f.format(", status object NOT IN QUEUE!");
               }
               f.format("%n");
               if (statusId != null) {
                   haveLogged.add(statusId);
               }
           }

           f.format("Status to Domain (skipping statuses already logged)%n");
           for (final ProjectStatusI ps : statusQueue.getAll()) {
               final String statusId = ps.getUID();
               if (!haveLogged.contains(statusId)) {
                   f.format("\tStatus ID = %s", statusId);
                   f.format(", Status = %s", ps.getStatus().getState().toString());
                   final EntityRefT ref = ps.getObsProjectRef();

                   if (ref != null) {
                       f.format(", ObsProject ID = %s", ref.getEntityId());
                   } else {
                       f.format(", status has missing domain reference");
                   }
                   f.format("%n");
               }
           }
       } catch (Exception e) {
           e.printStackTrace();
       } finally {
           logger.info(b.toString());
       }
   }

   private void logOUSsAndStatuses(ProjectQueue   domainQueue,
           OUSStatusQueue statusQueue) {
       final StringBuilder b = new StringBuilder();
       final Formatter     f = new Formatter(b);
       try {
           f.format("%nOUSs and Statuses in queues%n");

           final SortedSet<String> haveLogged = new TreeSet<String>();

           f.format("Domain to Status%n");
           for (final Project proj : domainQueue.getAll()) {
               final String projectId = proj.getObsProjectId();
               for (final Program p : proj.getAllPrograms()) {
                   final String domainId = p.getProgramId();
                   final String statusId = p.getOUSStatusId();
                   f.format("\tOUS ID = %s in %s, Status ID = %s", domainId, projectId, statusId);
                   final OUSStatusI status = statusQueue.get(statusId);
                   if (status != null) {
                       f.format(", Status = %s", status.getStatus().getState().toString());
                       final EntityRefT ref = status.getObsUnitSetRef();
                       if (ref != null) {
                           final String id = ref.getEntityId();
                           final String part = ref.getPartId();
                           if (id.equals(projectId) && part.equals(domainId)) {
                               f.format(", status and domain ids match");
                           } else {
                               f.format(", loopback and domain id MISMATCH %s in %s vs %s in %s",
                                       part, id, domainId, projectId);
                           }
                       } else {
                           f.format(", status has missing domain reference");
                       }
                   } else {
                       f.format(", status object NOT IN QUEUE!");
                   }
                   f.format("%n");
                   if (statusId != null) {
                       haveLogged.add(statusId);
                   }
               }
           }

           f.format("Status to Domain (skipping statuses already logged)%n");
           for (final OUSStatusI ps : statusQueue.getAll()) {
               final String statusId = ps.getUID();
               if (!haveLogged.contains(statusId)) {
                   f.format("\tStatus ID = %s", statusId);
                   f.format(", Status = %s", ps.getStatus().getState().toString());
                   final EntityRefT ref = ps.getObsUnitSetRef();

                   if (ref != null) {
                       f.format(", OUS ID = %s in %s", ref.getPartId(), ref.getEntityId());
                   } else {
                       f.format(", status has missing domain reference");
                   }
                   f.format("%n");
               }
           }
       } catch (Exception e) {
           e.printStackTrace();
       } finally {
           logger.info(b.toString());
       }
   }


   private void logSBsAndStatuses(SBQueue domainQueue, SBStatusQueue statusQueue) {
       final StringBuilder b = new StringBuilder();
       final Formatter     f = new Formatter(b);

       try {
           f.format("%nSBs and Statuses in queues%n");

           final SortedSet<String> haveLogged = new TreeSet<String>();

           f.format("Domain to Status%n");
           for (final SB p : domainQueue.getAll()) {
               final String domainId = p.getSchedBlockId();
               final String statusId = p.getSbStatusId();
               f.format("\tSchedBlock ID = %s, Status ID = %s", domainId, statusId);
               final SBStatusI status = statusQueue.get(statusId);
               if (status != null) {
                   f.format(", Status = %s", status.getStatus().getState().toString());
                   final EntityRefT ref = status.getSchedBlockRef();
                   if (ref != null) {
                       final String id = ref.getEntityId();
                       if (id.equals(domainId)) {
                           f.format(", status and domain ids match");
                       } else {
                           f.format(", loopback and domain id MISMATCH %s vs %s",
                                   id, domainId);
                       }
                   } else {
                       f.format(", status has missing domain reference");
                   }
               } else {
                   f.format(", status object NOT IN QUEUE!");
               }
               f.format("%n");
               if (statusId != null) {
                   haveLogged.add(statusId);
               }
           }

           f.format("Status to Domain (skipping statuses already logged)%n");
           for (final SBStatusI ps : statusQueue.getAll()) {
               final String statusId = ps.getUID();
               if (!haveLogged.contains(statusId)) {
                   f.format("\tStatus ID = %s", statusId);
                   f.format(", Status = %s", ps.getStatus().getState().toString());
                  final EntityRefT ref = ps.getSchedBlockRef();

                   if (ref != null) {
                       f.format(", SchedBlock ID = %s", ref.getEntityId());
                   } else {
                       f.format(", status has missing domain reference");
                   }
                   f.format("%n");
               }
           }
       } catch (Exception e) {
           e.printStackTrace();
       } finally{
           logger.info(b.toString());
       }
   }
   
   @SuppressWarnings("unused") // Sometimes we don't log, and that's OK
   private void logProjectStatuses(String             when,
								   ProjectStatusQueue result,
								   ProjectStatusQueue added,
								   ProjectStatusQueue removed) {
	   final Set<String> allIds = new TreeSet<String>();
	   allIds.addAll(result.getAllIds());
	   allIds.addAll(added.getAllIds());
	   allIds.addAll(removed.getAllIds());

	   final StringBuilder b = new StringBuilder();
	   final Formatter     f = new Formatter(b);

	   f.format("ProjectStatuses %s%n%n", when);
	   f.format("%32s%32s%6s%6s%6s%n", "Status UID", "Project UID", "In Q", "Add", "Delete");
	   for (final String uid : allIds) {
		   f.format("%32s", uid);
		   ProjectStatusI ps = null;
		   String inQ = "no";
		   String add = "no";
		   String del = "no";

		   if (result.get(uid) != null) {
			   ps = result.get(uid);
			   inQ = "Yes";
		   }
		   if (added.get(uid) != null) {
			   ps = added.get(uid);
			   add = "Yes";
		   }
		   if (removed.get(uid) != null) {
			   ps = removed.get(uid);
			   del = "Yes";
		   }

		   if (ps != null) {
			   f.format("%32s%6s%6s%6s%n", ps.getDomainEntityId(), inQ, add, del);
		   } else {
			   f.format(" - cannot find UID in any queue (seriously unexpected)%n");
		   }
	   }
	   logger.fine(b.toString());
   }
   
   @SuppressWarnings("unused") // Sometimes we don't log, and that's OK
   private void logStatuses(String                  when,
							StatusEntityQueueBundle bundle) {

	   final StringBuilder b = new StringBuilder();
	   final Formatter     f = new Formatter(b);

	   f.format("%s%n%n", when);
	   f.format("Project Statuses%n%n%32s %32s %s%n",
			   "Status UID",
			   "Domain UID",
			   "State");
	   for (final ProjectStatusI s : bundle.getProjectStatusQueue().getAll()) {
		   f.format("%32s %32s %s%n",
				   s.getUID(),
				   s.getDomainEntityId(),
				   s.getStatus().getState().toString());
	   }
	   f.format("OUS Statuses%n%n%32s %32s %32s %s%n",
			   "Status UID",
			   "Domain UID",
			   "Project Status ID",
			   "State");
	   for (final OUSStatusI s : bundle.getOUSStatusQueue().getAll()) {
		   f.format("%32s %32s %32s %s%n",
				   s.getUID(),
				   s.getDomainEntityId(),
				   s.getProjectStatusRef().getEntityId(),
				   s.getStatus().getState().toString());
	   }
	   f.format("SB Statuses%n%n%32s %32s %32s %32s %s%n",
			   "Status UID",
			   "Domain UID",
			   "Project Status ID",
			   "OUS Status ID",
			   "State");
	   for (final SBStatusI s : bundle.getSBStatusQueue().getAll()) {
		   f.format("%32s %32s %32s %32s %s%n",
				   s.getUID(),
				   s.getDomainEntityId(),
				   s.getProjectStatusRef().getEntityId(),
				   s.getContainingObsUnitSetRef().getEntityId(),
				   s.getStatus().getState().toString());
	   }
	   logger.fine(b.toString());
   }
   
   @SuppressWarnings("unused") // Sometimes we don't log, and that's OK
   private void logSBQueue(String  when,
						   SBQueue queue) {

	   final StringBuilder b = new StringBuilder();
	   final Formatter     f = new Formatter(b);

	   f.format("%s%n%n", when);
	   f.format("SchedBlocks%n%n%32s %32s %32s %s%n",
			   "Status UID",
			   "Domain UID",
			   "Project UID",
			   "State");
	   for (final SB s :queue.getAll()) {
		   f.format("%32s %32s %32s %s%n",
				   s.getId(),
				   s.getSbStatusId(),
				   s.getProject().getId(),
				   s.getStatus().getState().toString());
	   }
	   logger.fine(b.toString());
   }
   
   @SuppressWarnings("unused") // Sometimes we don't log, and that's OK
   private void logProjectQueue(String       when,
						        ProjectQueue queue) {

	   final StringBuilder b = new StringBuilder();
	   final Formatter     f = new Formatter(b);

	   f.format("%s%n%n", when);
	   f.format("Projects%n%n%32s %32s %s%n",
			   "Status UID",
			   "Domain UID",
			   "State");
	   for (final Project p :queue.getAll()) {
		   f.format("%32s %32s %s%n",
				   p.getId(),
				   p.getProjectStatusId(),
				   p.getStatus().getState().toString());
	   }
	   logger.fine(b.toString());
   }

   
   @SuppressWarnings("unused") // Sometimes we don't log, and that's OK
   private void logProject(Project project) {
	   final StringBuilder b = new StringBuilder();
	   final Formatter     f = new Formatter(b);
	   
	   f.format("Project %s%n", project.getId());
	   f.format("\tState:\t%s%n", project.getStatus().getState());
	   f.format("\tName:\t%s%n", project.getProjectName());
	   f.format("\tPI:\t%s%n", project.getPI());
	   f.format("\ttimeOfCreation:\t%s%n", project.getTimeOfCreation());
	   f.format("\ttimeOfUpdate:\t%s%n", project.getTimeOfUpdate());
	   for (final SB sb : project.getAllSBs()) {
		   f.format("\tSB %s%n", sb.getId());
		   f.format("\t\tState:\t%s%n", sb.getStatus().getState());
		   f.format("\t\tName:\t%s%n", sb.getSBName());
		   f.format("\t\ttimeOfCreation:\t%s%n", project.getTimeOfCreation());
		   f.format("\t\ttimeOfUpdate:\t%s%n", project.getTimeOfUpdate());
		   f.format("\tend SB %s%n", project.getId());
	   }
	   f.format("end Project %s%n", project.getId());
	   logger.fine(b.toString());
	}
    
    public Project getProject(String uid, SBStatusQueue sbStatusQueue) throws SchedulingException {
        Project prj = null;
        try {    
            ObsProject project = archive.getObsProject(uid);
            obsProjectBuffer.put(project.getObsProjectEntity().getEntityId(), project);
            String projectStatusId;
            try {
                projectStatusId = project.getProjectStatusRef().getEntityId();
                try {
                    ProjectStatusI ps = archive.getProjectStatusForObsProject(project);
                    SchedBlock[] sbs = archive.getSBsFromObsProject(project, sbStatusQueue);
                    // Cache the SBs to avoid having to perform another retrieval
                    // from the ARCHIVE.
                    schedBlockBuffer.put(project.getObsProjectEntity().getEntityId(), sbs);
                    if ( sbs != null ) {    
                        prj = projectUtil.map(project, sbs, ps, 
                            new DateTime(System.currentTimeMillis()));
                    } else {
                        logger.warning(String.format(
                                "No SchedBlocks for project %s",
                                projectStatusId));
                        AcsJObsProjectRejectedEx ex = new AcsJObsProjectRejectedEx();
                        ex.setProperty("UID", project.getObsProjectEntity().getEntityId());
                        ex.setProperty("Reason", "No SchedBlocks for project");
                        ex.log(logger);
                        
                    }
                } catch (SchedulingException e) {
                    logger.warning(e.getLocalizedMessage());
                }
            } catch (NullPointerException e) {
                logger.warning(String.format(
                        "Project from archive has no EntityId, project name is %s, PI is %s",
                        project.getProjectName(), project.getPI()));
                projectStatusId = null;
            }
        } catch (SchedulingException e1) {
            logger.severe("Scheduling encounter errors when get obsproject from archive");
        } catch (Exception e) {
            sendAlarm("Scheduling","SchedArchiveConnAlarm",1,ACSFaultState.ACTIVE);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e1) {
                e1.printStackTrace(System.out);
            }
            logger.severe("Scheduling encounter errors when get all projects from archive!!");
        }
        return prj;        
    }
    
    public Project[] getAllProject(ProjectStatusQueue prjStatusQueue, SBStatusQueue sbStatusQueue)
        throws SchedulingException {
        
        Project[] projects = null;
        
        try {    
            Vector<Project> tmp_projects = new Vector<Project>();
            ObsProject[] obsProjects = archive.getAllObsProjects(prjStatusQueue);
            
            for (final ObsProject project : obsProjects) {
                obsProjectBuffer.put(project.getObsProjectEntity().getEntityId(), project);
                
                String projectStatusId = project.getProjectStatusRef().getEntityId();
                String projectId = project.getObsProjectEntity().getEntityId();
                try {
                    try {
                        Profiler prof1 = new Profiler(logger); // profiler for this whole block
                        Profiler prof2 = new Profiler(logger); // profiler for parts of the block
                        
                        prof1.start("get all SchedBlocks for ObsProject " + projectId);
                        
                        prof2.start("archive.getProjectStatusForObsProject");
                        ProjectStatusI ps = null;
//                        logger.info("getting ProjectStatus for id " + project.getProjectStatusRef().getEntityId());
                        if (statusQs != null) {
                            ps = statusQs.getProjectStatusQueue().get(project.getProjectStatusRef().getEntityId());
                        }
                        if (ps == null) {
                            logger.fine(String.format("state %s, belonging to project %s is null",
                                    project.getProjectStatusRef().getEntityId(), project.getObsProjectEntity().getEntityId()));
                            ps = archive.getProjectStatusForObsProject(project);
                        }
                        prof2.end();

                        prof2.start("getSBsFromObsProject");
                        SchedBlock[] sbs = archive.getSBsFromObsProject(project, sbStatusQueue);
                        prof2.end();
                        
                        // Cache the SBs to avoid having to perform another retrieval
                        // from the ARCHIVE.
                        schedBlockBuffer.put(project.getObsProjectEntity().getEntityId(), sbs);
                        
                        //add here to check if the sbs get without problem
                        prof2.start("projectUtil.map");
                        if (sbs!=null) {    
                            Project p = projectUtil.map(project, sbs, ps, 
                                new DateTime(System.currentTimeMillis()));
                            if (p!=null){
                                tmp_projects.add(p);
                            }
                        } else {
                            logger.warning(String.format(
                                    "No SchedBlocks for project %s",
                                    projectStatusId));
                            AcsJObsProjectRejectedEx ex = new AcsJObsProjectRejectedEx();
                            ex.setProperty("UID", projectStatusId);
                            ex.setProperty("Reason", "No SchedBlocks for project");
                            ex.log(logger);

                        }
                        prof2.end();
                        
                        prof1.end();
                    } catch (SchedulingException e) {
                        logger.warning(e.getLocalizedMessage());
                    }
                } catch (NullPointerException e) {
                    logger.warning(String.format(
                            "Project from archive has no EntityId, project name is %s, PI is %s",
                            project.getProjectName(), project.getPI()));
                    projectStatusId = null;
                }
            }
            
            projects = new Project[tmp_projects.size()];
            for(int i=0; i < tmp_projects.size();i++) {
                projects[i] = tmp_projects.elementAt(i);
            }
            logger.fine("SCHEDULING: Scheduling converted "+projects.length+
                    " Projects from ObsProject found archived.");
            //return projects;
        } catch (SchedulingException e1) {
            logger.severe(String.format(
            		"Scheduling encountered errors when get obsproject from archive - %s",
            		e1.getMessage()));
            e1.printStackTrace(System.out);
        } catch (Exception e) {
            sendAlarm("Scheduling","SchedArchiveConnAlarm",1,ACSFaultState.ACTIVE);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e1) {
                e1.printStackTrace(System.out);
            }
            logger.severe("Scheduling encounter errors when get all projects from archive!!");
            //throw new SchedulingException (e);
        }
        return projects;
    }
    
    public void sendAlarm(String ff, String fm, int fc, String fs) {
        try {
            ACSAlarmSystemInterface alarmSource = ACSAlarmSystemInterfaceFactory.createSource("ALMAArchive");
            ACSFaultState state = ACSAlarmSystemInterfaceFactory.createFaultState(ff, fm, fc);
            state.setDescriptor(fs);
            state.setUserTimestamp(new Timestamp(clock.getDateTime().getMillisec()));
            Properties prop = new Properties();
            prop.setProperty(ACSFaultState.ASI_PREFIX_PROPERTY, "prefix");
            prop.setProperty(ACSFaultState.ASI_SUFFIX_PROPERTY, "suffix");
            prop.setProperty("ALMAMasterScheduling_PROPERTY", "ConnArchiveException");
            state.setUserProperties(prop);
            alarmSource.push(state);
        } catch(Exception e) {
            e.printStackTrace();
        }
        
    }
    
    public ProjectLite[] getProjectLites() {
        String[] ids = projectQueue.getAllIds();
        ProjectLite[] projectliteArray = new ProjectLite[ids.length];
        for(int i = 0; i < ids.length; i++) {
            try {
                logger.info("creating ProjectLite for " + ids[i]);
                projectliteArray[i] = liteFactory.createProjectLite(ids[i]);
            } catch (Exception ex) {
                projectliteArray[i] = null;
            }
        }
        return projectliteArray;
    }

    public ProjectLite[] getProjectLites(String[] ids) {
        ProjectLite[] projectliteArray = new ProjectLite[ids.length];
        for(int i = 0; i < ids.length; i++) {
            try {
                logger.info("creating ProjectLite for " + ids[i]);
                projectliteArray[i] = liteFactory.createProjectLite(ids[i]);
            } catch (Exception ex) {
                projectliteArray[i] = null;
            }
        }
        return projectliteArray;        
    }
    
    public SBLite[] getExistingSBLites() {
        String[] ids = sbQueue.getAllIds();
        logger.finer("ids.length = " + ids.length);
        SBLite[] sblites = new SBLite[ids.length];
        for(int i=0; i < ids.length; i++) {
            try {
                logger.info("creating SBLite for " + ids[i]);
                sblites[i] = liteFactory.createSBLite(ids[i]);
            } catch (Exception ex) {
                sblites[i] = null;
            }
        }
        return sblites;
    }

    public SBLite[] getExistingSBLites(String[] ids) {
        logger.finer("ids.length = " + ids.length);
        SBLite[] sblites = new SBLite[ids.length];
        for(int i=0; i < ids.length; i++) {
            try {
                logger.info("creating SBLite for " + ids[i]);
                sblites[i] = liteFactory.createSBLite(ids[i]);
            } catch (Exception ex) {
                sblites[i] = null;
            }
        }
        return sblites;
    }
    
    class LiteFactory {
    
        protected LiteFactory() {}
        
        protected ProjectLite createProjectLite(String id) {
            logger.info("entering createProjectLite"); // change level later
            Project p = projectQueue.get(id);;
            ProjectLite projectlite= new ProjectLite();
            projectlite.uid = p.getId();
            projectlite.projectName = p.getProjectName();
            projectlite.piName = p.getPI();
            projectlite.version = p.getProjectVersion();
            projectlite.creationTime = p.getTimeOfCreation().toString();
            projectlite.totalSBs = String.valueOf(p.getTotalSBs());
            projectlite.completeSBs = String.valueOf(p.getNumberSBsCompleted());
            projectlite.failedSBs = String.valueOf(p.getNumberSBsFailed());
            
            logger.fine("calling project getAllSBs");
            SB[] sbs = p.getAllSBs();
            String[] sbids= new String[sbs.length];
            for(int j=0; j < sbs.length;j++){
                sbids[j] = sbs[j].getId();
            }
            projectlite.allSBIds = sbids;
            ProjectStatusI ps = getPSForProject(p);
            projectlite.isComplete = isProjectComplete(ps);
    
            logger.fine("setting Project Status completeSBs");
            int sbcompl =
                statusQs.getOUSStatusQueue().get(ps.getObsProgramStatusRef()).getNumberSBsCompleted();
            projectlite.completeSBs = String.valueOf(sbcompl);
            
            logger.fine("setting Project Status failedSBs");
            int numsbfail = statusQs.getOUSStatusQueue().get(ps.getObsProgramStatusRef()).getNumberSBsFailed();
            projectlite.failedSBs = String.valueOf(numsbfail);
    
            logger.fine("serializing Project Status XML");
            if (ps != null ){
                StringWriter writer = new StringWriter();
                try{
                    ps.marshal(writer);
                    // logger.info("PS XML: " + writer.toString());
                    projectlite.statusXML = writer.toString();
                }catch(MarshalException ex){
                    ex.printStackTrace();
                }
                catch(ValidationException ex){
                    ex.printStackTrace();
                }
            } else {
                projectlite.statusXML = "";
            }
            
            projectlite.status = ps.getStatus().getState().toString();
            logger.fine("Project Status: " + projectlite.status);
            
            return projectlite;
        }

        protected SBLite createSBLite(String id) {
            logger.fine("entering createSBLite");
            String pid,sname,pname,pi,pri;
            double ra,dec;

            logger.info("createSBLite: sbQueue.get");
            SB sb = sbQueue.get(id);
            if (sb == null) {
                return null;
            }
            logger.info("createSBLite: sbQueue get complete");
            
            SBLite sblite = new SBLite();
            if(id == null || id =="") {
                id = "WARNING: Problem with SB id";
            }
            sblite.schedBlockRef =id;
            pid = sb.getProject().getId();
            if(pid ==null||pid=="") {
                pid = "WARNING: problem with project id";   
            }
            sblite.projectRef = pid;
            sblite.obsUnitsetRef = "";

            sname =sb.getSBName();
            if(sname == null || sname ==""){
                sname = "WARNING: problem with SB name";
            }
            sblite.sbName =sname;
            pname = sb.getProject().getProjectName();
            if(pname == null ||pname =="") {
                pname = "WARNING: problem with project name";
            }
            sblite.projectName = pname;
            pi = sb.getProject().getPI();
            if(pi == null || pi == ""){
                pi = "WARNING: problem with pi";
            }
            sblite.PI = pi;
            pri = sb.getProject().getScientificPriority().getPriority();
            if(pri == null || pri =="") {
                pri = "WARNING: problem with scientific priority";
            }
            sblite.priority = pri;
            try {
                ra = sb.getTarget().getCenter().getRa();
            } catch(NullPointerException npe) {
                logger.warning("SCHEDULING: RA object == null in SB, setting to 0.0");
                ra = 0.0;
            }
            sblite.ra = ra;
            try {
                dec = sb.getTarget().getCenter().getDec();
            } catch(NullPointerException npe) {
                logger.warning("SCHEDULING: DEC object == null in SB, setting to 0.0");
                dec = 0.0;
            }
            if(sb.getIndefiniteRepeat()){
                sblite.maxExec = "indefinite";
            } else {
                sblite.maxExec = String.valueOf(sb.getMaximumNumberOfExecutions());
            }
            sblite.dec = dec;
            sblite.freq = 0;
            sblite.maxTime = 0;
            sblite.score = 0;
            sblite.success = 0; 
            sblite.rank = 0 ;
            //have to get PS to get this info
            //System.out.println("SBid "+id);
            logger.info("createSBLite: getStatusFromSBId");
            SBStatusI sbs = statusQs.getSBStatusQueue().getStatusFromSBId(id);
            if (sbs == null) {
                // For some reason the sbs is no longer in the queue, so we
                // pretend that it's complete.
                sblite.isComplete = true;
            } else {
                // and SB is deemed complete if it's SUSPENDED as that's
                // the state we put an SB into after an execution (unless
                // we're operating in Full Auto mode in which case it might
                // be READY, but deeming that complete seems a bad idea).
                // We also deem FULLYOBSERVED as complete just in case the
                // SB has been moved to that state after (a quick) QA0.
                final StatusTStateType sbState = sbs.getStatus().getState();
                sblite.isComplete = sbState.equals(StatusTStateType.SUSPENDED) ||
                                    sbState.equals(StatusTStateType.FULLYOBSERVED);
            }
            logger.info("createSBLite: getStatusFromSBId completed");
            
            logger.info("createSBLite: serializing SBStatus");
            // We may pass the all the ExecStatus to the Scheduling Plugin
            // Right now just displaying the XML for the SB Status
            if (sbs != null) {          
                StringWriter writer = new StringWriter();
                try {
                    sbs.marshal(writer);
                    sblite.statusXML = writer.toString();
                } catch (MarshalException ex) {
                    ex.printStackTrace();
                } catch (ValidationException ex) {
                    ex.printStackTrace();
                }
            } else {
                sblite.statusXML = "";
            }
            logger.info("createSBLite: serialization complete");
            
            if (sbs != null)
                sblite.status = sbs.getStatus().getState().toString();
            else
                sblite.status = "Not in Scheduling Queue";
            logger.info("exiting createSBLite");
            return sblite;
        }
        
        private ProjectStatusI getPSForProject(Project p) {
            String ps_id = p.getProjectStatusId();
            ProjectStatusI ps = statusQs.getProjectStatusQueue().get(ps_id);
            
            if (ps == null) {
                logger.info("project status is null, getting it from the archive");
                try {
                    ps = archive.getProjectStatus(p);
                    statusQs.getProjectStatusQueue().add(ps);
                } catch (SchedulingException e) {
                    logger.warning(e.getLocalizedMessage());
                    ps = null;
                }
            }
            return ps;
        }
        
        private boolean isProjectComplete(ProjectStatusI ps){
            try {
                return ps.getStatus().getState().toString().equals("complete");
            } catch (NullPointerException e) {
                return false;
            }
        }
    }
}
