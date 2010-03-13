package alma.scheduling.datamodel.executive.dao;

import java.util.List;

import alma.scheduling.datamodel.executive.Executive;
import alma.scheduling.datamodel.executive.ExecutivePercentage;
import alma.scheduling.datamodel.executive.ExecutiveTimeSpent;
import alma.scheduling.datamodel.executive.ObservingSeason;
import alma.scheduling.datamodel.executive.PI;

public interface XmlExecutiveDAO {

    public List<Executive> getAllExecutive();
    
    public List<ObservingSeason> getAllObservingSeason();
    
    public List<PI> getAllPi();
    
    public ObservingSeason getCurrentSeason();
    
    public List<ExecutiveTimeSpent> getExecutiveTimeSpent(Executive ex, ObservingSeason os);
    
    public Executive getExecutive(String piName);

    public ExecutivePercentage getExecutivePercentage(Executive ex,
            ObservingSeason os);
}
