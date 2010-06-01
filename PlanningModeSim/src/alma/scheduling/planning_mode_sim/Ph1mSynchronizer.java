package alma.scheduling.planning_mode_sim;

import java.util.List;
import java.util.logging.Logger;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import alma.archive.database.helpers.wrappers.DbConfigException;
import alma.archive.database.helpers.wrappers.RelationalDbConfig;
import alma.obops.dam.config.Ph1mContextFactory;
import alma.obops.dam.ph1m.dao.Ph1mDao;
import alma.obops.dam.ph1m.domain.Proposal;
import alma.scheduling.dataload.DataLoader;
import alma.scheduling.datamodel.obsproject.ObsProject;
import alma.scheduling.datamodel.obsproject.dao.ObsProjectDao;

public class Ph1mSynchronizer {
    
    private static Logger logger = Logger.getLogger(Ph1mSynchronizer.class.getName());
    
    private Ph1mDao ph1mDao;
    private ObsProjectDao obsProjDao;
    private DataLoader linker;
    
    /**
     * 
     * @param p the ObsProject to be syncronized
     * @throws IllegalArgumentException if the project is null of the uid is invalid
     * @throws NullPointerException if the proposal retrived is null
     */
    public void syncrhonizeProject(ObsProject p) throws IllegalArgumentException, NullPointerException {
        logger.info("Synchronizing proposal: " + p.getUid());
        if(p == null || p.getUid() == null)
            throw new IllegalArgumentException("Obs Project is null or project uid is null ");
        Proposal prop = ph1mDao.findProposalByArchiveUid(p.getUid());
        if(prop == null)
            throw new NullPointerException("Proposal retrieved from Phase1m with uid: " + p.getUid() + " is null");
        p.setScienceRank(prop.getAssessment().getAprcRank());
        if (prop.getAssessment().getAprcScore() == null)
            throw new NullPointerException("aprc Score is null");
        p.setScienceScore(prop.getAssessment().getAprcScore().floatValue());
        
        obsProjDao.saveOrUpdate(p);
    }
    
    public void synchronizeAllProjects() throws IllegalArgumentException {
        List<ObsProject> prjs = obsProjDao.findAll(ObsProject.class);
        for(ObsProject p: prjs){
            try{
                syncrhonizeProject(p);
            }catch(NullPointerException e){
                logger.info("Project " + p.getUid() + " cannot be retieved from ph1m. Reason: " + e.getCause());
            }
        }
        try {
            linker.load();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void printReport(){
        System.out.println("Project UID\tAPRC Score\tAPRC Rank");
        List<ObsProject> prjs = obsProjDao.findAll(ObsProject.class);
        for(ObsProject p: prjs){
            Proposal prop = ph1mDao.findProposalByArchiveUid(p.getUid());
            if(prop == null)
                continue;
            String line =p.getUid() + "\t";
            line += prop.getAssessment().getAprcScore() + "\t";
            line += prop.getAssessment().getAprcRank();
            System.out.println(line);
        }
    }
    
    public void init(String ctxPath){
        //Scheduling context init
        ApplicationContext simCtx = new FileSystemXmlApplicationContext("file://"+ctxPath);
        obsProjDao = (ObsProjectDao) simCtx.getBean("obsProjectDao");
        linker = (DataLoader) simCtx.getBean("dataLinker");
        
        //Ph1m context init
        RelationalDbConfig ph1mDbConfig;
        try {
            ph1mDbConfig = new RelationalDbConfig(logger);
            Ph1mContextFactory.INSTANCE.init("/ph1mContext.xml", ph1mDbConfig);
            
        } catch (DbConfigException e) {
            e.printStackTrace();
            System.exit(1);
        }
        ph1mDao = Ph1mContextFactory.INSTANCE.getPh1mDao();
    }

}
