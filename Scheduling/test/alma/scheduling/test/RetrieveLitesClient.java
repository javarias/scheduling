package alma.scheduling.test;

import java.util.logging.Logger;

import alma.acs.component.client.ComponentClient;
import alma.acs.logging.AcsLogger;
import alma.entity.xmlbinding.obsproject.types.ControlBlockTArrayRequestedType;
import alma.scheduling.MasterSchedulerIF;
import alma.scheduling.ProjectLite;
import alma.scheduling.SBLite;

public class RetrieveLitesClient extends ComponentClient {

    public static final String MASTER_SCHEDULER_COMP_NAME = "SCHEDULING_MASTERSCHEDULER";
    
    private Logger logger;
    private MasterSchedulerIF masterScheduler;

    public static void main(String[] args) {
        String managerLoc = System.getProperty("ACS.manager");
        if (managerLoc == null) {
            System.out.println("Java property 'ACS.manager must be set to the corbaloc of the ACS manager!");
            System.exit(-1);
        }
        String clientName = "RetrieveLitesClient";
        RetrieveLitesClient retriever = null;
        try {
            retriever = new RetrieveLitesClient(null, managerLoc, clientName);
            String uid = null;
            retriever.retrieveLites();
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
        } finally {
            if (retriever != null) {
                try {
                    retriever.tearDown();
                } catch (Exception ex) {
                    ex.printStackTrace(System.err);
                }
            }
        }
    }
    
    public RetrieveLitesClient(AcsLogger logger, String managerLoc, String clientName) throws Exception {
        super(logger, managerLoc, clientName);
        this.logger = getContainerServices().getLogger();
        this.masterScheduler = alma.scheduling.MasterSchedulerIFHelper.narrow(
                getContainerServices().getComponentNonSticky(MASTER_SCHEDULER_COMP_NAME));
    }

    @Override
    public void tearDown() throws Exception {
        getContainerServices().releaseComponent(MASTER_SCHEDULER_COMP_NAME);
        super.tearDown();
    }

    public void retrieveLites() throws Exception {
        String pName = "*";
        String piName = "*";
        String[] projectTypes = {"All","Continuum","Polarization","Other"};
        String[] arrayTypes = {
                "All", 
                ControlBlockTArrayRequestedType.ACA.toString(),
                ControlBlockTArrayRequestedType.TWELVE_M.toString(),
                ControlBlockTArrayRequestedType.SEVEN_M.toString(),
                ControlBlockTArrayRequestedType.TP_ARRAY.toString()
            };
        
        logger.info("queryArchive");
        String[] sbs = masterScheduler.queryArchive(makeSBQuery(), "SchedBlock");
        logger.info("queryForProject");
        String[] projs = masterScheduler.queryForProject(pName, piName, projectTypes[0],
                arrayTypes[0], false);
        logger.info("getSBProjectUnion");
        String[] unionSB = masterScheduler.getSBProjectUnion(sbs,projs);
        logger.info("getExistingSBLite");
        SBLite[] unionSBLites = masterScheduler.getExistingSBLite(unionSB);
        logger.info("getProjectSBUnion");
        String[] unionProj = masterScheduler.getProjectSBUnion(projs,sbs);
        logger.info("getProjectLites");
        ProjectLite[] unionProjectLites = masterScheduler.getProjectLites(unionProj);
    }
        
    private String makeSBQuery() {
        String[] modeTypeChoices = {"All","Observatory", "User", "Expert"};
        String sbModeType = modeTypeChoices[0];
        String[] modeNameChoices = 
        {"All","Single Field Interferometry","Optical Pointing","Tower Holography", "Expert Mode"};
        String sbModeName = modeNameChoices[0];
        String query;
        if(sbModeName.equals("All") && sbModeType.equals("All") )  {
            query="/*";
        } else if(sbModeType.equals("All") ) {
            query="/sbl:SchedBlock[sbl:modeName=\""+sbModeName+"\"]";
        } else if(sbModeName.equals("All")) {
            query="/sbl:SchedBlock[@modeType=\""+sbModeType+"\"]";
        } else {
            query = "/sbl:SchedBlock[@modeType=\""+
                        sbModeType+"\" and sbl:modeName=\""+sbModeName+"\"]";
        }
        return query;
    }
}
