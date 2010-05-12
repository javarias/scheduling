package alma.scheduling.dataload;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import alma.scheduling.datamodel.executive.PI;
import alma.scheduling.datamodel.executive.dao.ExecutiveDAO;
import alma.scheduling.datamodel.obsproject.SchedBlock;
import alma.scheduling.datamodel.obsproject.dao.SchedBlockDao;

public class DataLinker implements DataLoader {

    private SchedBlockDao sbDao;
    private ExecutiveDAO execDao;
    
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

    @Override
    public void clear() {
        //Nothing to do with clear method
    }

    @Override
    @Transactional( readOnly=false)
    public void load() {
        System.out.println("");
        List<SchedBlock>sbs = sbDao.findAll();
        for(SchedBlock sb: sbs){
            PI pi = execDao.getPIFromEmail(sb.getPiName());
            sb.setExecutive(pi.getPIMembership().iterator().next().getExecutive());
            sbDao.saveOrUpdate(sb);
        }
    }

}
