package alma.scheduling.inttest;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Vector;
import java.util.logging.Logger;

import alma.SchedulingExceptions.wrappers.AcsJObsProjectRejectedEx;
import alma.SchedulingExceptions.wrappers.AcsJSchedBlockRejectedEx;
import alma.acs.component.client.ComponentClient;
import alma.acs.container.ContainerServices;
import alma.acs.entityutil.EntityException;
import alma.acs.logging.AcsLogger;
import alma.alarmsystem.source.ACSAlarmSystemInterface;
import alma.alarmsystem.source.ACSAlarmSystemInterfaceFactory;
import alma.alarmsystem.source.ACSFaultState;
import alma.entities.commonentity.EntityRefT;
import alma.entity.xmlbinding.obsproject.ObsProject;
import alma.entity.xmlbinding.projectstatus.ProjectStatus;
import alma.entity.xmlbinding.schedblock.SchedBlock;
import alma.entity.xmlbinding.valuetypes.types.StatusTStateType;
import alma.scheduling.AlmaScheduling.ALMAArchive;
import alma.scheduling.AlmaScheduling.ALMAClock;
import alma.scheduling.AlmaScheduling.OUSStatusQueue;
import alma.scheduling.AlmaScheduling.ProjectStatusQueue;
import alma.scheduling.AlmaScheduling.ProjectUtil;
import alma.scheduling.AlmaScheduling.SBStatusQueue;
import alma.scheduling.AlmaScheduling.StatusEntityQueueBundle;
import alma.scheduling.AlmaScheduling.statusIF.OUSStatusI;
import alma.scheduling.AlmaScheduling.statusIF.ProjectStatusI;
import alma.scheduling.AlmaScheduling.statusIF.SBStatusI;
import alma.scheduling.Define.DateTime;
import alma.scheduling.Define.Program;
import alma.scheduling.Define.Project;
import alma.scheduling.Define.ProjectQueue;
import alma.scheduling.Define.SB;
import alma.scheduling.Define.SBQueue;
import alma.scheduling.Define.SchedulingException;
import alma.xmlentity.XmlEntityStruct;
import alma.xmlstore.ArchiveInternalError;
import alma.xmlstore.Cursor;
import alma.xmlstore.CursorPackage.QueryResult;
import alma.xmlstore.OperationalPackage.StatusStruct;

public class ArchivePoller extends ComponentClient {

    final public static String[] OPRunnableStates = {
        StatusTStateType.READY.toString(),              
        StatusTStateType.PARTIALLYOBSERVED.toString()               
    };
    final public static String[] SBRunnableStates = {
        StatusTStateType.READY.toString(),              
        StatusTStateType.RUNNING.toString()             
    };
    
    private ContainerServices           container;
    private AcsLogger                   logger;
    private SBQueue                     sbQueue;
    private ProjectQueue                projectQueue;
    private StatusEntityQueueBundle     statusQs;
    private ProjectUtil                 projectUtil;
    private ALMAArchive                 archive;
    private ALMAClock                   clock;
    private Map<String, SchedBlock[]>   schedBlockBuffer;
    private Map<String, ObsProject>     obsProjectBuffer;
    
    public ArchivePoller(AcsLogger logger, String managerLoc, String clientName) throws Exception {
        super(logger, managerLoc, clientName);
        this.container = getContainerServices();
        this.logger = container.getLogger();
        this.sbQueue = new SBQueue();
        this.projectQueue = new ProjectQueue();
        this.statusQs = new StatusEntityQueueBundle(logger);
        this.clock = new ALMAClock();
        this.archive = new ALMAArchive(container, clock);
        this.projectUtil = archive.getProjectUtil();
        schedBlockBuffer = new HashMap<String, SchedBlock[]>();
        obsProjectBuffer = new HashMap<String, ObsProject>();
    }

    void pollArchive(String prjuid) throws SchedulingException {
        logger.fine("SCHEDULING: polling archive for runnable projects");
        
        schedBlockBuffer.clear();
        obsProjectBuffer.clear();
        
        archive.convertProjects(StatusTStateType.PHASE2SUBMITTED,
                                StatusTStateType.READY);
        final StatusEntityQueueBundle newQs =
            archive.determineRunnablesByStatus(OPRunnableStates,
                                               SBRunnableStates);
        statusQs.updateWith(newQs);
        
        OLD_PollArchive(prjuid);
        logger.info("The Scheduling Subsystem is currently managing "
                        + projectQueue.size() + " projects, "
                        + sbQueue.size() + " SBs, "
                        + statusQs.getProjectStatusQueue().size() + " project statuses and "
                        + statusQs.getSBStatusQueue().size() + " SB statuses");
        logNumbers("at end of pollArchive");
        logDetails("at end of pollArchive");
    }
    
    /**
     * polls the archive for new/updated projects
     * then updates the queues (project queue, sb queue & project status queue)
     */
   void OLD_PollArchive(String prjuid) throws SchedulingException {
       Project[] projectList = new Project[0];
       ProjectStatus ps;
       Vector<SB> tmpSBs = new Vector<SB>();
       final ProjectStatusQueue psQ  = statusQs.getProjectStatusQueue();
       final SBStatusQueue      sbsQ = statusQs.getSBStatusQueue();

       try {
           if (prjuid == null) {
               projectList = getAllProject();
           } else {
               projectList = new Project[1];
               projectList[0] = archive.getProject(prjuid);
           }
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
       logger.info("The Scheduling Subsystem is currently managing "
                               + projectQueue.size() + " projects, "
                               + sbQueue.size() + " SBs, "
                               + statusQs.getProjectStatusQueue().size() + " project statuses and "
                               + statusQs.getSBStatusQueue().size() + " SB statuses");
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


   private void logSBsAndStatuses(SBQueue       domainQueue,
           SBStatusQueue statusQueue) {
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

    private void checkSBUpdates() throws SchedulingException {
        try {
            SchedBlock[] sbs = archive.queryRecentSBs();
            // logger.fine("<check SBUpdates:>"+sbs.length);
            for (int i = 0; i < sbs.length; i++) {
                // System.out.println("schedblock name:"+sbs[i].getName());
                // first make sure the SB is a new SB or modify SB
                SB sb = sbQueue.get(sbs[i].getSchedBlockEntity().getEntityId());
                // System.out.println("sb name:"+sb.getSBName());
                if (sb == null) {

                    logger.fine("This is a new SB");
                    // sb=ProjectUtil.createSBfromSchedBlock(sbs[i],archive,pQueue);
                    // if(sb!=null) {
                    // System.out.println("new sb added:"+sb.getId());
                    // sbQueue.add(sb);
                    // }
                } else {

                    sb = projectUtil.updateSB(sb, sbs[i], clock.getDateTime());
                    sbQueue.replace(sb);
                    // logger.fine("<sb's name>"+sb.getSBName());
                    // logger.fine("<sb's program:>"+sb.getParent().getId());
                    // logger.fine("<sb program length>"+sb.getParent().getNumberMembers());
                }
            }
        } catch (SchedulingException e) {
            logger.warning("SCHEDULING: Problem checking for SB updates");
            throw e;
        }

    }

    private static SB[] getSBs(Vector v, String s) {
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

    public static void main(String[] args) {
        String managerLoc = System.getProperty("ACS.manager");
        if (managerLoc == null) {
            System.out
                    .println("Java property 'ACS.manager must be set to the corbaloc of the ACS manager!");
            System.exit(-1);
        }
        String clientName = "ArchivePoller";
        ArchivePoller poller = null;
        try {
            poller = new ArchivePoller(null, managerLoc, clientName);
            String uid = null;
            if (args.length != 0) uid = args[0];
            poller.pollArchive(uid);
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
        } finally {
            if (poller != null) {
                try {
                    poller.tearDown();
                } catch (Exception ex) {
                    ex.printStackTrace(System.err);
                }
            }
        }
    }
    
    public Project[] getAllProject() throws SchedulingException {
        Project[] projects = null;
        
        try {    
            Vector<Project> tmp_projects = new Vector<Project>();
            ObsProject[] obsProjects = archive.getAllObsProjects(
            		statusQs.getProjectStatusQueue());
            
            for (final ObsProject project : obsProjects) {
                obsProjectBuffer.put(project.getObsProjectEntity().getEntityId(), project);
                
                String projectId;
                try {
                    projectId = project.getProjectStatusRef().getEntityId();
                    try {
                        ProjectStatusI ps = archive.getProjectStatusForObsProject(project);
                        
                        //TODO should check project queue.. if project exists don't map a new one.
                        SchedBlock[] sbs = archive.getSBsFromObsProject(
                        		project,
                        		statusQs.getSBStatusQueue());
                        
                        // Cache the SBs to avoid having to perform another retrieval
                        // from the ARCHIVE.
                        schedBlockBuffer.put(project.getObsProjectEntity().getEntityId(), sbs);
                        
                        //add here to check if the sbs get without problem
                        if (sbs!=null) {    
                            Project p = projectUtil.map(project, sbs, ps, 
                                new DateTime(System.currentTimeMillis()));
                            if (p!=null){
                                tmp_projects.add(p);
                            }
                        } else {
                            logger.warning(String.format(
                                    "No SchedBlocks for project %s",
                                    projectId));
                            AcsJObsProjectRejectedEx ex = new AcsJObsProjectRejectedEx();
                            ex.setProperty("UID", projectId);
                            ex.setProperty("Reason", "No SchedBlocks for project");
                            ex.log(logger);

                        }
                    } catch (SchedulingException e) {
                        logger.warning(e.getLocalizedMessage());
//                        logger.warning(String.format(
//                              "Cannot find status object for project %s",
//                              projectId));
                    }
                } catch (NullPointerException e) {
                    logger.warning(String.format(
                            "Project from archive has no EntityId, project name is %s, PI is %s",
                            project.getProjectName(), project.getPI()));
                    projectId = null;
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
}
