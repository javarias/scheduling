package alma.scheduling.datamodel.executive.dao;

import java.util.List;

import org.hibernate.Query;
import org.springframework.transaction.annotation.Transactional;

import alma.scheduling.datamodel.GenericDaoImpl;
import alma.scheduling.datamodel.executive.Executive;
import alma.scheduling.datamodel.executive.ExecutivePercentage;
import alma.scheduling.datamodel.executive.ExecutiveTimeSpent;
import alma.scheduling.datamodel.executive.ObservingSeason;
import alma.scheduling.datamodel.executive.PI;

public class ExecutiveDaoImpl extends GenericDaoImpl implements ExecutiveDAO{

    @Override
    @Transactional(readOnly=true)
    public List<Executive> getAllExecutive() {
        return this.findAll(Executive.class);
    }
    
    @Override
    @Transactional(readOnly=true)
    public List<ObservingSeason> getAllObservingSeason() {
        return this.findAll(ObservingSeason.class);
    }

    @Override
    public List<PI> getAllPi() {
        return this.findAll(PI.class);
    }

    @SuppressWarnings("unchecked")
    @Override
    @Transactional(readOnly=true)
    public ObservingSeason getCurrentSeason() {
        List<ObservingSeason> os;
        Query query = getSession().createQuery("from ObservingSeason as os " +
                "order by os.startDate desc");
        query.setMaxResults(1);
        os = query.list();
        return os.get(0);
    }

    @Override
    @Transactional(readOnly=true)
    public Executive getExecutive(String piName) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    @Transactional(readOnly=true)
    public List<ExecutiveTimeSpent> getExecutiveTimeSpent(Executive ex,
            ObservingSeason os) {
        Object[] args= new Object[2];
        args[0] = os.getStartDate();
        args[1] = ex.getName();
        return this.executeNamedQuery(
                "ExecutiveTimeSpent.findBySeasonAndExecutive",args);
    }
    
    @Override
    @Transactional(readOnly=true)
    public ExecutivePercentage getExecutivePercentage(Executive ex,
            ObservingSeason os) {
        Object[] args= new Object[2];
        args[0] =os.getStartDate();
        args[1] = ex.getName();
        return (ExecutivePercentage) this.executeNamedQuery(
                "ExecutivePercentage.findBySeasonAndExecutive",args).get(0);
    }
}