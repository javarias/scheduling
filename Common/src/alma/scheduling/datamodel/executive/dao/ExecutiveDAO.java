package alma.scheduling.datamodel.executive.dao;

import java.util.Collection;
import java.util.List;

import alma.scheduling.datamodel.GenericDao;
import alma.scheduling.datamodel.executive.Executive;
import alma.scheduling.datamodel.executive.ExecutivePercentage;
import alma.scheduling.datamodel.executive.ExecutiveTimeSpent;
import alma.scheduling.datamodel.executive.ObservingSeason;
import alma.scheduling.datamodel.executive.PI;

public interface ExecutiveDAO extends GenericDao{

    public List<Executive> getAllExecutive();
    
    public ObservingSeason getCurrentSeason();
    
    public List<ExecutiveTimeSpent> getExecutiveTimeSpent(Executive ex, ObservingSeason os);
    
    public Executive getExecutive(String piName);

    public ExecutivePercentage getExecutivePercentage(Executive ex,
            ObservingSeason os);
    
    public void PopulateDB(Collection<PI> pi, Collection<Executive> exec, Collection<ObservingSeason> os);
}
