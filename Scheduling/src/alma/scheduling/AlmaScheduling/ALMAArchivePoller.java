package alma.scheduling.AlmaScheduling;

import java.io.StringWriter;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Vector;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;

import alma.SchedulingExceptions.wrappers.AcsJObsProjectRejectedEx;
import alma.SchedulingExceptions.wrappers.AcsJSchedBlockRejectedEx;
import alma.acs.container.ContainerServices;
import alma.acs.logging.AcsLogger;
import alma.alarmsystem.source.ACSAlarmSystemInterface;
import alma.alarmsystem.source.ACSAlarmSystemInterfaceFactory;
import alma.alarmsystem.source.ACSFaultState;
import alma.entities.commonentity.EntityRefT;
import alma.entity.xmlbinding.obsproject.ObsProject;
import alma.entity.xmlbinding.schedblock.SchedBlock;
import alma.entity.xmlbinding.valuetypes.types.StatusTStateType;
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

    final public static String[] OPRunnableStates = {
        StatusTStateType.READY.toString(),              
        StatusTStateType.PARTIALLYOBSERVED.toString()               
    };
    final public static String[] SBRunnableStates = {
        StatusTStateType.READY.toString(),              
        StatusTStateType.RUNNING.toString()             
    };
    
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
    
    synchronized public void pollArchive(String prjuid) throws SchedulingException {
        logger.fine(String.format(
        		"polling archive for runnable projects, prjuid = %s",
        		prjuid==null? "null": prjuid));

        schedBlockBuffer.clear();
        obsProjectBuffer.clear();
        
        String[] prjToUpd;
        if (prjuid == null) {
            prjToUpd = archive.getProjectsToUpdate();
            if ( prjToUpd != null ) { 
                logger.info("# of projects to update: " + prjToUpd.length);
                for ( String prj : prjToUpd )
                    logger.info("updating project: " + prj);
            }
        } else {
            prjToUpd = new String[] { prjuid };
            logger.info("updating just project " + prjuid);
        }
        // Asks the State Archive to convert all the Phase2Submitted projects
        // and sbs to Ready
        archive.convertProjects(StatusTStateType.PHASE2SUBMITTED,
                StatusTStateType.READY);
        // This is not working yet. The State Archive will throw a NotAuthorizedEx
        // exception.
        // archive.convertSchedBlocks(StatusTStateType.PHASE2SUBMITTED,
        //         StatusTStateType.READY);        
        if ( prjToUpd == null ) { // First update. Full load.
            // Asks the State Archive for the statuses and works out which ones
            // are runnable
            final StatusEntityQueueBundle newQs =
                archive.determineRunnablesByStatus(OPRunnableStates,
                        SBRunnableStates);
            statusQs.updateIncrWith(newQs);
            archive.setProjectUtilStatusQueue(statusQs);
        } else if ( prjToUpd.length > 0 ){ // Incremental load
            StatusEntityQueueBundle newQs =
                archive.determineRunnablesByStatusIncr(OPRunnableStates,
                        SBRunnableStates);
            statusQs.updateIncrWith(newQs);
            archive.setProjectUtilStatusQueue(statusQs);
        }
 
        OLD_PollArchive(prjToUpd);
        logger.info("The Scheduling Subsystem is currently managing "
                        + projectQueue.size() + " projects, "
                        + sbQueue.size() + " SBs, "
                        + statusQs.getProjectStatusQueue().size() + " project statuses and "
                        + statusQs.getSBStatusQueue().size() + " SB statuses");
//         logNumbers("at end of pollArchive");
//         logDetails("at end of pollArchive");
    }
    
	/**
     * polls the archive for new/updated projects
     * then updates the queues (project queue, sb queue & project status queue)
     */
   private void OLD_PollArchive(String[] prjuids) throws SchedulingException {
       Project[] projectList = new Project[0];
       Vector<SB> tmpSBs = new Vector<SB>();
       final ProjectStatusQueue psQ  = statusQs.getProjectStatusQueue();
       final SBStatusQueue      sbsQ = statusQs.getSBStatusQueue();

       try {
           if (prjuids == null) {
               projectList = getAllProject();
           } else {
               projectList = new Project[prjuids.length];
               int i = 0;
               for ( String prjuid : prjuids ) {
            	   // Refresh the status info from the state archive,
            	   // which ensures that we have the up to date version
            	   // which reflects the latest structure of the
            	   // changed project.
            	   statusQs.refreshProject(prjuid,
            			   CachedStatusFactory.getInstance());
                   Project prj = getProject(prjuid);
                   if ( prj != null ) {
                       projectList[i++] = prj;
                   }
               }
           }
           archive.setLastProjectQuery(clock.getDateTime());
           
           logger.finest("ProjectList size =  " + projectList.length);
           ArrayList<Project> projects = new ArrayList<Project>(
                   projectList.length);
           for (final Project project : projectList) {
               if (psQ.isExists(project.getProjectStatusId())) {
                   projects.add(project);
                   logger.fine(String.format(
                           "Including project %s (status is %s)",
                           project.getId(),
                           project.getStatus().getState()));
               } else {
                   logger.fine(String.format(
                           "Rejecting project %s (not in status queue, status = %s)",
                           project.getId(),
                           project.getStatus().getState()));
                   AcsJObsProjectRejectedEx ex = new AcsJObsProjectRejectedEx();
                   ex.setProperty("UID", project.getId());
                   ex.setProperty("Reason", "Not in status queue");
                   ex.log(logger);
               }
           }

           logger.finest("Projects size =  " + projects.size());
           for (final Project p : projects) {
               
               ObsProject obsProj = obsProjectBuffer.get(p.getId());
               SchedBlock[] schedblks = schedBlockBuffer.get(p.getId());
               ProjectStatusI prjStatus = psQ.get(obsProj.getProjectStatusRef());
               Project p2 = projectUtil.map(obsProj,
                                            schedblks,
                                            prjStatus,
                                            clock.getDateTime());
               SB[] sbs = p2.getAllSBs();
               
               // !!! This will query another ObsProject retrieval and
               // another retrieval for all its SBs. Replacing this call
               // for the call above.
               // SB[] sbs = archive.getSBsForProject(p.getId());
               
               for (final SB sb : sbs) {
                   if (sbsQ.isExists(sb.getSbStatusId())) {
                       // Only worry about SBs for which there is
                       // a status (which means the SB is in a
                       // runnable state.
                       tmpSBs.add(sb);
                   } else {
                       logger.fine(String.format(
                               "Rejecting SchedBlock %s (not in status queue, status = %s)",
                               sb.getId(),
                               sb.getStatus().getState()));
                       AcsJSchedBlockRejectedEx ex = new AcsJSchedBlockRejectedEx();
                       ex.setProperty("UID", sb.getId());
                       ex.setProperty("Reason", "Not in status queue");
                       ex.log(logger);
                   }
               }
           }

           logger.finest("projects = " + projects.size());
           logger.finest("tmp sbs " + tmpSBs.size());

           // For all the stuff gotten above from the archive, determine if
           // they are new (then add them), if they are updated (then
           // updated) or the same (then do nothing)
           for (final Project newProject : projects) {

               // does project exist in queue?
               if (projectQueue.isExists(newProject.getId())) {
                   final Project oldProject = projectQueue.get(newProject.getId());
                   // logger.finest("(old project)number of program in
                   // pollarchive:"+oldProject.getAllSBs().length);
                   // yes it is so check if project needs to be updated,
                   // check if
                   if (newProject.getTimeOfUpdate().compareTo(
                           oldProject.getTimeOfUpdate()) == 1) {
                       // needs updating
                       projectQueue.replace(newProject);
                   } else if (newProject.getTimeOfUpdate().compareTo(
                           oldProject.getTimeOfUpdate()) == 0) {
                       // DO NOTHING hasn't been updated
                   } else if (newProject.getTimeOfUpdate().compareTo(
                           oldProject.getTimeOfUpdate()) == -1) {
                       // TODO should throw an error coz the old project
                       // has been updated and the new one hasnt
                   } else {
                       // TODO Throw an error here
                   }

                   // TODO if the sbs need updating and if there are new
                   // ones to add
                   SB[] currSBs = getSBs(tmpSBs, newProject.getId());
                   SB newSB, oldSB;
                   for (int j = 0; j < currSBs.length; j++) {
                       newSB = currSBs[j];
                       if (sbQueue.isExists(newSB.getId())) {
                           logger.finest("Sb not new");
                           oldSB = sbQueue.get(newSB.getId());

                           // check if it needs to be updated, if yes then
                           // update
                           if (newSB.getTimeOfUpdate().compareTo(
                                   oldSB.getTimeOfUpdate()) == 1) {
                               logger.finest("Sb needs updating");
                               sbQueue.replace(newSB);
                               projectQueue.replace(newProject);
                           } else if (newSB.getTimeOfUpdate().compareTo(
                                   oldSB.getTimeOfUpdate()) == 0) {
                               // DO NOTHING, hasn't been updated
                           } else if (newSB.getTimeOfUpdate().compareTo(
                                   oldSB.getTimeOfUpdate()) == -1) {
                               // TODO should throw an error coz the old sb
                               // has been updated and the new one hasnt
                           } else {
                               // TODO Throw an error
                           }
                       } else {
                           // not in queue, so add it.
                           logger.finest("SB new, adding");
                           sbQueue.add(newSB);
                           projectQueue.replace(newProject);
                       }
                   }
               } else {
                   logger.finest("Project new, adding");
                   // no it isn't so add project to queue,
                   projectQueue.add(newProject);
                   // and sbs to sbqueue
                   SB[] schedBlocks = getSBs(tmpSBs, newProject.getId());
                   if (schedBlocks.length > 0) {
                       sbQueue.add(schedBlocks);
                       Program p = (schedBlocks[0]).getParent();
                       logger.finest("Program's session " + p.getId()
                               + "has " + p.getNumberSession()
                               + " session");
                   } else {
                       logger
                               .info("HSO hotfix 2008-06-07: new project "
                                       + newProject.getId()
                                       + " does not have any schedblocks. Not sure if this is OK.");
                   }
               }
           }

           // checkSBUpdates();
       } catch (Exception e) {
           e.printStackTrace();
           throw new SchedulingException(e);
       }
   }
   
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
   
   private void logDetails(String when) {
       logProjectsAndStatuses(projectQueue, statusQs.getProjectStatusQueue());
       logOUSsAndStatuses(projectQueue, statusQs.getOUSStatusQueue());
       logSBsAndStatuses(sbQueue, statusQs.getSBStatusQueue());
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

    private static SB[] getSBs(Vector<SB> v, String s) {
        Vector<SB> sbsV = new Vector<SB>();
        SB sb;
        for (int i = 0; i < v.size(); i++) {
            sb = (SB) v.elementAt(i);
            if (sb.getProject().getId().equals(s)) {
                sbsV.add(sb);
            }
        }
        SB[] sbs = new SB[sbsV.size()];
        for (int i = 0; i < sbsV.size(); i++) {
            sbs[i] = (SB) sbsV.elementAt(i);
        }
        return sbs;
    }
    
    public Project getProject(String uid) throws SchedulingException {
        Project prj = null;
        try {    
            ObsProject project = archive.getObsProject(uid);
            obsProjectBuffer.put(project.getObsProjectEntity().getEntityId(), project);
            String projectStatusId;
            try {
                projectStatusId = project.getProjectStatusRef().getEntityId();
                try {
                    ProjectStatusI ps = archive.getProjectStatusForObsProject(project);
                    SchedBlock[] sbs = archive.getSBsFromObsProject(project);
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
    
    public Project[] getAllProject() throws SchedulingException {
        Project[] projects = null;
        
        try {    
            Vector<Project> tmp_projects = new Vector<Project>();
            ObsProject[] obsProjects = archive.getAllObsProjects();
            
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
                        ProjectStatusI ps = archive.getProjectStatusForObsProject(project);
                        prof2.end();

                        //TODO should check project queue.. if project exists don't map a new one.
                        prof2.start("getSBsFromObsProject");
                        SchedBlock[] sbs = archive.getSBsFromObsProject(project);
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
            logger.severe("Scheduling encounter errors when get obsproject from archive");
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
            logger.info("entering createSBLite"); // changel level later
            String sid,pid,sname,pname,pi,pri;
            double ra,dec,freq,score,success,rank;
            long maxT;
            logger.info("createSBLite: sbQueue.get");
            SB sb = sbQueue.get(id);
            if (sb == null) {
                return null;
            }
            logger.info("createSBLite: sbQueue get complete");
            
            SBLite sblite = new SBLite();
            sid = sb.getId();
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
