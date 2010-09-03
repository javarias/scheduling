package alma.scheduling.dataload;

import java.util.HashSet;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import alma.scheduling.datamodel.config.Configuration;
import alma.scheduling.datamodel.config.ScienceGradeConfig.InvalidScienceGradeConfig;
import alma.scheduling.datamodel.config.dao.ConfigurationDao;
import alma.scheduling.datamodel.executive.PI;
import alma.scheduling.datamodel.executive.PIMembership;
import alma.scheduling.datamodel.executive.dao.ExecutiveDAO;
import alma.scheduling.datamodel.obsproject.ObsProject;
import alma.scheduling.datamodel.obsproject.ObsUnit;
import alma.scheduling.datamodel.obsproject.ObsUnitSet;
import alma.scheduling.datamodel.obsproject.SchedBlock;
import alma.scheduling.datamodel.obsproject.SchedBlockState;
import alma.scheduling.datamodel.obsproject.ScienceGrade;
import alma.scheduling.datamodel.obsproject.dao.ObsProjectDao;
import alma.scheduling.datamodel.obsproject.dao.SchedBlockDao;

public class DataLinker implements DataLoader {

	private static Logger logger = LoggerFactory.getLogger(DataLinker.class);
	
    private SchedBlockDao sbDao;
    private ExecutiveDAO execDao;
    private ConfigurationDao configDao;
    private ObsProjectDao obsPrjDao;
    
    public ConfigurationDao getConfigDao() {
        return configDao;
    }

    public void setConfigDao(ConfigurationDao configDao) {
        this.configDao = configDao;
    }

    public SchedBlockDao getSbDao() {
        return sbDao;
    }

    public void setSbDao(SchedBlockDao sbDao) {
        this.sbDao = sbDao;
    }

    public ExecutiveDAO getExecDao() {
        return execDao;
    }

    public void setExecDao(ExecutiveDAO execDao) {
        this.execDao = execDao;
    }

    public ObsProjectDao getObsPrjDao() {
        return obsPrjDao;
    }

    public void setObsPrjDao(ObsProjectDao obsPrjDao) {
        this.obsPrjDao = obsPrjDao;
    }

    @Override
    public void clear() {
        //Nothing to do with clear method
    }

    @Override
    @Transactional( readOnly=false)
    public void load() throws InvalidScienceGradeConfig {
        List<ObsProject>prjs = obsPrjDao.getObsProjectsOrderBySciRank();
        long i = 0;
        for(ObsProject p: prjs){
            if(i < configDao.getConfiguration().getScienceGradeConfig().getnGradeAPrj())
                p.setLetterGrade(ScienceGrade.A);
            else if (i >=  configDao.getConfiguration().getScienceGradeConfig().getnGradeAPrj() && 
                    i < configDao.getConfiguration().getScienceGradeConfig().getnGradeAPrj() + configDao.getConfiguration().getScienceGradeConfig().getnGradeBPrj())
                p.setLetterGrade(ScienceGrade.B);
            else if (i >= configDao.getConfiguration().getScienceGradeConfig().getnGradeBPrj() && 
                    i < configDao.getConfiguration().getScienceGradeConfig().getnGradeAPrj() + configDao.getConfiguration().getScienceGradeConfig().getnGradeBPrj() + configDao.getConfiguration().getScienceGradeConfig().getnGradeCPrj())
                p.setLetterGrade(ScienceGrade.C);
            else
                p.setLetterGrade(ScienceGrade.D);
            i++;
        }
        obsPrjDao.saveOrUpdate(prjs);
        
        
        List<SchedBlock>sbs = sbDao.findAll();
        for(SchedBlock sb: sbs){
            System.out.println("sb.getPiName() = " + sb.getPiName());
            PI pi = execDao.getPIFromEmail(sb.getPiName());
            if(pi == null){
                pi = new PI();
                pi.setEmail(sb.getPiName());
                pi.setEmail(sb.getPiName());
                pi.setPIMembership(new HashSet<PIMembership>());
                PIMembership pim = new PIMembership();
                pim.setExecutive(execDao.getAllExecutive().get(0));
                pim.setMembershipPercentage(1);
                pi.getPIMembership().add(pim);
                System.out.println("WARNING: Adding new PI: sb.getPiName()");
                execDao.saveOrUpdate(pi);
            }
            sb.setExecutive(pi.getPIMembership().iterator().next().getExecutive());
            ObsProject p = obsPrjDao.getObsProject(sb);
            sb.setScienceScore(p.getScienceScore());
            sb.setLetterGrade(p.getLetterGrade());
            sb.setScienceRank(p.getScienceRank());
            if(p.getStatus().compareTo("CANCELLED") == 0)
                sb.getSchedBlockControl().setState(SchedBlockState.CANCELED);
        }
        sbDao.saveOrUpdate(sbs);
        
        Configuration config = configDao.getConfiguration();
        config.getScienceGradeConfig().setTotalPrj(sbs.size());
        config.getScienceGradeConfig()
                .setnGradeDPrj(
                        config.getScienceGradeConfig().getTotalPrj()
                                - (config.getScienceGradeConfig().getnGradeAPrj()
                                   + config.getScienceGradeConfig().getnGradeBPrj() 
                                   + config.getScienceGradeConfig().getnGradeCPrj()));
        config.getScienceGradeConfig().testValues();
        configDao.updateConfig();
    }
}
