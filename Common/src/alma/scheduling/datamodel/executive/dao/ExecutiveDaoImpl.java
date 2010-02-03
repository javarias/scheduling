package alma.scheduling.datamodel.executive.dao;

import java.util.List;

import alma.scheduling.datamodel.GenericDaoImpl;
import alma.scheduling.datamodel.executive.Executive;
import alma.scheduling.datamodel.executive.ExecutiveTimeSpent;
import alma.scheduling.datamodel.executive.ObservingSeason;

public class ExecutiveDaoImpl extends GenericDaoImpl implements ExecutiveDAO{

    @Override
    public List<Executive> getAllExecutive() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ObservingSeason getCurrentSeason() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Executive getExecutive(String piName) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<ExecutiveTimeSpent> getExecutiveTimeSpent(Executive ex,
            ObservingSeason os) {
        // TODO Auto-generated method stub
        return null;
    }

}
