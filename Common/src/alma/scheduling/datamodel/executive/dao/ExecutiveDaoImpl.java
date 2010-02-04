package alma.scheduling.datamodel.executive.dao;

import java.util.List;

import alma.scheduling.datamodel.GenericDaoImpl;
import alma.scheduling.datamodel.executive.Executive;
import alma.scheduling.datamodel.executive.ExecutivePercentage;
import alma.scheduling.datamodel.executive.ExecutiveTimeSpent;
import alma.scheduling.datamodel.executive.ObservingSeason;

public class ExecutiveDaoImpl extends GenericDaoImpl implements ExecutiveDAO{

    @Override
    public List<Executive> getAllExecutive() {
        return this.findAll(Executive.class);
    }

    @SuppressWarnings("unchecked")
    @Override
    public ObservingSeason getCurrentSeason() {
        List<ObservingSeason> os = (List<ObservingSeason>)
        getHibernateTemplate().find("select top 1 os from ObservingSeason as os " +
                "order by os.date desc");
        return os.get(0);
    }

    @Override
    public Executive getExecutive(String piName) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<ExecutiveTimeSpent> getExecutiveTimeSpent(Executive ex,
            ObservingSeason os) {
        Object[] args= new Object[2];
        args[0] = os;
        args[1] = ex;
        return this.executeNamedQuery(
                "ExecutiveTimeSpent.findBySeasonAndExecutive",args);
    }
    
    public ExecutivePercentage getExecutivePercentage(Executive ex,
            ObservingSeason os) {
        Object[] args= new Object[2];
        args[0] = os.getStartDate().toString();
        args[1] = ex.getName();
        return (ExecutivePercentage) this.executeNamedQuery(
                "ExecutiveTimeSpent.findBySeasonAndExecutive",args).get(0);
    }

}
