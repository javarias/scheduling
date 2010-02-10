package alma.scheduling.datamodel.executive.dao;

import java.util.ArrayList;
import java.util.Collection;
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
    public List<Executive> getAllExecutive() {
        return this.findAll(Executive.class);
    }

    @SuppressWarnings("unchecked")
    @Override
    public ObservingSeason getCurrentSeason() {
        List<ObservingSeason> os;
        Query query = getSession().createQuery("from ObservingSeason as os " +
                "order by os.startDate desc");
        query.setMaxResults(1);
        os = query.list();
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
        args[0] = os.getStartDate();
        args[1] = ex.getName();
        return this.executeNamedQuery(
                "ExecutiveTimeSpent.findBySeasonAndExecutive",args);
    }
    
    public ExecutivePercentage getExecutivePercentage(Executive ex,
            ObservingSeason os) {
        Object[] args= new Object[2];
        args[0] =os.getStartDate();
        args[1] = ex.getName();
        return (ExecutivePercentage) this.executeNamedQuery(
                "ExecutivePercentage.findBySeasonAndExecutive",args).get(0);
    }

    @Transactional
    @Override
    public void PopulateDB(Collection<PI> pi, Collection<Executive> exec,
            Collection<ObservingSeason> os) {
        ArrayList<Object> objs = new ArrayList<Object>();
        objs.addAll(exec);
        objs.addAll(os);
        objs.addAll(pi);
        getHibernateTemplate().saveOrUpdateAll(objs);
    }
    

}
