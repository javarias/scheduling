package alma.scheduling.dataload;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import alma.scheduling.datamodel.config.Configuration;
import alma.scheduling.datamodel.config.ScienceGradeConfig.InvalidScienceGradeConfig;
import alma.scheduling.datamodel.config.dao.ConfigurationDao;
import alma.scheduling.datamodel.executive.PI;
import alma.scheduling.datamodel.executive.dao.ExecutiveDAO;
import alma.scheduling.datamodel.obsproject.ObsProject;
import alma.scheduling.datamodel.obsproject.SchedBlock;
import alma.scheduling.datamodel.obsproject.ScienceGrade;
import alma.scheduling.datamodel.obsproject.dao.ObsProjectDao;
import alma.scheduling.datamodel.obsproject.dao.SchedBlockDao;

public class DataLinker implements DataLoader {

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
        List<SchedBlock>sbs = sbDao.findAll();
        for(SchedBlock sb: sbs){
            PI pi = execDao.getPIFromEmail(sb.getPiName());
            sb.setExecutive(pi.getPIMembership().iterator().next().getExecutive());
            sbDao.saveOrUpdate(sb);
        }
        
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
            obsPrjDao.saveOrUpdate(p);
        }
    }

}
