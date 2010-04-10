package alma.scheduling.algorithm.observatory;

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import alma.scheduling.algorithm.VerboseLevel;
import alma.scheduling.algorithm.executive.ExecutiveSelector;
import alma.scheduling.algorithm.sbselection.AbstractBaseSelector;
import alma.scheduling.algorithm.sbselection.NoSbSelectedException;
import alma.scheduling.datamodel.observatory.ArrayConfiguration;
import alma.scheduling.datamodel.obsproject.SchedBlock;
import alma.scheduling.datamodel.obsproject.dao.SchedBlockDao;

public class ArrayConfigurationSelector extends AbstractBaseSelector{
    
    public ArrayConfigurationSelector(String selectorName) {
        super(selectorName);
    }

    private static Logger logger = LoggerFactory.getLogger(ExecutiveSelector.class);
    
    private SchedBlockDao sbDao;
    
    public SchedBlockDao getSbDao() {
        return sbDao;
    }

    public void setSbDao(SchedBlockDao sbDao) {
        this.sbDao = sbDao;
    }

    @Override
    @Transactional (readOnly=true)
    public Collection<SchedBlock> select() throws NoSbSelectedException {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UT"));
        return select(cal.getTime(),null);
    }

    @Override
    @Transactional (readOnly=true)
    public Collection<SchedBlock> select(Date ut) throws NoSbSelectedException {
        return select(ut, null);
    }


    @Override
    @Transactional(readOnly=true)
    public Collection<SchedBlock> select(ArrayConfiguration arrConf)
            throws NoSbSelectedException {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UT"));
        return select(cal.getTime(), arrConf);
    }
    
    @Override
    public Collection<SchedBlock> select(Date ut, ArrayConfiguration arrConf) {
        if (arrConf == null){
            logger.info("ArrayConfiguration is null, selecting all the SchedBlocks");
            return sbDao.findAll();
        }
        double remaningTime = (arrConf.getEndTime().getTime() - ut.getTime()) / (1000 * 60 * 60);
        Collection<SchedBlock> sbs = sbDao.findSchedBlocksByEstimatedExecutionTime(remaningTime);
        printVerboseInfo(sbs, arrConf.getId(), ut);
        return sbs;
    }
    
    @Override
    public String toString() {
        return getClass().toString();
    }
    
    

}
