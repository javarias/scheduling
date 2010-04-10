package alma.scheduling.algorithm.obsproject;

import java.util.Collection;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import alma.scheduling.algorithm.astro.TimeUtil;
import alma.scheduling.algorithm.sbselection.AbstractBaseSelector;
import alma.scheduling.algorithm.sbselection.NoSbSelectedException;
import alma.scheduling.datamodel.config.dao.ConfigurationDao;
import alma.scheduling.datamodel.observatory.ArrayConfiguration;
import alma.scheduling.datamodel.obsproject.SchedBlock;
import alma.scheduling.datamodel.obsproject.dao.SchedBlockDao;

public class FieldSourceObservableSelector extends AbstractBaseSelector {
    
    private static Logger logger = LoggerFactory.getLogger(FieldSourceObservableSelector.class);
    
    public FieldSourceObservableSelector(String selectorName) {
        super(selectorName);
    }

    // --- Spring set properties and accessors ---
    
    private ConfigurationDao configDao;
    public void setConfigDao(ConfigurationDao configDao) {
        this.configDao = configDao;
    }

    private SchedBlockDao schedBlockDao;
    public void setSchedBlockDao(SchedBlockDao schedBlockDao) {
        this.schedBlockDao = schedBlockDao;
    }

    // --- SchedBlockSelector impl --
    
    @Override
    public Collection<SchedBlock> select() throws NoSbSelectedException {
        return select(new Date());
    }

    @Override
    public Collection<SchedBlock> select(Date ut) throws NoSbSelectedException {
        double longitude = configDao.getConfiguration().getArrayCenterLongitude();
        double lst = TimeUtil.gstToLST(TimeUtil.utToGST(ut), longitude);
        logger.debug("lst = " + lst);
        return schedBlockDao.findSchedBlocksWithVisibleRepresentativeTarget(lst);
    }

    @Override
    public Collection<SchedBlock> select(ArrayConfiguration arrConf)
            throws NoSbSelectedException {
        return select();
    }

    @Override
    public Collection<SchedBlock> select(Date ut, ArrayConfiguration arrConf)
            throws NoSbSelectedException {
        Collection<SchedBlock> sbs = select(ut);
        printVerboseInfo(sbs, arrConf.getId(), ut);
        return sbs;
    }

}
