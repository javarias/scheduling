package alma.scheduling.util;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import org.exolab.castor.xml.XMLException;
import org.hibernate.Session;
import org.hibernate.Transaction;

import alma.acs.component.client.ComponentClient;
import alma.acs.container.ContainerServices;
import alma.acs.entityutil.EntityDeserializer;
import alma.acs.logging.AcsLogger;
import alma.entity.xmlbinding.obsproject.ObsProject;
import alma.entity.xmlbinding.obsproject.ObsProjectRefT;
import alma.entity.xmlbinding.ousstatus.OUSStatus;
import alma.entity.xmlbinding.ousstatus.OUSStatusRefT;
import alma.entity.xmlbinding.projectstatus.ProjectStatus;
import alma.entity.xmlbinding.sbstatus.SBStatus;
import alma.entity.xmlbinding.sbstatus.SBStatusRefT;
import alma.entity.xmlbinding.schedblock.SchedBlock;
import alma.entity.xmlbinding.schedblock.SchedBlockRefT;
import alma.lifecycle.persistence.domain.OUSStatusPF;
import alma.lifecycle.persistence.domain.ObsProjectStatusPF;
import alma.lifecycle.persistence.domain.SchedBlockStatusPF;
import alma.xmlentity.XmlEntityStruct;
import alma.xmlstore.ArchiveConnection;
import alma.xmlstore.ArchiveConnectionHelper;
import alma.xmlstore.Operational;

public class StatusChecker extends ComponentClient {

    public static final String ARCHIVE_CONNECTION_COMP = "ARCHIVE_CONNECTION";

    private class ReportComposer {
                
        private String report;
        private int level;
        private String del_cmds;
        
        public ReportComposer() {
            report = "";
            level = 0;
            del_cmds = "";
        }
        
        public void startProjectStatus(String uid) {
            level = 0;
            report += "ProjectStatus " + uid + " {\n";
            del_cmds = "";
            del_cmds += "delete from obs_project_status where status_entity_id = '" + uid + "';\n";
        }

        public void endProjectStatus(ProjectStatus status, String info) {
            report += "} " + info + "\n";
            if (!info.equals("[fine]")) {
                report += del_cmds;
            }
        }

        public void startObsProject(String uid) {
            report += "(ObsProject " + uid;
            del_cmds += "update xml_obsproject_entities set deleted = 1 where archive_uid = '" + uid + "';\n";
        }

        public void endObsProject(ObsProject obsProject, String info) {
            report += " " + info + ")\n";
        }
        
        public void startOUSStatus(String uid) {
            level++;
            report += getIndentation() + "OUSStatus " + uid + " {\n";
            del_cmds += "delete from obs_unit_set_status where status_entity_id = '" + uid + "';\n";
        }

        public void endOUSStatus(OUSStatus status, String info) {
            report += getIndentation() + "} " + info + "\n";
            level--;
        }
        
        public void startSBStatus(String uid) {
            report += getIndentation() + "    SBStatus " + uid + " ";
            del_cmds += "delete from sched_block_status where status_entity_id = '" + uid + "';\n";
        }
        
        public void endSBStatus(SBStatus status, String info) {
            report += info + "\n";            
        }

        public void startSchedBlock(String uid) {
            report += "(SchedBlock " + uid; 
            del_cmds += "update xml_schedblock_entities set deleted = 1 where archive_uid = '" + uid + "';\n";
        }

        public void endSchedBlock(SchedBlock schedBlock, String info) {
            report += " " + info + ") ";
        }
        
        public void report() {
            System.out.println(report);
        }
        
        private String getIndentation() {
            String retVal = "";
            for (int i=0; i < level; i++) retVal += "    ";
            return retVal;
        }
    }
    
    private ContainerServices container;
    private Logger logger;
    private ArchiveConnection archConnection;
    private Operational archOperational;
    private Session session;
    private ReportComposer reporter;
    private EntityDeserializer entityDeserializer;
    
    public StatusChecker(AcsLogger logger, String managerLoc, String clientName) throws Exception {
        super(logger, managerLoc, clientName);
        this.container = getContainerServices();
        this.logger = container.getLogger();
        // Get Archive components
        this.archConnection =
            ArchiveConnectionHelper.narrow(container.getComponent(ARCHIVE_CONNECTION_COMP));
        this.archOperational = archConnection.getOperational("ObsProjectChecker");
        // Initialize Hibernate Session
        this.session =
            HibernateUtil.getSessionFactory().openSession();
        this.reporter = new ReportComposer();
        entityDeserializer = EntityDeserializer.getEntityDeserializer(logger);
    }
    
    public void tearDown() throws Exception {
        // Shutting down Hibernate Session
        session.close();        
        HibernateUtil.shutdown();
        // Release ACS components
        container.releaseComponent(ARCHIVE_CONNECTION_COMP);
        super.tearDown();
    }
    
    public void check(String prjStatusId) {
        Transaction tx = session.beginTransaction();
        List prjStatusPFs;
        if (prjStatusId == null) {
            prjStatusPFs =
            session.createQuery("from ObsProjectStatusPF").list();
        } else {
            String query = String.format("from ObsProjectStatusPF where statusEntityId = '%s'", prjStatusId);
            prjStatusPFs = session.createQuery(query).list();
        }
        
        logger.info(prjStatusPFs.size() +
                " ObsProjectStatusPF(s) found:");
        for (Iterator iter = prjStatusPFs.iterator(); iter.hasNext(); ) {
            ObsProjectStatusPF prjStatusPF = (ObsProjectStatusPF) iter.next();
            logger.info(prjStatusPF.getProjectStatusId());
            reporter.startProjectStatus(prjStatusPF.getProjectStatusId());
            
            try {
                ProjectStatus prjStatus =
                    ProjectStatus.unmarshalProjectStatus(new StringReader(prjStatusPF.getXml()));
                
                OUSStatus rootOUSStatus = getOUSStatus(prjStatus.getObsProgramStatusRef());
                List<SBStatus> sbStatuses = new ArrayList<SBStatus>();
                getSBStatuses(rootOUSStatus, sbStatuses);
                try {
                    reporter.startObsProject(prjStatus.getObsProjectRef().getEntityId());
                    ObsProject obsProject = getObsProject(prjStatus.getObsProjectRef());
                    reporter.endObsProject(obsProject, "[fine]");
                } catch(Exception ex) {
                    reporter.endObsProject(null, "[" + ex +"]");
                    throw ex;
                }
               reporter.endProjectStatus(prjStatus, "[fine]"); 
            } catch (Exception e) {
                reporter.endProjectStatus(null, "[" + e +"]");
            }
        }        
        tx.commit();
        reporter.report();
    }
    
    private OUSStatus getOUSStatus(OUSStatusRefT ref) throws Exception {
        String query = String.format("from OUSStatusPF where statusEntityId = '%s'",
                ref.getEntityId());
        OUSStatusPF ousStatusPF =
            (OUSStatusPF) session.createQuery(query).uniqueResult();
        if (ousStatusPF == null)
            throw new NullPointerException(ref.getEntityId() + " not found in database");
        OUSStatus ousStatus = OUSStatus.unmarshalOUSStatus(new StringReader(ousStatusPF.getXml()));
        return ousStatus;
    }
    
    private void getSBStatuses(OUSStatus ousStatus, List<SBStatus> sbStatuses) throws XMLException {
        SBStatusRefT[] sbStatusRefs = ousStatus.getOUSStatusChoice().getSBStatusRef();
        for (int i = 0; i < sbStatusRefs.length; i++) {
            try {
                reporter.startSBStatus(sbStatusRefs[i].getEntityId());
                SBStatus sbStatus = getSBStatus(sbStatusRefs[i]);
                sbStatuses.add(sbStatus);
                try {
                    reporter.startSchedBlock(sbStatus.getSchedBlockRef().getEntityId());
                    SchedBlock schedBlock = getSchedBlock(sbStatus.getSchedBlockRef());
                    reporter.endSchedBlock(schedBlock, "[fine]");
                } catch(Exception ex) {
                    reporter.endSchedBlock(null, "[" + ex +"]");
                    throw ex;
                }
                reporter.endSBStatus(sbStatus, "[fine]");
            } catch (Exception ex) {
                reporter.endSBStatus(null, "[" + ex +"]");
            }
        }
        OUSStatusRefT[] ousStatusRefs = ousStatus.getOUSStatusChoice().getOUSStatusRef();
        for (int i = 0; i < ousStatusRefs.length; i++) {
            try {
                reporter.startOUSStatus(ousStatusRefs[i].getEntityId());
                OUSStatus childOUSStatus = getOUSStatus(ousStatusRefs[i]);
                getSBStatuses(childOUSStatus, sbStatuses);
                reporter.endOUSStatus(childOUSStatus, "[fine]");
            } catch (Exception ex) {
                reporter.endOUSStatus(null, "[" + ex +"]");                
            }
        }
    }
    
    private SBStatus getSBStatus(SBStatusRefT ref) throws Exception {
        String query = String.format("from SchedBlockStatusPF where statusEntityId = '%s'",
                ref.getEntityId());
        SchedBlockStatusPF sbStatusPF =
            (SchedBlockStatusPF) session.createQuery(query).uniqueResult();
        if (sbStatusPF == null)
            throw new NullPointerException(ref.getEntityId() + " not found in database");
        SBStatus sbStatus = SBStatus.unmarshalSBStatus(new StringReader(sbStatusPF.getXml()));
        return sbStatus;
    }

    private ObsProject getObsProject(ObsProjectRefT ref) throws Exception {
        ObsProject project = null;
        XmlEntityStruct xmlent = archOperational.retrieveDirty(ref.getEntityId());
        project = (ObsProject) entityDeserializer.deserializeEntity(xmlent, ObsProject.class);
        return project;
    }
    
    private SchedBlock getSchedBlock(SchedBlockRefT ref) throws Exception {
        SchedBlock schedblock = null;
        XmlEntityStruct xmlent = archOperational.retrieveDirty(ref.getEntityId());
        schedblock = (SchedBlock) entityDeserializer.deserializeEntity(xmlent, SchedBlock.class);
        return schedblock;        
    }
    
    public static void main(String[] args) {

        String managerLoc = System.getProperty("ACS.manager");
        if (managerLoc == null) {
            System.out.println("Java property 'ACS.manager must be set to the corbaloc of the ACS manager!");
            System.exit(-1);
        }
        String clientName = "ObsProjectChecker";
        StatusChecker checker = null;
        try {
            checker = new StatusChecker(null, managerLoc, clientName);
            String prjstatusid = null;
            if (args.length > 0)
                prjstatusid = args[0];
            checker.check(prjstatusid);
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
        } finally {
            if (checker != null) {
                try {
                    checker.tearDown();
                } catch (Exception ex) {
                    ex.printStackTrace(System.err);
                }
            }
        }
    }
}
