package alma.scheduling.algorithm.sbselection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import alma.scheduling.datamodel.executive.Executive;
import alma.scheduling.datamodel.executive.ExecutivePercentage;
import alma.scheduling.datamodel.executive.ExecutiveTimeSpent;
import alma.scheduling.datamodel.executive.ObservingSeason;
import alma.scheduling.datamodel.executive.PI;
import alma.scheduling.datamodel.executive.PIMembership;
import alma.scheduling.datamodel.executive.dao.ExecutiveDAO;
import alma.scheduling.datamodel.obsproject.SchedBlock;
import alma.scheduling.datamodel.obsproject.dao.SchedBlockDao;

public class ExecutiveSelector implements SchedBlockSelector {

    private ExecutiveDAO execDao;
    private SchedBlockDao sbDao;
    private HashMap<String, Double> availableTime;
    
    public ExecutiveDAO getExecDao() {
        return execDao;
    }

    public void setExecDao(ExecutiveDAO execDao) {
        this.execDao = execDao;
    }

    public SchedBlockDao getSbDao() {
        return sbDao;
    }

    public void setSbDao(SchedBlockDao sbDao) {
        this.sbDao = sbDao;
    }
    
    /*
     * in this Selector all the SBs get 1 hour to be completed
     * (non-Javadoc)
     * @see alma.scheduling.algorithm.sbselection.SchedBlockSelector#select()
     */
    @Override
    public Collection<SchedBlock> select() throws NoSbSelectedException{
        calculateRemainingTime();
        List<SchedBlock> acceptedSbs =  new ArrayList<SchedBlock>();
        List<SchedBlock> sbs =  sbDao.findAll(SchedBlock.class);
        for(SchedBlock sb: sbs){
            PI pi = execDao.findById(PI.class, sb.getPiName());
            Iterator<PIMembership> it = pi.getPIMembership().iterator();
            Double avTime = availableTime.get(
                    it.next().getExecutive().getName());
            if (avTime.doubleValue() >= 1)
                acceptedSbs.add(sb);
        }
        if(acceptedSbs.size() == 0){
            String strCause = "Cannot get any SB valid to be ranked using " + this.toString();
            throw new NoSbSelectedException(strCause);
        }
        return acceptedSbs;
    }
    
    private void calculateRemainingTime(){
        if (availableTime == null)
            availableTime = new HashMap<String, Double>();
        availableTime.clear();
        List<Executive> execs = execDao.getAllExecutive();
        ObservingSeason currOs = execDao.getCurrentSeason();
        for(Executive exec: execs){
            ExecutivePercentage ep = execDao.getExecutivePercentage(exec, currOs);
            List<ExecutiveTimeSpent> etss = 
                execDao.getExecutiveTimeSpent(exec, currOs);
            double spentTime = 0;
            for(ExecutiveTimeSpent ets: etss){
                spentTime += ets.getTimeSpent();
            }
            availableTime.put(exec.getName(), 
                    new Double(ep.getTotalObsTimeForSeason() - spentTime));
        }
        
    }

    @Override
    public String toString() {
        return "ExecutiveSelector";
    }

    
}
