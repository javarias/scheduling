package alma.scheduling.algorithm.obsproject;

import java.util.Collection;
import java.util.Date;

import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

import alma.scheduling.algorithm.sbselection.AbstractBaseSelector;
import alma.scheduling.algorithm.sbselection.NoSbSelectedException;
import alma.scheduling.datamodel.observatory.ArrayConfiguration;
import alma.scheduling.datamodel.obsproject.SchedBlock;
import alma.scheduling.datamodel.obsproject.SchedBlockState;
import alma.scheduling.datamodel.obsproject.dao.SchedBlockDao;

public class SchedBlockStatusSelector extends AbstractBaseSelector {

    private SchedBlockDao sbDao;
    
    public SchedBlockDao getSbDao() {
        return sbDao;
    }

    public void setSbDao(SchedBlockDao sbDao) {
        this.sbDao = sbDao;
    }

    public SchedBlockStatusSelector(String selectorName) {
        super(selectorName);
    }

    @Override
    public Collection<SchedBlock> select(Date ut, ArrayConfiguration arrConf)
            throws NoSbSelectedException {
        return sbDao.findSchedBlockWithStatusReady();
    }
    
    @Override
    public Criterion getCriterion(Date ut, ArrayConfiguration arrConf) {
        Criterion crit = Restrictions.eq("schedBlockControl.state", SchedBlockState.READY);
        return crit;
    }

}
