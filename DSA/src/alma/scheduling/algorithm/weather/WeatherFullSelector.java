package alma.scheduling.algorithm.weather;

import java.util.Collection;

import alma.scheduling.algorithm.sbselection.NoSbSelectedException;
import alma.scheduling.algorithm.sbselection.SchedBlockSelector;
import alma.scheduling.datamodel.obsproject.SchedBlock;
import alma.scheduling.datamodel.obsproject.dao.SchedBlockDao;

public class WeatherFullSelector implements SchedBlockSelector {

    private SchedBlockDao schedBlockDao;
    public void setSchedBlockDao(SchedBlockDao schedBlockDao) {
        this.schedBlockDao = schedBlockDao;
    }
    
    @Override
    public Collection<SchedBlock> select() throws NoSbSelectedException {
        return schedBlockDao.findAll();
    }

}
