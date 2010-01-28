package alma.scheduling.datamodel.executive.dao;

import java.util.List;

import alma.scheduling.datamodel.executive.Executive;
import alma.scheduling.datamodel.executive.ExecutiveTimeSpent;
import alma.scheduling.datamodel.executive.ObservingSeason;

public interface ExecutiveDAO {

    public List<Executive> getAllExecutive();
    
    public ObservingSeason getCurrentSeason();
    
    public ExecutiveTimeSpent getExecutiveTimeSpent(Executive ex, ObservingSeason os);
    
    public void SaveOrUpdate(ExecutiveTimeSpent ets);
}
