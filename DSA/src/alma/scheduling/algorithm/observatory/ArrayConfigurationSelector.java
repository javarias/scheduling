package alma.scheduling.algorithm.observatory;

import java.util.Collection;
import java.util.Date;

import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import alma.scheduling.algorithm.executive.ExecutiveSelector;
import alma.scheduling.algorithm.sbselection.AbstractBaseSelector;
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

    @Override
    public Criterion getCriterion(Date ut, ArrayConfiguration arrConf) {
        double remaningTime = (arrConf.getEndTime().getTime() - ut.getTime()) / (1000 * 60 * 60);
        Criterion crit = Restrictions.le("obsUnitControl.estimatedExecutionTime", new Double(remaningTime));
        return crit;
    }
    

}
