package alma.scheduling.output;

import org.springframework.transaction.annotation.Transactional;

import alma.scheduling.datamodel.GenericDao;
import alma.scheduling.datamodel.executive.ExecutiveTimeSpent;
import alma.scheduling.datamodel.executive.dao.ExecutiveDAO;
import alma.scheduling.datamodel.obsproject.SchedBlock;

public class ExecutiveReporter implements Reporter {

    private ExecutiveDAO execDao;
    
    public ExecutiveDAO getExecDao() {
        return execDao;
    }

    public void setExecDao(ExecutiveDAO execDao) {
        this.execDao = execDao;
    }

    @Override
    public void generateXMLOutput() {
        // TODO Auto-generated method stub
        // Not implemented?
    }

    @Override
    @Transactional
    public void report(SchedBlock schedBlock) {
        ExecutiveTimeSpent ets = new ExecutiveTimeSpent();
        ets.setExecutive(execDao.getExecutive(schedBlock.getPiName()));
        ets.setObservingSeason(execDao.getCurrentSeason());
        ets.setSbId(schedBlock.getId());
        //TODO: Change this
        ets.setTimeSpent((float) 1.0);
        ((GenericDao) execDao).saveOrUpdate(ets);
    }

}
